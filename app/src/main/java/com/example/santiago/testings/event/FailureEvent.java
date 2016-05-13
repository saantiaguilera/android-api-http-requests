package com.example.santiago.testings.event;

import com.example.santiago.event.Event;

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
