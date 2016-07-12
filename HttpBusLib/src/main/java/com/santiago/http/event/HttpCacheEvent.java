package com.santiago.http.event;

import android.support.annotation.NonNull;

import com.santiago.event.Event;

import okhttp3.Cache;

/**
 * Event for setting a cache
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpCacheEvent extends Event {

    private @NonNull Cache cache;

    public HttpCacheEvent(@NonNull Cache cache) {
        this.cache = cache;
    }

    public @NonNull Cache getCache() { return cache; }

}
