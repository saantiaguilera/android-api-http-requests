package com.example.santiago.testings.event;

import com.example.santiago.event.Event;

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
