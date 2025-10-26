package com.alphapay.payEngine.integration.serviceImpl;

import com.alphapay.payEngine.account.management.exception.DuplicateEntryException;
import com.alphapay.payEngine.alphaServices.model.IntegrationApiToken;
import com.alphapay.payEngine.alphaServices.repository.IntegrationApiTokenRepository;
import com.alphapay.payEngine.common.encryption.EncryptionService;
import com.alphapay.payEngine.common.httpclient.service.RestClientService;
import com.alphapay.payEngine.integration.exception.PaymentProcessorException;
import com.alphapay.payEngine.integration.model.orchast.*;
import com.alphapay.payEngine.integration.repository.ServiceWorkflowRepository;
import com.alphapay.payEngine.integration.repository.ServiceWorkflowStepRepository;
import com.alphapay.payEngine.integration.service.WorkflowService;
import com.alphapay.payEngine.transactionLogging.data.WorkFlowLogs;
import com.alphapay.payEngine.transactionLogging.data.WorkFlowLogsRepository;
import com.alphapay.payEngine.utilities.JsonNodeUtil;
import com.alphapay.payEngine.utilities.SecureSignUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

import static com.alphapay.payEngine.utilities.UtilHelper.mergeData;

@Slf4j
@Service
public class WorkflowImpl implements WorkflowService {
    @Autowired
    RestClientService restClientService;
    @Autowired
     IntegrationApiTokenRepository integrationApiTokenRepository;


    @Autowired
    private ServiceWorkflowRepository workflowRepository;

    @Autowired
    private ServiceWorkflowStepRepository stepRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VelocityTemplateEngine velocityTemplateEngine;

    @Autowired
    WorkFlowLogsRepository workFlowLogsRepository;

    @Autowired
    EncryptionService encryptionService;

    @Value("${mbme.alphapay.hid}")
    private Integer hid;


