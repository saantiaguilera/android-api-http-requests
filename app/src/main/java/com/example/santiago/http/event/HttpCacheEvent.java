package com.example.santiago.http.event;

import com.example.santiago.event.Event;

import okhttp3.Cache;

/**
 * Created by santi on 06/07/16.
 */
public class HttpCacheEvent extends Event {

    private Cache cache;

    public HttpCacheEvent(Cache cache) {
        this.cache = cache;
    }

    public Cache getCache() { return cache; }

}
