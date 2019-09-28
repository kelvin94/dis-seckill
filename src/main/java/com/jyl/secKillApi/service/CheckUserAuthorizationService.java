package com.jyl.secKillApi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CheckUserAuthorizationService {
    private static Logger logger = LogManager.getLogger(CheckUserAuthorizationService.class.getSimpleName());
    private final RestTemplate resTemplate;
    private final ObjectMapper jsonObjectMapper;
    @Value("${app.authapihost}")
    private String authApiUrl;

    @Value("${app.authapiurl.findAccountAuthorization}")
    private String endPoint;

    // ##Construtor injection
    public CheckUserAuthorizationService(
            RestTemplate restTemplate,
            ObjectMapper jsonObjectMapper
    ) {
        this.resTemplate = restTemplate;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public String findAccountAuthorization(String token) {
        logger.debug("## findAccountAuthorization start" );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        final String url = authApiUrl + endPoint;
        logger.debug("# authApiUrl "+ authApiUrl);
        logger.debug("# endPoint "+ endPoint);

        logger.debug("# url "+ url);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = resTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        logger.debug("# auth api response "+ response);
        String responseBody = response.getBody();
        try {
            Map map = jsonObjectMapper.readValue(responseBody, Map.class);
            return (String) map.get("roleName");

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }
}
