package com.alphapay.payEngine.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class JSONParseUtil {
    private static final Logger logger = LoggerFactory.getLogger(JSONParseUtil.class);

    public static HashMap<String, Object> jsonResponseConvertorDeprecated(String json) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(json,HashMap.class);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        logger.debug(map.toString());

        return map;

    }

    public static HashMap<String, Object> jsonResponseConvertor(String json) {
        HashMap<String, Object> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(json);

            if (rootNode.isArray()) {
                // If it's an array, process as a list and put it under "dataList" key
                List<HashMap<String, Object>> list = mapper.readValue(json, new TypeReference<List<HashMap<String, Object>>>(){});
                result.put("dataList", list);
            } else if (rootNode.isObject()) {
                // If it's an object, process as a single HashMap
                HashMap<String, Object> map = mapper.readValue(json, new TypeReference<HashMap<String, Object>>(){});
                result = map;
            }
        } catch (JsonProcessingException e) {
            logger.error("JSON Processing Error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error("IO Error: " + e.getLocalizedMessage());
        }

        logger.debug(result.toString());

        return result;
    }


}
