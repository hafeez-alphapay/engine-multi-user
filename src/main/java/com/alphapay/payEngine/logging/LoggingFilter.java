package com.alphapay.payEngine.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LoggingFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
	private static final String REQUEST_PREFIX = "Request: ";
	private static final String RESPONSE_PREFIX = "Response: ";


	private void logRequest(final HttpServletRequest request) {
		StringBuilder msg = new StringBuilder();
		msg.append(REQUEST_PREFIX);
		if (request instanceof RequestLoggingWrapper) {
			msg.append("request id=")
					.append(((RequestLoggingWrapper) request).getId())
					.append("; ");
		}
		HttpSession session = request.getSession(false);
		if (session != null) {
			msg.append("session id=").append(session.getId()).append("; ");
		}
		if (request.getContentType() != null) {
			msg.append("content type=").append(request.getContentType())
					.append("; ");
		}
		msg.append("method=").append(request.getMethod()).append("; ");
		msg.append("uri=").append(request.getRequestURI());
		if (request.getQueryString() != null) {
			msg.append('?').append(request.getQueryString());
		}

		if (request instanceof RequestLoggingWrapper && !isMultipart(request)) {
			RequestLoggingWrapper RequestLoggingWrapper = (RequestLoggingWrapper) request;
			try {
				String charEncoding = RequestLoggingWrapper
						.getCharacterEncoding() != null ? RequestLoggingWrapper
						.getCharacterEncoding() : "UTF-8";
				msg.append("; payload=").append(
						new String(RequestLoggingWrapper.toByteArray(),
								charEncoding));
			} catch (UnsupportedEncodingException e) {
				logger.warn("Failed to parse request payload", e);
			}
		}
		logger.trace(msg.toString());
	}

	private void logResponse(final ResponseLoggingWrapper response) {
		StringBuilder msg = new StringBuilder();
		msg.append(RESPONSE_PREFIX);
		msg.append("request id=").append((response.getId()));
		try {
			msg.append("; payload=").append(
					new String(response.toByteArray(), response
							.getCharacterEncoding()));
		} catch (UnsupportedEncodingException e) {
			logger.warn("Failed to parse response payload", e);
		}
		logger.trace(msg.toString());
	}

	private boolean isMultipart(final HttpServletRequest request) {
		return request.getContentType() != null
				&& request.getContentType().startsWith("multipart/form-data");
	}

	private String readRememberMeCookie(final HttpServletRequest request, String cookieName) {
		String cookieValue = "";

		 Cookie[] cookies = request.getCookies();

		if(cookies != null) {
			for(Cookie cookie: cookies) {
				if (cookie.getName().equals(cookieName))
					return cookie.getValue();
			}
		}
		return cookieValue;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (logger.isDebugEnabled()) {
			long id = System.currentTimeMillis();
			RequestLoggingWrapper requestLoggingWrapper = new RequestLoggingWrapper(
					id, request);
			ResponseLoggingWrapper responseLoggingWrapper = new ResponseLoggingWrapper(
					id, response);
			if(!request.getRequestURI().equals("/status")) {
				logger.debug("{}: http request {}", id, request.getRequestURI());
			}
			try {
				filterChain.doFilter(requestLoggingWrapper, responseLoggingWrapper);
			}
			finally {
				if(!request.getRequestURI().equals("/status")) {
					logRequest(requestLoggingWrapper);
					logResponse(responseLoggingWrapper);
				}
			}
			if(!request.getRequestURI().equals("/status")) {
				logger.debug("{}: http response {}  finished in {} ms", id, response.getLocale(),(System.currentTimeMillis() - id));
			}
		}
	}
}
