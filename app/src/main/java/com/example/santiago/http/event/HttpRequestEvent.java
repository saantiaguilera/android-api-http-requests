package com.example.santiago.http.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.santiago.event.Event;
import com.example.santiago.http.http.HttpMethod;
import com.example.santiago.http.http.HttpParseException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class for requests.
 * Currently, requests are defined inside subclasses of HttpRequestEvent, this means each
 * request you will have you will define it inside one of these.
 *
 * Created by saantiaguilera on 13/05/16.
 */
public abstract class HttpRequestEvent<E> extends Event {

    /**
     *
     * This method runs on a background thread
     *
     * Endpoint of the REST request
     * @return endpoint of the request
     */
    public abstract @NonNull String getUrl();

    /**
     *
     * This method runs on a background thread
     *
     * REST HttpMethod
     * @return method
     */
    public abstract @NonNull HttpMethod getHttpMethod();

    /**
     *
     * This method runs on a background thread
     *
     * Body for the request. If the method needs them you MUST override it else it will crash
     * @return RequestBody with the params or empty one if none
     */
    public @Nullable RequestBody getBody() {
        return null;
    }

    /**
     *
     * This method runs on a background thread
     *
     * Headers for the request. Bear in mind that you should not include the sticky ones
     * if the service has.
     * @return headers
     */
    public @Nullable Headers getHeaders() {
        return null;
    }

    /**
     * Method for single client per call mode (if you want this request to be done with specific
     * client options (lets say different cache from the default or timeouts or whatever) override this
     * method, change in the builder to your needs (the builder holds the default client data) and
     * return it.
     *
     * @Note this will not override the default client used, so its just for this single request. if you plan
     * on doing a lot of requests with a particular modified client, then use the default or hold the dispatchers
     * and stuff apart, inherit this class and modify them every time with the persisted pojos
     *
     * @param builder of the default okhttpclient
     * @return okhttpclient to be used
     */
    public @Nullable OkHttpClient overrideClient(OkHttpClient.Builder builder) {
        return null;
    }

    /**
     * Implement it and parse the response you will receive to form whatever you expect this
     * request to return
     *
     * This method runs on a background thread
     *
     * @param response . use its body() to fetch the data. Remember its a buffer so you will
     *                 flush it (meaning you can only do this once per request)
     * @return Object expected to receive from the request
     * @throws HttpParseException in case there was a malformed response
     */
    public abstract E parseResponse(@NonNull Response response) throws HttpParseException;

    /**
     * If the requests fails, it will execute this method.
     * Override it if you want your request to have a defined logic for when the request fails
     *
     * This method runs on the main thread
     *
     * @param exception exception thrown
     */
    public void onHttpRequestFailure(@NonNull Exception exception) {}

    /**
     * If the requests succeeds, it will execute this method.
     * Override it if you want your request to have a defined logic for when the request is OK
     *
     * This method runs on the main thread
     *
     * @param result the result?
     */
    public void onHttpRequestSuccess(E result) {}


}
