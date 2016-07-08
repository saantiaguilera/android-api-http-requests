package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

import okhttp3.CookieJar;

/**
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpCookieEvent extends Event {

    private @NonNull
    CookieJar cookies;

    public HttpCookieEvent(@NonNull CookieJar cookies) {
        this.cookies = cookies;
    }

    public @NonNull
    CookieJar getCookies() { return cookies; }

}
