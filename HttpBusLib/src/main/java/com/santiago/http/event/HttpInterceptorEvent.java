package com.santiago.http.event;

import android.support.annotation.NonNull;

import com.santiago.event.Event;

import okhttp3.Interceptor;

/**
 * Event for adding interceptors
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpInterceptorEvent extends Event {

    private boolean isNetwork = false;
    private @NonNull Interceptor interceptor;

    public HttpInterceptorEvent(@NonNull Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public HttpInterceptorEvent(@NonNull Interceptor interceptor, boolean isFromNetwork) {
        this.isNetwork = isFromNetwork;
        this.interceptor = interceptor;
    }

    public boolean isNetwork() { return isNetwork; }

    public @NonNull Interceptor getInterceptor() { return interceptor; }

}
