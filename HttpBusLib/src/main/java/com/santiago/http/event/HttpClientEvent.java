package com.santiago.http.event;

import android.support.annotation.NonNull;

import com.santiago.event.Event;

import okhttp3.OkHttpClient;

/**
 * Created by saguilera on 9/2/16.
 */
public class HttpClientEvent extends Event {

    private @NonNull OkHttpClient okHttpClient;

    public HttpClientEvent(@NonNull OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @NonNull
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

}
