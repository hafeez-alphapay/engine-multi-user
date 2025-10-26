package com.alphapay.payEngine.common.httpclient.service;


import org.springframework.http.HttpStatusCode;

public class RestUtil {

        public static boolean isError(HttpStatusCode status) {
            return status.isError();
        }
}
