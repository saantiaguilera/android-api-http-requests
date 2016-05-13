package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;
import com.example.santiago.event.listener.EventListener;
import com.example.santiago.http.basic.HttpParseException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by santiago on 13/05/16.
 */
public abstract class RequestEvent<E> extends Event {

    protected abstract Request buildRequest();
    protected @NonNull OkHttpClient buildClient() { return new OkHttpClient(); }

    protected abstract E parseResponse(@NonNull Response response) throws HttpParseException;

    protected void onHttpRequestFailure(@NonNull EventListener dispatcher, @NonNull Exception exception) {}
    protected void onHttpRequestSuccess(@NonNull EventListener dispatcher, E result) {}

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
