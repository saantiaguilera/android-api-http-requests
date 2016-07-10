package com.santiago.loader;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.santiago.event.EventBus;
import com.santiago.http.http.HttpService;

/**
 * Created by santi on 10/07/16.
 */
public final class HttpBus {

    private static EventBus INSTANCE;

    /**
     * This shouldnt be called by anyone except us on the start of the app
     * Ideally try to call it in the application onCreate. So the context is the applicationContext :)
     *
     * @param context
     * @return bus
     */
    static @NonNull EventBus _initHttpBus(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (EventBus.class) {
                if (INSTANCE == null) INSTANCE = new EventBus(context);
            }
        }

        Intent serviceIntent = new Intent(context, HttpService.class);
        context.startService(serviceIntent);

        return INSTANCE;
    }

    /**
     * Getter for the Http bus. Its a singleton so all responses and requests are done over this the same bus
     * @return http bus
     */
    public static @NonNull EventBus getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Trying to get HttpBus without init. Be sure you are calling initHttpBus first. A good practice would be in a Application or a ContentProvider :)");

        return INSTANCE;
    }

}
