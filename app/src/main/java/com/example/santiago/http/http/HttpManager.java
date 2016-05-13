package com.example.santiago.http.http;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.example.santiago.event.EventManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Singleton for managing http stuff
 * Everytime your context will change, this applies to:
 *  - New activity using it
 *  - Configuration changes on an Activity (it applies to the first so nvm)
 *  - Service using it
 * You should use HttpManager.with() . If your ctx can change during runtime in the middle
 * of an activity, then always use this.
 * Else, since its cheaper you can use getInstance (knowing your context is still the same)
 *
 * Created by santiago on 13/05/16.
 */
public class HttpManager {

    //Singleton instance
    private static HttpManager instance;

    //Service and a flag for knowing if we are bound or not
    private @NonNull HttpService httpService;
    private boolean bound = false;

    //Lists to handle the eventmanagers that are currently listening
    private @NonNull List<EventManager> allTimeEMs = new ArrayList<>();
    private @NonNull Queue<EventManager> emQueue = new ArrayDeque<>();

    /**
     * Getter for the HttpManager singleton.
     *
     * @return HttpManager singleton
     */
    public static @NonNull HttpManager getInstance() {
        if (instance == null)
            instance = new HttpManager();

        return instance;
    }

    /**
     * Checks if our HttpService is running or not
     * @param context
     * @return boolean
     */
    private boolean isServiceRunning(@NonNull ContextWrapper context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (HttpService.class.getName().equals(service.service.getClassName()))
                return true;

        return false;
    }

    /**
     * @ALWAYS
     * Call it everytime your activity cycle goes to onStart() and you are using the
     * HttpManager
     *
     * Will bind to the server so he starts listening to us.
     * If you dont call this, it wont work :)
     */
    public void onStart(@NonNull ContextWrapper contextWrapper) {
        Intent intent = new Intent(contextWrapper, HttpService.class);

        if (!isServiceRunning(contextWrapper))
            contextWrapper.startService(intent);

        contextWrapper.bindService(intent, mConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    /**
     * @ALWAYS
     * Call it everytime your activity cycle goes to onStop() and you are using the
     * HttpManager
     *
     * Will unbind from the service and remove all our eventmanagers so leaks are less
     * probables (although activities and stuff are all weakreferenced, but still)
     */
    public void onStop(@NonNull ContextWrapper contextWrapper) {
        if (bound) {
            for (EventManager em : allTimeEMs)
                if (em != null) httpService.remove(em);

            allTimeEMs.clear();

            contextWrapper.unbindService(mConnection);
            bound = false;
        }
    }

    /**
     * Add the eventmanager to the service
     * @param eventManager
     */
    public void addEventManager(@NonNull EventManager eventManager) {
        if (bound) {
            httpService.add(eventManager);
            allTimeEMs.add(eventManager);
        } else emQueue.add(eventManager);
    }

    /**
     * Remove the EventManager from the service
     * @param eventManager
     */
    public void removeEventManager(@NonNull EventManager eventManager) {
        if (bound) {
            httpService.remove(eventManager);
            allTimeEMs.remove(eventManager);
        } else emQueue.remove(eventManager);
    }

    private @NonNull ServiceConnection mConnection = new ServiceConnection() {

        /**
         * Get the service instance and add all our eventManager that tried to
         * while it wasnt connected.
         *
         * @param className
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HttpService.HttpBinder binder = (HttpService.HttpBinder) service;
            httpService = binder.getService();
            bound = true;

            for (EventManager eventManager : emQueue)
                httpService.add(eventManager);

            allTimeEMs.addAll(emQueue);
            emQueue.clear();
        }

        /**
         * If this happens during runtime we are fucked up
         * https://www.youtube.com/watch?v=dQF0GCzlA8Y
         * Doc says will only happen if process is killed so we shouldnt care about it
         * @param arg0
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }

    };
    
}
