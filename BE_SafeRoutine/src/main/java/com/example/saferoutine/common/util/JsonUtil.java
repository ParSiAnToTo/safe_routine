package com.example.saferoutine.common.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JsonUtil {
    /**
     * JSONObject => Map<String, String>
     * @param {JSONObject} jsonObject
     * @returns {Map} map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObject) {
        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonObject.toString(), Map.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * JSONArray => List<Map<String, String>>
     * @param {jsonArray} jsonArray
     * @returns {List} list
     */
    public static List<Map<String, Object>> getListMapFromJsonArray(JSONArray jsonArray) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        if (jsonArray != null) {

            int jsonSize = jsonArray.size();

            for (int i = 0; i < jsonSize; i++) {

                Map<String, Object> map = getMapFromJsonObject((JSONObject)jsonArray.get(i));
                list.add(map);

            }
        }
        return list;
    }
}
