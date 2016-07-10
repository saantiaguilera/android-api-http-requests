package com.santiago.event;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.santiago.http.http.HttpService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class in charge of receiving events (from listeners), manage them and/or broadcast them to the other listeners.
 *
 * @note Thread-safe.
 *
 * Created by saantiaguilera on 01/03/16.
 */
public class EventBus {

    //Context asociated with this bus
    private WeakReference<Context> context = null;
    //Singleton for the http bus
    private static EventBus httpBus = null;
    //Dude in charge of dispatching events
    private final @NonNull EventDispatcher dispatcher = new EventDispatcher();
    //List of all the objects willing to receive events
    private final @NonNull List<WeakReference<Object>> observables = new ArrayList<>();
    //List of all the events that are sticky (are sent to even new observables)
    private final @NonNull List<Event> stickyEvents = new ArrayList<>();

    /**
     * This shouldnt be called by anyone except us on the start of the app
     * Ideally try to call it in the application onCreate. So the context is the applicationContext :)
     *
     * @param context
     * @return bus
     */
    public static @NonNull EventBus _initHttpBus(@NonNull Context context) {
        if (httpBus == null) {
            synchronized (EventBus.class) {
                if (httpBus == null) httpBus = new EventBus(context);
            }
        }

        Intent serviceIntent = new Intent(context, HttpService.class);
        context.startService(serviceIntent);

        return httpBus;
    }

    /**
     * Getter for the Http bus. Its a singleton so all responses and requests are done over this the same bus
     * @return http bus
     */
    public static @NonNull EventBus getHttpBus() {
        if (httpBus == null)
            throw new IllegalStateException("Trying to get HttpBus without init. Be sure you are calling initHttpBus first. A good practice would be in a Application or a ContentProvider :)");

        return httpBus;
    }

    /**
     * Constructor for an event bus.
     * @param context
     */
    public EventBus(@NonNull Context context){
        this.context = new WeakReference<>(context);
    }

    /**
     * Getter for the context asociated with the event bus
     * @return context
     */
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

    /**
     * Clear the observables list
     */
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
        List<WeakReference<Object>> observablesCopy;
        synchronized (observables) {
            observablesCopy = new ArrayList<>(observables);
        }

        boolean shouldCleanReferences = false;

        //Iterate through all the objects listening and dispatch it too
        for(WeakReference<Object> observable : observablesCopy)
            if(observable.get() != null)
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

    /**
     * Dispatches event as sticky. This means that every one that suscribes after this event will
     * also receive it (if listening to it)
     * @param event to dispatch
     */
    public void dispatchEventSticky(@NonNull Event event) {
        stickyEvents.add(event);
        dispatchEvent(event);
    }

    /**
     * Dispatches an event but after a time has passed
     *
     * @param event to dispatch
     * @param millisInFuture time in millis to wait for dispatching the event
     */
    public void dispatchEventDelayed(final @NonNull Event event, long millisInFuture) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchEvent(event);
            }
        }, millisInFuture);
    }

    /**
     * Dispatches an event as sticky but after a time has passed.
     *
     * @Note it Will become sticky after the time has passed too. This wont create it as sticky
     * at this moment and later dispatch it beware.
     *
     * @param event to dispatch
     * @param millisInFuture time in millis to wait for dispatching the event
     */
    public void dispatchEventStickyDelayed(final @NonNull Event event, long millisInFuture) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchEventSticky(event);
            }
        }, millisInFuture);
    }

    /**
     * Removes an event from the sticky list
     * @param event
     * @return boolean if removed
     */
    public boolean removeEventSticky(@NonNull Event event) {
        return stickyEvents.remove(event);
    }

    /**
     * Dispatches all the available sticky events to a new observable that was just added
     * @param observable
     */
    private void dispatchStickies(@NonNull Object observable) {
        for (Event event : stickyEvents)
            dispatcher.dispatchEvent(event, observable);
    }

}