package com.example.santiago.http.http;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import com.example.santiago.event.EventBus;
import com.example.santiago.event.anotation.EventAsync;
import com.example.santiago.event.anotation.EventMethod;
import com.example.santiago.http.event.HttpAuthenticatorEvent;
import com.example.santiago.http.event.HttpCacheEvent;
import com.example.santiago.http.event.HttpCancelRequestEvent;
import com.example.santiago.http.event.HttpCookieEvent;
import com.example.santiago.http.event.HttpDispatcherEvent;
import com.example.santiago.http.event.HttpInterceptorEvent;
import com.example.santiago.http.event.HttpStickyHeadersEvent;
import com.example.santiago.http.event.HttpTimeoutsEvent;
import com.example.santiago.http.event.HttpRequestEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service for managing network requests outside of the activities stuff
 * This way we can survive configuration changes and lifecycles that shouldnt condition a
 * network connection + we avoid memory leaks.
 *
 * Created by santiago on 13/05/16.
 */
public class HttpService extends Service {

    //Rest client. Mutable
    private OkHttpClient restClient = new OkHttpClient();
    //Those headers that should always appear
    private final Map<String, String> stickyHeaders = new ConcurrentHashMap<>();
    //Map for storing the pending requests (So we can cancel them if needed)
    private final Map<HttpRequestEvent, Call> pendingRequests = new ConcurrentHashMap<>();
    //A lock?
    private final Object lock = new Object();
    //IBinder instance so that binders can use us
    private final @NonNull IBinder serviceBinder = new HttpBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        //Avoid duplicates
        EventBus.getHttpBus().removeObservable(this);
        EventBus.getHttpBus().addObservable(this);
    }

    /**
     * Using START_STICKY because we want it to live forever
     * (WHO WANTS TO LIVE FOREVER?)
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Return a HttpBinder instance so that they can know our instance running :)
     *
     * @param intent
     * @return HttpBinder instance
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    /**
     * Async method from where the requests are done
     *
     * @param event
     */
    @SuppressWarnings("unused")
    @RequiresPermission(Manifest.permission.INTERNET)
    @EventAsync //Because maybe theres heavy shit in the event. Like loading a whole image
    @EventMethod(HttpRequestEvent.class)
    private void onRequestEvent(@NonNull final HttpRequestEvent event) {
        Request.Builder request = new Request.Builder()
                .url(event.getUrl());

        RequestBody body = event.getBody();
        switch (event.getHttpMethod()) {
            case GET:
                request.get();
                break;
            case DELETE:
                if (body == null)
                    request.delete();
                else request.delete(body);
                break;
            case POST:
                validateBodyNonNull(body);
                request.post(event.getBody());
                break;
            case PUT:
                validateBodyNonNull(body);
                request.put(event.getBody());
                break;
            case PATCH:
                validateBodyNonNull(body);
                request.patch(event.getBody());
                break;
            case HEAD:
                request.head();
                break;
        }

        Headers headers = event.getHeaders();
        if (headers != null)
            request.headers(event.getHeaders());

        for (Map.Entry<String, String> kv : stickyHeaders.entrySet())
            request.addHeader(kv.getKey(), kv.getValue());

        request.tag(event);

        synchronized (lock) {
            Call call = restClient.newCall(request.build());

            pendingRequests.put(event, call);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    event.onHttpRequestFailure(e);
                    pendingRequests.remove(event);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        event.onHttpRequestSuccess(event.parseResponse(response));
                    } catch (HttpParseException e) {
                        event.onHttpRequestFailure(e);
                    } finally {
                        pendingRequests.remove(event);
                    }
                }
            });
        }
    }

    /**
     * Cancel a pending request if exists
     * @param event
     */
    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCancelRequestEvent.class)
    private void onCancelRequestEvent(HttpCancelRequestEvent event) {
        Call request = pendingRequests.get(event);

        if (request != null && !request.isCanceled())
            request.cancel();
    }

    /**
     * Checks that the body isnt null, else notifies
     * @param body
     */
    private void validateBodyNonNull(RequestBody body) {
        if (body == null) throw new NullPointerException("Body is null in a Request that needs it");
    }

    /*---------------CONFIGS---------------*/

    /**
     * Theres no configuration for the following ones because they shouldnt be changed for common reasons:
     * - Follow redirects (its handled by default the 300, and it should be)
     * - Retry on fail (Derp, never. Poor users)
     *
     * Also there are some I didnt implement, if you need them just shout out me in github and Ill do them :)
     */

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpStickyHeadersEvent.class)
    private void newStickyHeaders(HttpStickyHeadersEvent event) {
        for (String deleteKey : event.getRemovedKeys())
            stickyHeaders.remove(deleteKey);

        stickyHeaders.putAll(event.getEntries());
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpDispatcherEvent.class)
    private void newDispatcherEvent(HttpDispatcherEvent event) {
        OkHttpClient.Builder builder = restClient.newBuilder().dispatcher(event.getDispatcher());

        synchronized (lock) {
            restClient = builder.build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpTimeoutsEvent.class)
    private void newTimeoutsEvent(HttpTimeoutsEvent event) {
        OkHttpClient.Builder builder = restClient.newBuilder();
        builder.connectTimeout(event.getConnectionTimeout(), TimeUnit.SECONDS);
        builder.readTimeout(event.getReadTimeout(), TimeUnit.SECONDS);
        builder.writeTimeout(event.getWriteTimeout(), TimeUnit.SECONDS);

        synchronized (lock) {
            restClient = builder.build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCookieEvent.class)
    private void newCookieEvent(HttpCookieEvent event) {
        OkHttpClient.Builder builder = restClient.newBuilder().cookieJar(event.getCookies());

        synchronized (lock) {
            restClient = builder.build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCacheEvent.class)
    private void newCacheEvent(HttpCacheEvent event) {
        OkHttpClient.Builder builder = restClient.newBuilder().cache(event.getCache());

        synchronized (lock) {
            restClient = builder.build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpInterceptorEvent.class)
    private void newInterceptorEvent(HttpInterceptorEvent event) {
        OkHttpClient.Builder builder = restClient.newBuilder();
        if (event.isNetwork())
            builder.addNetworkInterceptor(event.getInterceptor());
        else builder.addInterceptor(event.getInterceptor());

        synchronized (lock) {
            restClient = builder.build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpAuthenticatorEvent.class)
    private void newAuthenticatorEvent(HttpAuthenticatorEvent event) {
        OkHttpClient.Builder builder = restClient.newBuilder().authenticator(event.getAuthenticator());

        synchronized (lock) {
            restClient = builder.build();
        }
    }

    /**
     * Binder subclass for giving our instance to the bound ones.
     */
    public class HttpBinder extends Binder {

        public @NonNull HttpService getService() {
            return HttpService.this;
        }

    }

}
