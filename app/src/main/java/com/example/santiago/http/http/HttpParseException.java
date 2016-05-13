package com.example.santiago.http.http;

/**
 * Class for parsing exceptions, nothing new.
 * Is it necessary ?
 */
public class HttpParseException extends Exception {
	
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
