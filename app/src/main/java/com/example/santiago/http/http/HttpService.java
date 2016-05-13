package com.example.santiago.http.http;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.santiago.event.EventManager;
import com.example.santiago.event.anotation.EventAsync;
import com.example.santiago.event.anotation.EventMethod;
import com.example.santiago.http.event.RequestEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing network requests outside of the activities stuff
 * This way we can survive configuration changes and lifecycles that shouldnt condition a
 * network connection + we avoid memory leaks.
 *
 * Created by santiago on 13/05/16.
 */
public class HttpService extends Service {

    //Map were we store all the possible dispatchers
    private Map<Integer, EventManager> dispatchers = new HashMap<>();

    //IBinder instance so that binders can use us
    private final IBinder serviceBinder = new HttpBinder();

    /**
     * Using START_STICKY because we want it to live forever
     * (WHO WANTS TO LIVE FOREVER?)
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Return a HttpBinder instance so that they can know our instance running :)
     *
     * @param intent
     * @return HttpBinder instance
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    /**
     * Add an EventManager to our dispatchers list.
     *
     * You should add it so that the request is able to dispatch methods later after
     * the request has been done.
     *
     * @Note If you dont the request wont be made at all.
     *
     * @param eventManager
     */
    public void add(EventManager eventManager) {
        dispatchers.put(eventManager.getTag().hashCode(), eventManager);

        eventManager.addObservable(this);
    }

    /**
     * Remove an EventManager to our dispatchers list.
     *
     * Removing an eventmanager makes requests done via him not executable anymore.
     *
     * @note You should remove it so that EM and everything he has can be gced (its threads/ctxs)
     * Although they are weak references so idk if memory leaks can happen.
     *
     * @param eventManager
     */
    public void remove(EventManager eventManager) {
        dispatchers.remove(eventManager.getTag().hashCode());

        eventManager.removeObservable(this);
    }

    /**
     * Async method from where the requests are done
     *
     * Uses double dispatch so the service from here onwards doesnt know what happens with the
     * request.
     *
     * @param event
     */
    @EventAsync
    @EventMethod(RequestEvent.class)
    private void onRequestEvent(RequestEvent event) {
        EventManager dispatcher = dispatchers.get(event.getParentHashCode());

        if (dispatcher != null)
            event.execute(dispatcher);
    }

    /**
     * Binder subclass for giving our instance to the bound ones.
     */
    public class HttpBinder extends Binder {

        public HttpService getService() {
            return HttpService.this;
        }

    }

}