    public static JsonNode velocityMapper(JsonNode requestJsonNode, String velocityTemplate, VelocityTemplateEngine engine) {
        try {
            Map context = new ObjectMapper().convertValue(requestJsonNode, Map.class);
            String mergedJson = engine.mergeTemplate(velocityTemplate, context);
            return new ObjectMapper().readTree(mergedJson);
        } catch (Exception e) {
            throw new RuntimeException("Velocity template processing failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean isVelocityExpression(String input) {
        return input != null && (
                input.contains("$") ||
                        input.contains("#if") ||
                        input.contains("#foreach") ||
                        input.contains("#set") ||
                        input.contains("${") ||
                        input.contains("#macro")
        );
    }

    public static String decryptMBMEResponseData(String response, String headlessApiKey) throws Exception {
        JSONObject jsonResponse = new JSONObject(response);
        if (!jsonResponse.has("iv")) {
            return response;
        }
        String iv = jsonResponse.getString("iv");
        String data = "";
        if (jsonResponse.has("data")) {
            data = jsonResponse.getString("data");
        } else {
            data = jsonResponse.getString("config");
        }
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] key = sha256.digest(headlessApiKey.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(hexStringToByteArray(iv));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(hexStringToByteArray(data));
        String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
        log.debug("decryptedString:::{}", decryptedString);
        return decryptedString;
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    @Transactional
    public Object executeWorkflow(String workflowName, Object request, String requestId) {
        ServiceWorkflow workflow = workflowRepository.findByName(workflowName)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowName));
        ServiceWorkflowStep currentStep = stepRepository.findByServiceWorkflowAndService(workflow, workflow.getInitialServiceEntity())
                .orElseThrow(() -> new RuntimeException("Initial step not found for workflow: " + workflowName));

        ObjectNode mergedDataToResponse = objectMapper.createObjectNode();
        Object finalResponse = null;
        boolean workflowComplete = false;

        while (!workflowComplete && currentStep != null) {
            // Execute current step
            ExecutionResult result = executeStep(currentStep, request, mergedDataToResponse, workflowName);
            // Log the step execution in DB
            logStep(result,requestId, workflowName);

            // Determine next step based on result
            currentStep = determineNextStep(currentStep, result);
            // Update request context with latest response
            request = result.getMergedResponse();
            // Check if we should return response and exit
            if (result.shouldReturnResponse()) {
                finalResponse = result.getResponse();
                workflowComplete = true;
            }
        }

        return finalResponse;
    }

    void logStep(ExecutionResult result,String requestId,String workflowName)
    {
        try{
            if (result == null) {
                log.warn("ExecutionResult is null, cannot log step");
                return;
            }
            String request = result.getRawRequest();
            String response = result.getRawResponse();

            WorkFlowLogs workFlowLogs = new WorkFlowLogs();
            workFlowLogs.setRequestId(requestId);
            workFlowLogs.setWorkflow(workflowName);
            workFlowLogs.setRequest(maskSensitiveJSON(request));
            workFlowLogs.setRawResponse(maskSensitiveJSON(response));
            workFlowLogs.setMappedResponse("");
                    //JsonNodeUtil.safeToString(result.getMergedResponse(),Boolean.FALSE));
            workFlowLogsRepository.save(workFlowLogs);

        }
        catch(Throwable t)
        {
            log.error("Error while logging step: {}", t.getMessage(), t);
        }
    }

    private ExecutionResult executeStep(ServiceWorkflowStep step, Object request, ObjectNode mergedData, String workflowName) {
        ServiceEntity service = step.getService();
        ResponseEntity<?> response;
        JsonNode responseBodyJsonNode = null;

        String velocityTemplate = convertToVelocityTemplate(service.getRequestMapper());
        JsonNode mappedRequest = velocityMapper((JsonNode) request, velocityTemplate, velocityTemplateEngine);

        /*
        Re-calcualte Hash for MBME
         */
        if (mappedRequest.get("secure_sign") != null) {
            String key = "";
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> mappedRequestMap = mapper.convertValue(mappedRequest, Map.class);
            mappedRequestMap.remove("secure_sign");
            if (((JsonNode) request).has("merchantExternalKey"))
                key = ((JsonNode) request).get("merchantExternalKey").asText();
            String secureSign = SecureSignUtil.generateSecureSign(mappedRequestMap, key, "HmacMD5");
            mappedRequestMap.put("secure_sign", secureSign);
            mappedRequest = objectMapper.valueToTree(mappedRequestMap);

        }

        response = executeService(service, step.getServiceWorkflow().getServiceProvider(), mappedRequest, request, workflowName);
        String jsonString="";
        // Process response
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                 jsonString = objectMapper.writeValueAsString(response.getBody());

                if (service.getIsResponseEncryptedBody()) {
                    jsonString = decryptMBMEResponseData(jsonString, service.getApiEncryptionKey());
                }
                log.debug("ClearResponse----------->{}", jsonString);
                responseBodyJsonNode = objectMapper.readTree(jsonString);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse JSON response", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Merge request/response for context
            mergedData.set("request", objectMapper.valueToTree(request));
            mergedData.set("response", objectMapper.valueToTree(responseBodyJsonNode));
        }

        // Map response if needed
        String responseVelocityTemplate = convertToVelocityTemplate(service.getResponseMapper());
        JsonNode mappedResponse = responseBodyJsonNode != null ?
                velocityMapper(mergedData, responseVelocityTemplate, velocityTemplateEngine) : null;
        mergedData.set("mappedResponse", mappedResponse);


        return new ExecutionResult(
                response.getStatusCode().is2xxSuccessful(),
                mappedResponse,
                mergedData,
                step.getReturnResponse(), JsonNodeUtil.safeToString(mappedRequest,Boolean.FALSE),jsonString
        );
    }

    private ServiceWorkflowStep determineNextStep(ServiceWorkflowStep currentStep, ExecutionResult result) {
        if (result.isSuccess()) {
            return evaluateTransitions(currentStep, currentStep.getSuccessTransitions(), result.getMergedResponse());
        } else {
            return evaluateTransitions(currentStep, currentStep.getFailureTransitions(), result.getMergedResponse());
        }
    }

