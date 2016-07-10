package com.santi.testing.event;

import com.santiago.event.Event;

/**
 * Created by santiago on 13/05/16.
 */
public class SuccessEvent extends Event {

    private String string;

    public SuccessEvent(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

}
