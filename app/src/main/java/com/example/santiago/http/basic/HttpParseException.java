package com.example.santiago.http.basic;

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