    private ServiceWorkflowStep evaluateTransitions(ServiceWorkflowStep currentStep,
                                                    Set<ServiceWorkflowTransition> transitions,
                                                    JsonNode context) {
        if (transitions == null || transitions.isEmpty()) {
            return null;
        }
        for (ServiceWorkflowTransition transition : transitions) {
            if (transition.getConditionExpression() != null) {
                try {
                    JsonNode conditionResult = velocityMapper(context, transition.getConditionExpression(), velocityTemplateEngine);
                    if (conditionResult.get("condition").asBoolean()) {
                        ServiceWorkflowStep nextStep = stepRepository.findByServiceWorkflowAndService(
                                currentStep.getServiceWorkflow(),
                                transition.getTargetService()
                        ).orElse(null);
                        return nextStep;
                    }
                } catch (Exception e) {
                    log.warn("Failed to evaluate transition condition", e);
                }
            }
        }

        // Return default transition if no conditions matched
        return transitions.stream().filter(ServiceWorkflowTransition::getIsDefault).findFirst()
                .map(t -> stepRepository.findByServiceWorkflowAndService(
                        currentStep.getServiceWorkflow(),
                        t.getTargetService()
                ).orElse(null))
                .orElse(null);
    }

    private String convertToVelocityTemplate(String mapperConfig) {
        if (mapperConfig == null || mapperConfig.trim().isEmpty()) {
            throw new RuntimeException("Mapper configuration is empty");
        }

        // Check if it's already a Velocity template
        if (mapperConfig.trim().startsWith("#") ||
                mapperConfig.contains("$") ||
                mapperConfig.contains("#foreach") ||
                mapperConfig.contains("#if")) {
//            log.debug("mapperConfig::{}", mapperConfig);
            return mapperConfig;
        }

        // Otherwise try to parse as JSON and convert to Velocity
        try {
            JsonNode configNode = objectMapper.readTree(mapperConfig);
            return transformToVelocity(configNode);
        } catch (Exception e) {
            throw new RuntimeException("Invalid mapper configuration - must be valid JSON or Velocity template", e);
        }
    }

    private ResponseEntity<?> executeService(ServiceEntity serviceEntity, ServiceProvider serviceProvider,
                                             JsonNode request, Object mainRequest, String workflowName) {
        return executeService(serviceEntity,serviceProvider,request,mainRequest,workflowName,null,Boolean.TRUE);

    }

    private ResponseEntity<?> executeService(ServiceEntity serviceEntity, ServiceProvider serviceProvider,
                                             JsonNode request, Object mainRequest, String workflowName, String authToken,
                                             boolean retryIfAuthFailed) {
        // Deep copy of request (JsonNode → JsonNode)
        JsonNode requestCopy = (request == null) ? null : request.deepCopy();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String headerVelocityTemplate = convertToVelocityTemplate(serviceEntity.getJsonHeader());
        JsonNode mappedHeaders = velocityMapper((JsonNode) mainRequest, headerVelocityTemplate, velocityTemplateEngine);

        // Process headers
        mappedHeaders.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            String value = entry.getValue().asText();

            if (key.equals("Authorization")) {
                if(authToken==null)
                    value = serviceEntity.getAuthPrefix() + value;
                else
                    value = serviceEntity.getAuthPrefix() + authToken; //used in mbme stored token
            }
            headers.set(key, value);
        });
        if (workflowName.equals("mf_execute_payment") || workflowName.equals("mbme_direct_payment")) {
            log.debug("clearRequest:----------->{}", maskSensitiveData(request));
        } else {
            log.debug("clearRequest:----------->{}", request);
        }
        if (serviceEntity.getIsRequestEncryptedBody()) {
            try {
                request = objectMapper.readTree(encryptMbmeRequestData(request, serviceEntity.getApiEncryptionKey(),
                        hid));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        HttpEntity<String> requestEntity;
        String endpointUrl = serviceEntity.getEndpointUrl();
        if (isVelocityExpression(endpointUrl)) {
            JsonNode endpoint = velocityMapper((JsonNode) mainRequest, endpointUrl, velocityTemplateEngine);
            endpointUrl = endpoint.get("endPoint").asText();
        }
        log.debug("endpointUrl:----------->{}", endpointUrl);
        String httpMethod = serviceEntity.getHttpMethod();
        if ("GET".equalsIgnoreCase(httpMethod)) {
            StringBuilder urlBuilder = new StringBuilder(endpointUrl);
            if (request != null && request.isObject()) {
                urlBuilder.append("?");
                Iterator<Map.Entry<String, JsonNode>> fields = request.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    urlBuilder.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue().asText());
                    if (fields.hasNext()) {
                        urlBuilder.append("&");
                    }
                }
            }
            endpointUrl = urlBuilder.toString();
            requestEntity = new HttpEntity<>(headers);
        } else {
            requestEntity = new HttpEntity<>(request.toString(), headers);
        }
