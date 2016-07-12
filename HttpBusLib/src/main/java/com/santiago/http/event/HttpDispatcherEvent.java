package com.santiago.http.event;

import android.support.annotation.NonNull;

import com.santiago.event.Event;

import okhttp3.Dispatcher;

/**
 * Event for attaching a new dispatcher to the client
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpDispatcherEvent extends Event {

    private @NonNull Dispatcher dispatcher;

    public HttpDispatcherEvent(@NonNull Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public @NonNull Dispatcher getDispatcher() { return dispatcher; }

}
