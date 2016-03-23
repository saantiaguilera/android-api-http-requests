package com.example.santiago.http_requests.http;

import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Using this is the same as the response, but the body is already consumed so you dont have to worry
 * about not consuming it twice.
 *
 * Created by santi on 23/03/16.
 */
public class HttpResponse {

    private Request request;
    private Protocol protocol;
    private String message;
    private int code;
    private String body;

    public HttpResponse(Response response) {
        request = response.request();
        protocol = response.protocol();
        message = response.message();
        code = response.code();

        try {
            body = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getCode() {
        return code;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Request getRequest() {
        return request;
    }

    public String getBody() {
        return body;
    }

    public String getMessage() {
        return message;
    }

}
