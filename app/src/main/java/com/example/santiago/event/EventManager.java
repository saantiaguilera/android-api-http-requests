package com.example.santiago.event;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;

import com.example.santiago.event.listener.EventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Class in charge of receiving events (from listeners), manage them and/or broadcast them to the other listeners.
 *
 * @note Thread-safe.
 *
 * Created by santiaguilera@theamalgama.com on 01/03/16.
 */
public class EventManager implements EventListener {

    private WeakReference<ContextWrapper> context = null;
    private WeakReference<Object> tag = null;

    private EventDispatcher dispatcher; //Dude in charge of dispatching events

    private final List<Object> observables = new ArrayList<>(); //List of all the objects willing to receive events

    public EventManager(@NonNull ContextWrapper context, @NonNull Object tag){
        this.context = new WeakReference<>(context);
        this.tag = new WeakReference<>(tag);

        dispatcher = new EventDispatcher();
    }

    public @NonNull Context getContext() {
        return context.get();
    }

    public @NonNull Object getTag() { return tag.get(); }

    /**
     * Adds an instance to the list of all the
     * classes that will be notified in the income of an event
     * @param observable
     */
    public void addObservable(@NonNull Object observable){
        synchronized (observables) {
            observables.add(observable);
        }
    }

    /**
     * Removes an instance to the list of all the classes that will be notified in the income of an event
     * @param observable
     * @return if it was successfully removed
     */
    public boolean removeObservable(@NonNull Object observable) {
        synchronized (observables) {
            return observables.remove(observable);
        }
    }

    /**
     * Method called (by one sending an event) for the purpose of managing that event, and/or else broadcast it to the
     * list of classes observing
     *
     * @note Since we could be adding or rm observables from other threads, we create a copy of the current observables
     * and iterate over them.
     *
     * @param event
     */
    @Override
    public void dispatchEvent(@NonNull Event event) {
        //Set my own hashcode as "his parent" in case you need to know which EM called him
        event.setParentHashCode(tag.get().hashCode());

        //Dispatch the event to ourselves
        dispatcher.dispatchEvent(event, this);

        List<Object> observablesCopy;
        synchronized (observables) {
            observablesCopy = new ArrayList<>(observables);
        }

        //Iterate through all the objects listening and dispatch it too
        for(Object object : observablesCopy)
            if(object != null)
                dispatcher.dispatchEvent(event, object);
    }

}
