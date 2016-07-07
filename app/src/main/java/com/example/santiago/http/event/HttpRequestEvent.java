package com.example.santiago.http.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.santiago.event.Event;
import com.example.santiago.http.http.HttpMethod;
import com.example.santiago.http.http.HttpParseException;


import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class for requests.
 * Currently, requests are defined inside subclasses of HttpRequestEvent, this means each
 * request you will have you will define it inside one of these.
 *
 * Created by santiago on 13/05/16.
 */
public abstract class HttpRequestEvent<E> extends Event {

    public abstract @NonNull String getUrl();

    public abstract @NonNull HttpMethod getHttpMethod();

    public @Nullable RequestBody getBody() {
        return null;
    }

    public @Nullable Headers getHeaders() {
        return null;
    };

    /**
     * Implement it and parse the response you will receive to form whatever you expect this
     * request to return
     *
     * @param response . use its body() to fetch the data. Remember its a buffer so you will
     *                 flush it (meaning you can only do this once per request)
     * @return Object expected to receive from the request
     * @throws HttpParseException in case there was a malformed response
     */
    public abstract E parseResponse(@NonNull Response response) throws HttpParseException;

    /**
     * If the requests fails, it will execute this method.
     * Override it if you want your request to have a defined logic for when the request fails
     *
     * @param exception exception thrown
     */
    public void onHttpRequestFailure(@NonNull Exception exception) {}

    /**
     * If the requests succeeds, it will execute this method.
     * Override it if you want your request to have a defined logic for when the request is OK
     *
     * @param result the result?
     */
    public void onHttpRequestSuccess(E result) {}


}
