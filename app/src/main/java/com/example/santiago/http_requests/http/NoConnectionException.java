package com.example.santiago.http_requests.http;

public class NoConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5059274241411209996L;
	
	
	public NoConnectionException() {}
	
	public NoConnectionException(String detailMessage) {
		super(detailMessage);
	}
	
}
