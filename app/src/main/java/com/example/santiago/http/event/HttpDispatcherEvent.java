package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

import okhttp3.Dispatcher;

/**
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpDispatcherEvent extends Event {

    private @NonNull
    Dispatcher dispatcher;

    public HttpDispatcherEvent(@NonNull Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public @NonNull
    Dispatcher getDispatcher() { return dispatcher; }

}
