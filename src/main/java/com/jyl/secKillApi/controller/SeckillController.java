package com.jyl.secKillApi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jyl.secKillApi.dto.SeckillExecution;
import com.jyl.secKillApi.dto.UrlExposer;
import com.jyl.secKillApi.entity.SeckillSwag;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.resource.SeckillParameter;
import com.jyl.secKillApi.service.SeckillService;
import com.jyl.secKillApi.util.GeneralUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@RestController
public class SeckillController {
    private static Logger logger = LogManager.getLogger(SeckillController.class.getSimpleName());


    private final SeckillService seckillService;

    public SeckillController(
            SeckillService seckillService
    ) {
        this.seckillService = seckillService;
    }


    @RequestMapping(path = "/swags", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    public String showAllSwagsController() throws JsonProcessingException {
        List<SeckillSwag> result = seckillService.findAll();
        logger.debug("###result db " + result);

        String response = "";
        try {
            response = GeneralUtil.convertToJson(result);

        } catch (Exception ex ) {
            logger.error("JSON processing exception!!" + ex.getMessage());
            throw ex;
        }
        return response;
    }



    @RequestMapping(path = "/swags/{seckillSwagId}", method = RequestMethod.GET, produces =  MediaType.APPLICATION_JSON_VALUE)
    public String findOneSwag(@PathVariable(value = "seckillSwagId", required = true) Long seckillSwagId) {
        SeckillSwag result = seckillService.findBySeckillSwagId(seckillSwagId);
        try {
            return GeneralUtil.convertToJson(result);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    @RequestMapping(path = "/swags/{seckillSwagId}/expose", method = RequestMethod.GET, produces =  MediaType.APPLICATION_JSON_VALUE)
    public String exposeSwag(@PathVariable(value = "seckillSwagId", required = true) Long seckillSwagId) {
        UrlExposer result = seckillService.exportSeckillUrl(seckillSwagId);
        if(result != null) {
            try {
                return GeneralUtil.convertToJson(result);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return null;
    }

    @RequestMapping(path = "/swags", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String killSwag(@Valid @RequestBody SeckillParameter requestParam) throws Exception {
        SeckillExecution result = null;
        int invalidState = -1; // 无效 for any exception thrown

//        try {
            result = seckillService.executeSeckill(requestParam);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            return GeneralUtil.convertToJson(new SeckillExecution(requestParam.getSeckillSwagId(), invalidState, e.getMessage()));
//        }
        if(result != null) {
//            try {
                return GeneralUtil.convertToJson(result);
//            } catch (Exception ex) {
//                logger.error(ex.getMessage());
//                return GeneralUtil.convertToJson(new SeckillExecution(requestParam.getSeckillSwagId(), invalidState, ex.getMessage()));
//            }
        }
        return null;
    }


}
