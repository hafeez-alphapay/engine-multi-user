package com.alphapay.payEngine.logging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** * Request logging wrapper using proxy split stream to extract request body */
public class RequestLoggingWrapper extends HttpServletRequestWrapper {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingWrapper.class);

	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
	private long id;

	/**
	 * @param requestId
	 *            and id which gets logged to output file. It's used to bind
	 *            request with response
	 * @param request
	 *            request from which we want to extract post data
	 */
	public RequestLoggingWrapper(Long requestId, HttpServletRequest request) {
		super(request);
		this.id = requestId;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ServletInputStream servletInputStream = RequestLoggingWrapper.super
				.getInputStream();
		return new ServletInputStream() {


			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}

			private TeeInputStream tee = new TeeInputStream(servletInputStream,
					bos);

			@Override
			public int read() throws IOException {
				return tee.read();
			}
		};
	}

	public byte[] toByteArray() {
		return bos.toByteArray();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}