package com.example.santiago.http_requests.http;

public class HttpParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5059274241411209996L;
	
	
	public HttpParseException() {}
	
	public HttpParseException(String detailMessage) {
		super(detailMessage);
	}
	
	public HttpParseException(Throwable throwable) {
		super(throwable);
	}
	
	public HttpParseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
	
}
