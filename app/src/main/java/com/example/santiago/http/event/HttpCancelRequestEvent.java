package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

/**
 * Created by saantiaguilera on 07/07/16.
 */
public class HttpCancelRequestEvent extends Event {

    private @NonNull
    HttpRequestEvent event;

    public HttpCancelRequestEvent(@NonNull HttpRequestEvent cancelEvent) {
        event = cancelEvent;
    }

    public @NonNull
    HttpRequestEvent getCancelEvent() {
        return event;
    }

}
