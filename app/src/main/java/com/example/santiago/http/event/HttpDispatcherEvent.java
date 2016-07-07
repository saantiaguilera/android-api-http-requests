package com.example.santiago.http.event;

import com.example.santiago.event.Event;

import okhttp3.Dispatcher;

/**
 * Created by santi on 06/07/16.
 */
public class HttpDispatcherEvent extends Event {

    private Dispatcher dispatcher;

    public HttpDispatcherEvent(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Dispatcher getDispatcher() { return dispatcher; }

}
