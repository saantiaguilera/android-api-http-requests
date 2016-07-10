package com.santi.testing.event;

import com.santiago.event.Event;

/**
 * Created by santiago on 13/05/16.
 */
public class FailureEvent extends Event {

    private Exception e;

    public FailureEvent(Exception e) {
        this.e = e;
    }

    public Exception getException() {
        return e;
    }

}
