package com.alphapay.payEngine.common.httpclient.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/*
Class have been upgraded to match httpClient 5 specs
 */
@Service
public class RestClientService {
    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;
    private static final Logger logger = LoggerFactory.getLogger(RestClientService.class);

    @Value("${circuit.bk.enabled}")
    private boolean circuitBreakerEnabled;

    @Autowired
    RestTemplate genericRestTemplate;


    public RestTemplate getGenericRestTemplate() {
        return this.genericRestTemplate;
    }


    /**
     * Invokes a remote service.
     *
     * @param url the service URL.
     * @param method the HTTP method.
     * @param requestEntity the request entity, including headers and body.
     * @param responseType the expected response type.
     * @param uriVariables URI variables if needed.
     * @return a ResponseEntity with the specified type.
     */
    public <T> ResponseEntity<T> invokeRemoteService(String url,
                                                     HttpMethod method,
                                                     HttpEntity<?> requestEntity,
                                                     Class<T> responseType,
                                                     Map<String, ?> uriVariables) {
        ResponseEntity<T> response;
        if (uriVariables == null) {
            response = genericRestTemplate.exchange(url, method, requestEntity, responseType);
        } else {
            response = genericRestTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        }
        return response;
    }

    public <T> ResponseEntity<T> invokeRemoteService(String url,
                                                     HttpMethod method, MediaType contentType, HttpEntity<?> requestEntity,
                                                     Class<T> responseType, Map<String, ?> uriVariables, RestTemplate restTemplate) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        HttpEntity<?> newRequestEntity = new HttpEntity<>(requestEntity.getBody(), headers);

        ResponseEntity<T> response;

        if (uriVariables == null) {
            response = restTemplate.exchange(url, method, newRequestEntity, responseType);
        } else {
            response = restTemplate.exchange(url, method, newRequestEntity, responseType, uriVariables);
        }

        return response;
    }

    /**
     * Generic method to invoke SyberConsumer remote api services. This method uses RestTemplate generic exchange method.
     *
     * @param url
     * @param method
     * @param requestEntity
     * @param responseType
     * @param uriVariables
     * @return
     */
    public <T> ResponseEntity<T> invokeRemoteService(String url,
                                                     HttpMethod method, HttpEntity<?> requestEntity,
                                                     Class<T> responseType, Map<String, ?> uriVariables, RestTemplate rest,String circuitBreaker) {
        if(circuitBreakerEnabled && StringUtils.isNotBlank(circuitBreaker))
        {
            return invokeRemoteServiceWithCB(url, method, requestEntity,
                    responseType, uriVariables, rest,circuitBreaker);
        }
        else
        {
            return invokeService(url, method, requestEntity,
                    responseType, uriVariables , rest);
        }


    }

    public <T> ResponseEntity<T> invokeRemoteService(String url,
                                                     HttpMethod method, HttpEntity<?> requestEntity,
                                                     Class<T> responseType, Map<String, ?> uriVariables, RestTemplate rest) {
        return invokeRemoteService(url,method,requestEntity,responseType,uriVariables,rest,null);

    }


    public <T> ResponseEntity<T> invokeRemoteServiceWithCB(String url,
                                                     HttpMethod method, HttpEntity<?> requestEntity,
                                                     Class<T> responseType, Map<String, ?> uriVariables, RestTemplate restTemplate, String cb) {

        logger.debug("{} to {}", method, url);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(cb, cb);
        ResponseEntity<T> response = null;
        if(uriVariables==null)
            response = circuitBreaker.executeSupplier(() -> restTemplate.exchange(url,method, requestEntity,
                    responseType) );
        else
            response = circuitBreaker.executeSupplier(() -> restTemplate.exchange(url,method, requestEntity,
                    responseType,uriVariables) );


        if (response != null)
            logger.debug("response: {}", response.getStatusCode());

        return response;

    }
    public <T> ResponseEntity<T> invokeService(String url,
                                                     HttpMethod method, HttpEntity<?> requestEntity,
                                                     Class<T> responseType, Map<String, ?> uriVariables, RestTemplate restTemplate) {

        logger.debug("{} to {}", method, url);
        ResponseEntity<T> response = null;
        //if (uriVariables == null)
            response =  restTemplate.postForEntity(url, requestEntity,
                    responseType) ;


        if (response != null)
            logger.debug("response: {}", response.getStatusCode());

        return response;

    }

    /**
     * Checks the health check of the remote service.
     *
     * @return String health-check message from the remote service
     */
    public ResponseEntity<String> pingRemoteService(RestTemplate template) {
        return invokeRemoteService("/get", HttpMethod.GET, null, String.class, null, template);
    }

}
