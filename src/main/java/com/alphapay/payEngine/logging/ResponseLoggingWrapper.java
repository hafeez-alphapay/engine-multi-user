package com.alphapay.payEngine.logging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseLoggingWrapper extends HttpServletResponseWrapper {

	private static final Logger log = LoggerFactory.getLogger(ResponseLoggingWrapper.class);

	private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
	private long id;
	private PrintWriter writer = new PrintWriter(bos);

	/**
	 * @param requestId
	 *            and id which gets logged to output file. It's used to bind
	 *            response with response (they will have same id,
	 *            currenttimemilis is used)
	 * @param response
	 *            response from which we want to extract stream data
	 */
	public ResponseLoggingWrapper(Long requestId, HttpServletResponse response) {
		super(response);
		this.id = requestId;
	}
	
	 @Override
	    public HttpServletResponse getResponse() {
	        return this;
	    }

	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		final ServletOutputStream servletOutputStream = ResponseLoggingWrapper.super.getOutputStream();

		return new ServletOutputStream() {

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {

			}

			private TeeOutputStream tee = new TeeOutputStream(servletOutputStream, bos);

			@Override
			public void write(int b) throws IOException {
				tee.write(b);
			}
		};
	}

	public void logRequest() {
		byte[] toLog = toByteArray();
		if (toLog != null && toLog.length > 0)
			log.info(getId() + ": http response " + new String(toLog));
	}

	@Override
    public PrintWriter getWriter() throws IOException {
        return new TeePrintWriter(super.getWriter(), writer);
    }
	
	/**
	 * this method will clear the buffer, so
	 *
	 * @return captured bytes from stream
	 */
	public byte[] toByteArray() {
		byte[] ret = bos.toByteArray();
		bos.reset();
		return ret;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}