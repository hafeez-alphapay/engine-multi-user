package com.alphapay.payEngine.management.service;

import com.alphapay.payEngine.management.data.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.alphapay.payEngine.account.management.exception.AuthorizationException;
import com.alphapay.payEngine.account.management.exception.MessageResolverService;
import com.alphapay.payEngine.common.bean.ErrorResponse;
import com.alphapay.payEngine.common.exception.BaseWebApplicationException;
import com.alphapay.payEngine.management.data.repository.ApplicationRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

@Component(value = "authorizationHeaderFilter")
public class PayEngineApplicationAuthorizationHeaderFilter extends OncePerRequestFilter {

    final private static Logger logger = LoggerFactory.getLogger(PayEngineApplicationAuthorizationHeaderFilter.class);

    @Autowired
    private MessageResolverService messageResolverService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (httpServletRequest.getRequestURI().equals("/status") || httpServletRequest.getRequestURI().contains("swagger") || httpServletRequest.getRequestURI().equals("/v2/api-docs") || httpServletRequest.getRequestURI().matches("^/actuator/.*") || httpServletRequest.getRequestURI().matches("^/payEnginetest/.*")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            boolean isValidKey = false;
            String Authorization = httpServletRequest.getHeader("Authorization");
            if (Authorization != null) {
                Application application = getByAuthKey(Authorization);
                if (application != null) {
                    httpServletRequest.setAttribute("applicationId", application.getApplicationId());
                    httpServletRequest.setAttribute("applicationApplicationId", application.getId());
                    httpServletRequest.setAttribute("applicationObject", application);
                    isValidKey = true;
                    MDC.put("applicationName", application.getApplicationName());
                }
            }

            // validate the value in xAuth
            if (isValidKey) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } else {
                BaseWebApplicationException authorizationException = new AuthorizationException("APP NOT AUTHORIZED");

                httpServletResponse.setContentType("application/json;charset=UTF-8");
                try (Writer out = httpServletResponse.getWriter()) {
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    ErrorResponse errorResponse = authorizationException.getErrorResponse();
                    errorResponse.setErrorMessage(messageResolverService.resolveLocalizedErrorMessage(authorizationException));
                    errorResponse.setLocalizedErrorMessage(messageResolverService.resolveLocalizedErrorMessage(authorizationException, new Locale("ar")));
                    String json = ow.writeValueAsString(errorResponse);
                    httpServletResponse.setStatus(authorizationException.getStatus());
                    out.write(json);
                } catch (Exception e) {
                    logger.error("failed to write to Authentication Failure response", e);
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication Failure processing failed");
                }
            }
        }
    }

    // @Cacheable(value = "OptimusApplicationIDCache")
    private Application getByAuthKey(String xAuth) {
        return applicationRepository.getByAuthorizationKey(xAuth);
    }
}