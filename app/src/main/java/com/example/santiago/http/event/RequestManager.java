package com.example.santiago.http.event;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.santiago.event.EventManager;
import com.example.santiago.http.service.HttpService;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by santiago on 13/05/16.
 */
public class RequestManager {

    private static RequestManager instance;

    private HttpService httpService;
    private boolean bound = false;

    private List<EventManager> allTimeEMs = new ArrayList<>();
    private Queue<EventManager> emQueue = new ArrayDeque<>();
    
    private WeakReference<ContextWrapper> context;
    
    public static RequestManager with(ContextWrapper context) {
        if (instance == null) {
            instance = new RequestManager();

            Intent intent = new Intent(context, HttpService.class);
            context.startService(intent); //This should only be done at start in a splash eg
        }

        instance.context = new WeakReference<>(context);

        return instance;
    }

    public static RequestManager getInstance() {
        return instance;
    }
    
    public void onStart() {
        Intent intent = new Intent(context.get(), HttpService.class);
        context.get().bindService(intent, mConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    public void onStop() {
        if (bound) {
            for (EventManager em : allTimeEMs)
                if (em != null) httpService.remove(em);

            allTimeEMs.clear();

            context.get().unbindService(mConnection);
            bound = false;
        }
    }

    public void addEventManager(EventManager eventManager) {
        if (bound) {
            httpService.attach(eventManager);
            allTimeEMs.add(eventManager);
        } else emQueue.add(eventManager);
    }

    public void removeEventManager(EventManager eventManager) {
        if (bound) {
            httpService.remove(eventManager);
            allTimeEMs.remove(eventManager);
        } else emQueue.remove(eventManager);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HttpService.HttpBinder binder = (HttpService.HttpBinder) service;
            httpService = binder.getService();
            bound = true;

            for (EventManager eventManager : emQueue)
                httpService.attach(eventManager);

            allTimeEMs.addAll(emQueue);
            emQueue.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }

    };
    
}
