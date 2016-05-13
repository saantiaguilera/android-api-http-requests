package com.example.santiago.testings.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.listener.EventListener;
import com.example.santiago.http.basic.HttpParseException;
import com.example.santiago.http.event.RequestEvent;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by santiago on 13/05/16.
 */
public class GetRequestEvent extends RequestEvent<String> {

    @Override
    protected Request buildRequest() {
        return new Request.Builder()
                .url("http://httpbin.org/get")
                .get()
                .build();
    }

    @Override
    protected String parseResponse(@NonNull Response response) throws HttpParseException {
        try {
            return response.body().string();
        } catch (IOException e) {
            throw new HttpParseException(e);
        }
    }

    @Override
    protected void onHttpRequestFailure(@NonNull EventListener dispatcher, @NonNull Exception exception) {
        dispatcher.dispatchEvent(new FailureEvent(exception));
    }

    @Override
    protected void onHttpRequestSuccess(@NonNull EventListener dispatcher, String result) {
        dispatcher.dispatchEvent(new SuccessEvent(result));
    }

}
