package com.jyl.secKillApi.requestInterceptor;

import com.jyl.secKillApi.service.CheckUserAuthorizationService;
import com.jyl.secKillApi.service.SeckillServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckAuthInterceptor extends HandlerInterceptorAdapter {
    private static Logger logger = LogManager.getLogger(CheckAuthInterceptor.class.getSimpleName());
    private final CheckUserAuthorizationService authService;

    public CheckAuthInterceptor(
            CheckUserAuthorizationService authService
    ) {
        this.authService = authService;
    }

    //before the actual handler will be executed
    // if return true,the execution chain should proceed with the next interceptor or the handler itself.
    // Else, DispatcherServlet assumes that this interceptor has already dealt with the response itself.
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        logger.debug("## request " + request);

        logger.debug("## invoke prehandle(), request.getHeader(\"Authorization\").indexOf(\"Bearer\")  " + request.getHeader("Authorization").substring(0, 6).equalsIgnoreCase("Bearer"));
        if(request.getHeader("Authorization") != null && request.getHeader("Authorization").substring(0, 6).equalsIgnoreCase("Bearer")) {
            logger.debug("## request.getHeader(\"Authorization\") != null ");

            String token = request.getHeader("Authorization"); // include "Bearer" at the front of the string
            String roleName = this.authService.findAccountAuthorization(token); // if the token is not valid, roleName will not be returned. if token is not passed spring security's jwtFilter, no result will be returned.
            return roleName != null && !roleName.isEmpty();
        }
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        return false;
    }
}
