package com.example.santiago.testings;

import android.app.Application;

import com.example.santiago.event.EventBus;

/**
 * Created by santi on 06/07/16.
 */
public class TestingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus._initHttpBus(this);
    }

}
