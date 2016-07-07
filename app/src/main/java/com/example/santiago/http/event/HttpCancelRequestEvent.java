package com.example.santiago.http.event;

import com.example.santiago.event.Event;

/**
 * Created by santi on 07/07/16.
 */
public class HttpCancelRequestEvent extends Event {

    private HttpRequestEvent event;

    public HttpCancelRequestEvent(HttpRequestEvent cancelEvent) {
        event = cancelEvent;
    }

    public HttpRequestEvent getCancelEvent() {
        return event;
    }

}
