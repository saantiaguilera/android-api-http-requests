package com.santiago.http.event;

import android.support.annotation.NonNull;

import com.santiago.event.Event;

/**
 * Event for cancelling a particular request
 * Created by saantiaguilera on 07/07/16.
 */
public class HttpCancelRequestEvent extends Event {

    private @NonNull HttpRequestEvent event;

    public HttpCancelRequestEvent(@NonNull HttpRequestEvent cancelEvent) {
        event = cancelEvent;
    }

    public @NonNull HttpRequestEvent getCancelEvent() {
        return event;
    }

}
