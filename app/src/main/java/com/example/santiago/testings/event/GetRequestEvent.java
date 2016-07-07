package com.example.santiago.testings.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.santiago.event.EventBus;
import com.example.santiago.http.event.RequestEvent;
import com.example.santiago.http.http.HttpMethod;
import com.example.santiago.http.http.HttpParseException;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by santiago on 13/05/16.
 */
public class GetRequestEvent extends RequestEvent<String> {

    @NonNull
    @Override
    public String getUrl() {
        return "http://httpbin.org/get";
    }

    @NonNull
    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Nullable
    @Override
    public RequestBody getBody() {
        return null;
    }

    @Nullable
    @Override
    public Headers getHeaders() {
        return null;
    }

    @Override
    public String parseResponse(@NonNull Response response) throws HttpParseException {
        try {
            return response.body().string();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public void onHttpRequestFailure(@NonNull Exception exception) {
        EventBus.getHttpBus().dispatchEvent(new FailureEvent(exception));
    }

    @Override
    public void onHttpRequestSuccess(String result) {
        EventBus.getHttpBus().dispatchEvent(new SuccessEvent(result));
    }

}
