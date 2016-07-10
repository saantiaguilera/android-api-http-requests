package com.santi.testing.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.santiago.event.EventBus;
import com.santiago.http.event.HttpRequestEvent;
import com.santiago.http.http.HttpMethod;
import com.santiago.http.http.HttpParseException;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by santi on 06/07/16.
 */
public class PostHttpRequestEvent extends HttpRequestEvent<String> {
    @NonNull
    @Override
    public String getUrl() {
        return "http://posttestserver.com/post.php";
    }

    @NonNull
    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Nullable
    @Override
    public RequestBody getBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("arg1", "value1");
        builder.addFormDataPart("arg2", "value2");

        return builder.build();
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
