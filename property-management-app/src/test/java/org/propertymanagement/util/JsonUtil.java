package org.propertymanagement.util;

import tools.jackson.databind.json.JsonMapper;

public interface JsonUtil {
    static String asJsonString(final Object obj) {
        return JsonMapper.builder().build().writeValueAsString(obj);
    }
}
