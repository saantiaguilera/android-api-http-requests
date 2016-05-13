package com.example.santiago.http.service;

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
 * Created by santiago on 13/05/16.
 */
public class HttpService extends Service {

    private Map<Integer, EventManager> dispatchers = new HashMap<>();

    private final IBinder serviceBinder = new HttpBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public void attach(EventManager eventManager) {
        dispatchers.put(eventManager.hashCode(), eventManager);

        eventManager.addObservable(this);
    }

    public void remove(EventManager eventManager) {
        dispatchers.remove(eventManager.hashCode());

        eventManager.removeObservable(this);
    }

    @EventAsync
    @EventMethod(RequestEvent.class)
    private void onRequestEvent(RequestEvent event) {
        EventManager dispatcher = dispatchers.get(event.parentHashCode());

        if (dispatcher != null)
            event.execute(dispatcher);
    }

    public class HttpBinder extends Binder {

        public HttpService getService() {
            return HttpService.this;
        }

    }

}
