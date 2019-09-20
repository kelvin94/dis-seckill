package com.jyl.secKillApi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jyl.secKillApi.controller.SeckillController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeneralUtil {
    private static Logger logger = LogManager.getLogger(SeckillController.class.getSimpleName());

    public static String convertToJson(Object obj) throws JsonProcessingException {

        if(obj != null) {
            ObjectMapper mapper = new ObjectMapper();
            logger.debug("## obj " + mapper.writeValueAsString(obj));
            return mapper.writeValueAsString(obj);
        }
        return "";
    }
}
