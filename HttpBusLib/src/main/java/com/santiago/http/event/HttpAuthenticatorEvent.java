package com.santiago.http.event;

import android.support.annotation.NonNull;

import com.santiago.event.Event;

import okhttp3.Authenticator;

/**
 * Event for attaching an Authenticator to the client
 * Created by saantiaguilera on 06/07/16.
 */
public class HttpAuthenticatorEvent extends Event {

    private @NonNull Authenticator authenticator;

    public HttpAuthenticatorEvent(@NonNull Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public @NonNull Authenticator getAuthenticator() { return authenticator; }

}
