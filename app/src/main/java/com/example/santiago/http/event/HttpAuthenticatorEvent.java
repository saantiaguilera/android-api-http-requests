package com.example.santiago.http.event;

import android.support.annotation.NonNull;

import com.example.santiago.event.Event;

import okhttp3.Authenticator;

/**
 * Created by santi on 06/07/16.
 */
public class HttpAuthenticatorEvent extends Event {

    private @NonNull
    Authenticator authenticator;

    public HttpAuthenticatorEvent(@NonNull Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public @NonNull Authenticator getAuthenticator() { return authenticator; }

}
