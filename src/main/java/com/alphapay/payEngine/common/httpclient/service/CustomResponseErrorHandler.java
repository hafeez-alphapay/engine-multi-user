package com.alphapay.payEngine.common.httpclient.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
@Slf4j
public class CustomResponseErrorHandler implements ResponseErrorHandler{

    private ResponseErrorHandler myErrorHandler = new DefaultResponseErrorHandler();
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return RestUtil.isError(clientHttpResponse.getStatusCode());
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        log.error("Response error: {} {}", clientHttpResponse.getStatusCode(), clientHttpResponse.getStatusText());
    }
}
