package com.jyl.secKillApi.controller;

import com.jyl.secKillApi.execptions.RepeatkillException;
import com.jyl.secKillApi.execptions.SeckillCloseException;
import com.jyl.secKillApi.execptions.SeckillException;
import com.jyl.secKillApi.stateenum.SeckillStateEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static Logger logger = LogManager.getLogger(GlobalExceptionHandler.class.getSimpleName());

    /*
     * Provides handling for exceptions throughout this service. */
    @ExceptionHandler({RepeatkillException.class})
    public ResponseEntity<String> handleRepeatkillException(RepeatkillException ex) {
        logger.debug("#handleRepeatkillException");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(ex.getMessage(), responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SeckillCloseException.class})
    public ResponseEntity handleSeckillCloseException(Exception ex) {
        logger.debug("#handleSeckillCloseException");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(ex.getMessage(), responseHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SeckillException.class})
    public ResponseEntity handleSeckillException(Exception ex) {
        logger.debug("#handleSeckillException");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(ex.getMessage(), responseHeaders, HttpStatus.BAD_REQUEST);
    }

    // General ExceptionHandler class
    @ExceptionHandler({Exception.class})
    public ResponseEntity handleGeneralException(Exception ex) {
        logger.debug("#handleGeneralException");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<String>(ex.getMessage(), responseHeaders, HttpStatus.BAD_REQUEST);
    }
}
