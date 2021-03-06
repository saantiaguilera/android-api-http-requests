package com.santi.testing.event;

import android.support.annotation.NonNull;

import com.santiago.event.EventBus;
import com.santiago.http.event.HttpRequestEvent;
import com.santiago.http.http.HttpMethod;
import com.santiago.http.http.HttpParseException;
import com.santiago.loader.HttpBus;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by santi on 06/07/16.
 */
public class DelayedHttpRequestEvent extends HttpRequestEvent<String> {
    @NonNull
    @Override
    public String getUrl() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "https://httpbin.org/get?show_env=1";
    }

    @NonNull
    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
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
        HttpBus.getInstance().dispatchEvent(new FailureEvent(exception));
    }

    @Override
    public void onHttpRequestSuccess(String result) {
        HttpBus.getInstance().dispatchEvent(new SuccessEvent(result));
    }

}
