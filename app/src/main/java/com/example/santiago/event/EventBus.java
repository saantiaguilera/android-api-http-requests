package com.example.santiago.event;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.santiago.http.http.HttpService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class in charge of receiving events (from listeners), manage them and/or broadcast them to the other listeners.
 *
 * @note Thread-safe.
 *
 * Created by santiaguilera@theamalgama.com on 01/03/16.
 */
public class EventBus {

    private WeakReference<ContextWrapper> context = null;

    private EventDispatcher dispatcher; //Dude in charge of dispatching events

    private static EventBus httpBus;

    private final List<WeakReference<Object>> observables = new ArrayList<>(); //List of all the objects willing to receive events
    private final List<Event> stickyEvents = new ArrayList<>();

    /**
     * This shouldnt be called by anyone except us on the start of the app
     * @param context
     * @return bus
     */
    public static EventBus _initHttpBus(@NonNull ContextWrapper context) {
        httpBus = new EventBus(context);

        Intent serviceIntent = new Intent(context, HttpService.class);
        context.startService(serviceIntent);

        return httpBus;
    }

    public static EventBus getHttpBus() {
        if (httpBus == null)
            throw new IllegalStateException("Trying to get HttpBus without init. Be sure you are calling initHttpBus first. A good practice would be in a Application or a ContentProvider :)");

        return httpBus;
    }

    public EventBus(@NonNull ContextWrapper context){
        this.context = new WeakReference<>(context);

        dispatcher = new EventDispatcher();
    }

    public @NonNull Context getContext() {
        return context.get();
    }

    /**
     * Adds an instance to the list of all the
     * classes that will be notified in the income of an event
     * @param observable
     */
    public void addObservable(@NonNull Object observable){
        synchronized (observables) {
            observables.add(new WeakReference<>(observable));
        }

        dispatchStickies(observable);
    }

    /**
     * Removes an instance to the list of all the classes that will be notified in the income of an event
     * @param observable
     */
    public void removeObservable(@NonNull Object observable) {
        synchronized (observables) {
            for (Iterator<WeakReference<Object>> iterator = observables.iterator(); iterator.hasNext(); ) {
                WeakReference<Object> weakRef = iterator.next();
                if (weakRef.get() == observable || weakRef.get() == null) {
                    iterator.remove();
                }
            }
        }
    }

    public void clear() {
        synchronized (observables) {
            observables.clear();
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
    public void dispatchEvent(@NonNull Event event) {
        //Dispatch the event to ourselves
        dispatcher.dispatchEvent(event, this);

        List<WeakReference<Object>> observablesCopy;
        synchronized (observables) {
            observablesCopy = new ArrayList<>(observables);
        }

        boolean shouldCleanReferences = false;

        //Iterate through all the objects listening and dispatch it too
        for(WeakReference<Object> observable : observablesCopy)
            if(observable != null)
                dispatcher.dispatchEvent(event, observable.get());
            else shouldCleanReferences = true;

        if (shouldCleanReferences) {
            synchronized (observables) {
                for (Iterator<WeakReference<Object>> iterator = observables.iterator(); iterator.hasNext(); ) {
                    WeakReference<Object> weakRef = iterator.next();
                    if (weakRef.get() == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void dispatchEventSticky(@NonNull Event event) {
        stickyEvents.add(event);
        dispatchEvent(event);
    }

    public boolean removeEventSticky(@NonNull Event event) {
        return stickyEvents.remove(event);
    }

    private void dispatchStickies(Object observable) {
        for (Event event : stickyEvents)
            dispatcher.dispatchEvent(event, observable);
    }

}
