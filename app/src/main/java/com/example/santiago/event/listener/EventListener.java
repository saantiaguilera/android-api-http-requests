package com.example.santiago.event.listener;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

/**
 * Created by santiaguilera@theamalgama.com on 01/03/16.
 */
public interface EventListener {

    /**
     * Dispatches the event to all the ones observing
     * @param event
     */
    void dispatchEvent(@NonNull Event event);

}
