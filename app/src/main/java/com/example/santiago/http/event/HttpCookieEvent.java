package com.example.santiago.http.event;

import com.example.santiago.event.Event;

import okhttp3.CookieJar;

/**
 * Created by santi on 06/07/16.
 */
public class HttpCookieEvent extends Event {

    private CookieJar cookies;

    public HttpCookieEvent(CookieJar cookies) {
        this.cookies = cookies;
    }

    public CookieJar getCookies() { return cookies; }

}
