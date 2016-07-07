package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

import okhttp3.Cache;

/**
 * Created by santi on 06/07/16.
 */
public class HttpCacheEvent extends Event {

    private @NonNull Cache cache;

    public HttpCacheEvent(@NonNull Cache cache) {
        this.cache = cache;
    }

    public @NonNull Cache getCache() { return cache; }

}
