package com.example.santiago.http.event;

import com.example.santiago.event.Event;

import okhttp3.Interceptor;

/**
 * Created by santi on 06/07/16.
 */
public class HttpInterceptorEvent extends Event {

    private boolean isNetwork = false;
    private Interceptor interceptor;

    public HttpInterceptorEvent(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public HttpInterceptorEvent(Interceptor interceptor, boolean isFromNetwork) {
        this.isNetwork = isFromNetwork;
        this.interceptor = interceptor;
    }

    public boolean isNetwork() { return isNetwork; }

    public Interceptor getInterceptor() { return interceptor; }

}