//        log.debug("requestEntity:::{}", requestEntity);
        try {
            String dfCircuitBK = "paymentCircuitBreaker";
            RestTemplate defaultRestTemplate = restClientService.getGenericRestTemplate();
            return restClientService.invokeRemoteService(endpointUrl,
                    HttpMethod.valueOf(serviceEntity.getHttpMethod()),
                    requestEntity, Object.class, null, defaultRestTemplate, dfCircuitBK);
        } catch (HttpServerErrorException | HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.debug("response:----------->{}", responseBody);
            String errorVelocityTemplate = convertToVelocityTemplate(serviceEntity.getErrorResponseMapper());
            JsonNode responseBodyJsonNode = null;
            try {
                responseBodyJsonNode = objectMapper.readTree(decryptMBMEResponseData(responseBody, serviceEntity.getApiEncryptionKey()));
//                log.debug("ClearResponse:----------->{}", responseBodyJsonNode);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            JsonNode errorMessage = velocityMapper(responseBodyJsonNode, errorVelocityTemplate, velocityTemplateEngine);

            Object[] validationErrors = objectMapper.convertValue(errorMessage.get("validationErrors"), Object[].class);
            if(workflowName.contains("mbme") && responseBodyJsonNode!=null && responseBodyJsonNode.has("status_code"))
            {
                if(401==responseBodyJsonNode.get("status_code").asInt())
                {
                    if(responseBodyJsonNode.has("status_message") && responseBodyJsonNode.get("status_message").asText().contains("token"))
                    {
                        log.debug("MBME Faliure due to token validity , let's refresh and retry if allowed, retry ? = {}",retryIfAuthFailed);
                        if(retryIfAuthFailed)
                        {
                            var row = integrationApiTokenRepository.findOne("mbme_login","mbme_login");
                            String newToken=issueMbmeToken(row);
                            row.setAccessToken(newToken);
                            integrationApiTokenRepository.saveAndFlush(row);
                            return executeService( serviceEntity,  serviceProvider, requestCopy,  mainRequest,  workflowName,  newToken, Boolean.FALSE);

                        }
                    }
                }
            }
            throw new PaymentProcessorException(errorMessage, validationErrors);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException();
        }
    }
    private String maskSensitiveData(Object request)
    {
        try {
            String json = objectMapper.writeValueAsString(request);
            // Mask common sensitive fields using regex
            return maskSensitiveJSON(json);

              } catch (Exception e) {
            return "[Unable to mask sensitive data]";
        }

    }
    private String maskSensitiveJSON(String json) {

            return json
                    .replaceAll("(?i)(\"card_number\"\\s*:\\s*\")\\d+(\"\\s*)", "$1****MASKED****$2")
                    .replaceAll("(?i)(\"cardNumber\"\\s*:\\s*\")\\d+(\"\\s*)", "$1****MASKED****$2")
                    .replaceAll("(?i)(\"card_security_code\"\\s*:\\s*\")\\d+(\"\\s*)", "$1***$2")
                    .replaceAll("(?i)(\"SecurityCode\"\\s*:\\s*\")\\d+(\"\\s*)", "$1***$2")
                    .replaceAll("(?i)(\"cvv\"\\s*:\\s*\")\\d+(\"\\s*)", "$1***$2")
                    .replaceAll("(?i)(\"HolderName\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"Number\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"ExpiryMonth\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"ExpiryYear\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"card_expiry_month\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"card_expiry_year\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"card_name\"\\s*:\\s*\").*?(\")", "$1****$2")
                    .replaceAll("(?i)(\"merchantExternalKey\"\\s*:\\s*\").*?(\")", "$1****MASKED****$2")
                    .replaceAll("(?i)(\"password\"\\s*:\\s*\").*?(\")", "$1****MASKED****$2")
                    .replaceAll("(?i)(\"Password\"\\s*:\\s*\").*?(\")", "$1****MASKED****$2");



    }
    private String transformToVelocity(JsonNode configNode) {
        StringBuilder template = new StringBuilder();
        template.append("{");

        Iterator<Map.Entry<String, JsonNode>> fields = configNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            template.append("\"").append(key).append("\": ");

            if (value.isTextual()) {
                String path = value.asText();
                if (path.startsWith("func_")) {
                    // Handle function calls
                    String[] parts = path.split("_", 3);
                    template.append("\"#evaluateVelocityFunction('")
                            .append(parts[1]).append("'");
                    if (parts.length > 2) {
                        template.append(", '").append(parts[2]).append("'");
                    }
                    template.append(")\"");
                } else if (path.startsWith("$.")) {
                    // Handle path expressions
                    String velocityPath = path.substring(2).replace('.', '_');
                    template.append("\"$").append(velocityPath).append("\"");
                } else {
                    // Handle literal values
                    template.append("\"").append(escapeJsonString(value.asText())).append("\"");
                }
            } else if (value.isObject()) {
                template.append(transformToVelocity(value));
            } else if (value.isArray()) {
                template.append("[");
                Iterator<JsonNode> elements = value.elements();
                while (elements.hasNext()) {
                    template.append(transformToVelocity(elements.next()));
                    if (elements.hasNext()) {
                        template.append(",");
                    }
                }
                template.append("]");
            } else if (value.isNumber()) {
                template.append(value.numberValue());
            } else if (value.isBoolean()) {
                template.append(value.booleanValue());
            } else if (value.isNull()) {
                template.append("null");
            }

            if (fields.hasNext()) {
                template.append(",");
            }
        }

        template.append("}");
        return template.toString();
    }

    private String escapeJsonString(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String encryptMbmeRequestData(Object config, String headlessApiKey, Integer hid) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");

            byte[] keyBytes = digest.digest(headlessApiKey.getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            String jsonConfig = objectMapper.writeValueAsString(config);
//            log.debug("JSON Prior encypt :::{}", jsonConfig);
            byte[] encryptedBytes = cipher.doFinal(jsonConfig.getBytes(StandardCharsets.UTF_8));
            String encryptedData = bytesToHex(encryptedBytes);
            Map<String, Object> encryptedPayload = new HashMap<>();
            encryptedPayload.put("hid", hid);
            encryptedPayload.put("iv", bytesToHex(iv));
            encryptedPayload.put("config", encryptedData);
            return objectMapper.writeValueAsString(encryptedPayload);
        } catch (NoSuchAlgorithmException | JsonProcessingException | IllegalBlockSizeException | BadPaddingException |
                 NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String issueMbmeToken(IntegrationApiToken token) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("merchant_userName", token.getUserName());
        try {
            dataMap.put("merchant_password", encryptionService.decryptPass(token.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonNode mergedDataMbme = mergeData(dataMap);


        Object executeWorkflowLogin = executeWorkflow("mbme_login", mergedDataMbme, UUID.randomUUID().toString());
        log.debug(">>>>> login {} -> of class {}", executeWorkflowLogin,executeWorkflowLogin.getClass());
        // Try nested responseData.authKey first
        String newToken = ((ObjectNode)executeWorkflowLogin).path("responseData").path("authKey").asText(null);

        // Fallback to top-level authKey if missing
        if (newToken == null || newToken.isEmpty()) {
            newToken = ((ObjectNode)executeWorkflowLogin).path("authKey").asText();
        }

        log.debug("Extracted Token: {}",  newToken);
        return newToken;
    }
}