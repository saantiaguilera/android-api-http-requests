package com.example.santiago.http.event;

import com.example.santiago.event.Event;

import okhttp3.Authenticator;

/**
 * Created by santi on 06/07/16.
 */
public class HttpAuthenticatorEvent extends Event {

    private Authenticator authenticator;

    public HttpAuthenticatorEvent(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public Authenticator getAuthenticator() { return authenticator; }

}
