package com.example.santiago.http.event;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.example.santiago.event.Event;
import com.example.santiago.event.listener.EventListener;
import com.example.santiago.http.http.HttpParseException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class for requests.
 * Currently, requests are defined inside subclasses of RequestEvent, this means each
 * request you will have you will define it inside one of these.
 *
 * Created by santiago on 13/05/16.
 */
public abstract class RequestEvent<E> extends Event {

    /**
     * Implement it and here create your request (use the params you get from a constructor
     * or whatever)...
     *
     * @return
     */
    protected abstract Request buildRequest();

    /**
     * Creates an OkHttpClient to manage the request.
     * If you need to set timeouts / authenticators / ssl / proxy / whatever override this
     * method and use your own OkHttpClient instance. By default it wont have anything.
     *
     * @return Empty OkHttpClient instance
     */
    protected @NonNull OkHttpClient buildClient() { return new OkHttpClient(); }

    /**
     * Implement it and parse the response you will receive to form whatever you expect this
     * request to return
     *
     * @param response . use its body() to fetch the data. Remember its a buffer so you will
     *                 flush it (meaning you can only do this once per request)
     * @return Object expected to receive from the request
     * @throws HttpParseException in case there was a malformed response
     */
    protected abstract E parseResponse(@NonNull Response response) throws HttpParseException;

    /**
     * If the requests fails, it will execute this method.
     * Override it if you want your request to have a defined logic for when the request fails
     *
     * @param dispatcher use it if you need to dispatch events
     * @param exception exception thrown
     */
    protected void onHttpRequestFailure(@NonNull EventListener dispatcher, @NonNull Exception exception) {}

    /**
     * If the requests succeeds, it will execute this method.
     * Override it if you want your request to have a defined logic for when the request is OK
     *
     * @param dispatcher use it if you need to dispatch events
     * @param result the result?
     */
    protected void onHttpRequestSuccess(@NonNull EventListener dispatcher, E result) {}

    /**
     * Uses double dispatch with the EventListener to handle different events inside here.
     *
     * Builds a request and executes it.
     * If everything goes fine onHttpRequestSuccess() will be called else
     * onHttpRequestFailure will be.
     *
     * @param dispatcher EventListener
     */
    @RequiresPermission(Manifest.permission.INTERNET)
    public void execute(@NonNull EventListener dispatcher) {
        try {
            Request request =  buildRequest();

            if (request == null)
                throw new NullPointerException("Request is null in " + getClass().getName());

            Response response = buildClient().newCall(request).execute();

            E e = parseResponse(response);

            onHttpRequestSuccess(dispatcher, e);
        } catch (IOException | HttpParseException e) {
            onHttpRequestFailure(dispatcher, e);
        }
    }

}
