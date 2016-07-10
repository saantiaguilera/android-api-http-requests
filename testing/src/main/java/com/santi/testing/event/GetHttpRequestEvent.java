package com.santi.testing.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.santiago.event.EventBus;
import com.santiago.http.event.HttpRequestEvent;
import com.santiago.http.http.HttpMethod;
import com.santiago.http.http.HttpParseException;
import com.santiago.loader.HttpBus;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by santiago on 13/05/16.
 */
public class GetHttpRequestEvent extends HttpRequestEvent<String> {

    @NonNull
    @Override
    public String getUrl() {
        return "https://httpbin.org/get?show_env=1";
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
        HttpBus.getInstance().dispatchEvent(new FailureEvent(exception));
    }

    @Override
    public void onHttpRequestSuccess(String result) {
        HttpBus.getInstance().dispatchEvent(new SuccessEvent(result));
    }

}
