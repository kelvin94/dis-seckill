package com.jyl.secKillApi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jyl.secKillApi.entity.SeckillSwag;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.util.GeneralUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class SeckillController {
    private static Logger logger = LogManager.getLogger(SeckillController.class.getSimpleName());

//    private final GeneralUtil util;
    private final SwagRepository swagRepo;

    public SeckillController(SwagRepository swagRepo) {
        this.swagRepo = swagRepo;
    }


    @RequestMapping(path = "/swags", method = RequestMethod.GET)
    public String showAllSwagsController() throws JsonProcessingException {
        List<SeckillSwag> result = new ArrayList<SeckillSwag>(swagRepo.findAll());

        return GeneralUtil.convertToJson(result);
    }


}
