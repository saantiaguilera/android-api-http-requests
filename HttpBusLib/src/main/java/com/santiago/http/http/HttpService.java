package com.santiago.http.http;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.santiago.event.EventBus;
import com.santiago.event.anotation.EventAsync;
import com.santiago.event.anotation.EventMethod;
import com.santiago.http.event.HttpAuthenticatorEvent;
import com.santiago.http.event.HttpCacheEvent;
import com.santiago.http.event.HttpCancelAllRequestsEvent;
import com.santiago.http.event.HttpCancelRequestEvent;
import com.santiago.http.event.HttpClientEvent;
import com.santiago.http.event.HttpCookieEvent;
import com.santiago.http.event.HttpDispatcherEvent;
import com.santiago.http.event.HttpInterceptorEvent;
import com.santiago.http.event.HttpRequestEvent;
import com.santiago.http.event.HttpStickyHeadersEvent;
import com.santiago.http.event.HttpTimeoutsEvent;
import com.santiago.loader.HttpBus;

import java.io.IOException;
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
 * Created by saantiaguilera on 13/05/16.
 */
public class HttpService extends Service {

    //Rest client. Mutable
    private volatile @NonNull OkHttpClient restClient = new OkHttpClient();
    //Those headers that should always appear
    private final @NonNull Map<String, String> stickyHeaders = new ConcurrentHashMap<>();
    //Map for storing the pending requests (So we can cancel them if needed)
    private final @NonNull Map<HttpRequestEvent, Call> pendingRequests = new ConcurrentHashMap<>();
    //A lock?
    private final @NonNull Object lock = new Object();
    //IBinder instance so that binders can use us
    private final @NonNull IBinder serviceBinder = new HttpBinder();
    //Handler for posting in the main thread
    private final Handler resultsDispatcher = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        //Avoid duplicates
        HttpBus.getInstance().removeObservable(this);
        HttpBus.getInstance().addObservable(this);
    }

    /**
     * Using START_STICKY because we want it to live forever
     * (WHO WANTS TO LIVE FOREVER?)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Return a HttpBinder instance so that they can know our instance running :)
     *
     * @param intent intent from the start
     * @return HttpBinder instance
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    /**
     * Async method from where the requests are done
     * @param event the request event
     */
    @SuppressWarnings({"unused", "unchecked"})
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
                request.post(validateBodyNonNull(body));
                break;
            case PUT:
                request.put(validateBodyNonNull(body));
                break;
            case PATCH:
                request.patch(validateBodyNonNull(body));
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

        //Is this lock really needed ?
        //synchronized (lock) {
            //Check if the event holds a singular OkHttpClient.
            OkHttpClient client = event.overrideClient(restClient.newBuilder());
            if (client == null) //Else use the default
                client = restClient;

            Call call = client.newCall(request.build());

            pendingRequests.put(event, call);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //If its cancelled dont post anything, since the user has to cancel it, so he knows.
                    if (!call.isCanceled())
                        post(event, e);

                    pendingRequests.remove(event);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        //Note that the parseResponse is still in the worker thread. But not the success
                        if (!call.isCanceled())
                            post(event, event.parseResponse(response));
                    } catch (HttpParseException e) {
                        post(event, e);
                    } finally {
                        pendingRequests.remove(event);
                    }
                }
            });
        //}
    }

    /**
     * Post a failure response.
     * This doesnt dispatch events. It just calls the failure method redefined in the event
     * to let the user do what he pleases
     *
     * @param event request that failed
     * @param exception exception that was thrown
     */
    private void post(final HttpRequestEvent event, final Exception exception) {
        resultsDispatcher.post(new Runnable() {
            @Override
            public void run() {
                event.onHttpRequestFailure(exception);
            }
        });
    }

    /**
     * Post a successfull response
     * This doesnt dispatch events. It just calls the success method redefined in the event
     * to let the user do what he pleases
     * @param event request that succeed
     * @param object that was parsed as response
     */
    @SuppressWarnings("unchecked")
    private void post(final HttpRequestEvent event, final Object object) {
        resultsDispatcher.post(new Runnable() {
            @Override
            public void run() {
                event.onHttpRequestSuccess(object);
            }
        });
    }

    /**
     * Cancel a pending request if exists
     * @param event the cancel event
     */
    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCancelRequestEvent.class)
    private void onCancelRequestEvent(@NonNull HttpCancelRequestEvent event) {
        Call request = pendingRequests.get(event.getCancelEvent());

        if (request != null && !request.isCanceled())
            request.cancel();
    }

    /**
     * Cancel all pending requests existing
     */
    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCancelAllRequestsEvent.class)
    private void onCancelAllRequestsEvent() {
        for (Call call : pendingRequests.values())
            call.cancel();

        pendingRequests.clear();
        //Since a single event can override the client, theres no guarantee that it will only be a single dispatcher across all requests
//        restClient.dispatcher().cancelAll();
    }

    /**
     * Checks that the body isnt null, else notifies
     * @param body body of a request
     */
    private @NonNull RequestBody validateBodyNonNull(@Nullable RequestBody body) {
        if (body == null) {
            Log.w(HttpService.class.getSimpleName(), "Theres a request without body that should have it. Performing request with empty body.");
            return RequestBody.create(null, new byte[0]);
        }

        return body;
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
    private void newStickyHeaders(@NonNull HttpStickyHeadersEvent event) {
        for (String deleteKey : event.getRemovedKeys())
            stickyHeaders.remove(deleteKey);

        stickyHeaders.putAll(event.getEntries());
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpDispatcherEvent.class)
    private void newDispatcherEvent(@NonNull HttpDispatcherEvent event) {
        synchronized (lock) {
            restClient = restClient.newBuilder().dispatcher(event.getDispatcher()).build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpTimeoutsEvent.class)
    private void newTimeoutsEvent(@NonNull HttpTimeoutsEvent event) {
        synchronized (lock) {
            restClient = restClient.newBuilder()
                            .connectTimeout(event.getConnectionTimeout(), TimeUnit.SECONDS)
                            .readTimeout(event.getReadTimeout(), TimeUnit.SECONDS)
                            .writeTimeout(event.getWriteTimeout(), TimeUnit.SECONDS).build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCookieEvent.class)
    private void newCookieEvent(@NonNull HttpCookieEvent event) {
        synchronized (lock) {
            restClient = restClient.newBuilder().cookieJar(event.getCookies()).build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpCacheEvent.class)
    private void newCacheEvent(@NonNull HttpCacheEvent event) {
        synchronized (lock) {
            restClient = restClient.newBuilder().cache(event.getCache()).build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpInterceptorEvent.class)
    private void newInterceptorEvent(@NonNull HttpInterceptorEvent event) {
        synchronized (lock) {
            OkHttpClient.Builder builder = restClient.newBuilder();

            if (event.isNetwork())
                builder.addNetworkInterceptor(event.getInterceptor());
            else builder.addInterceptor(event.getInterceptor());

            restClient = builder.build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpAuthenticatorEvent.class)
    private void newAuthenticatorEvent(@NonNull HttpAuthenticatorEvent event) {
        synchronized (lock) {
            restClient = restClient.newBuilder().authenticator(event.getAuthenticator()).build();
        }
    }

    @SuppressWarnings("unused")
    @EventAsync
    @EventMethod(HttpClientEvent.class)
    private void newClientEvent(@NonNull HttpClientEvent event) {
        synchronized (lock) {
            restClient = event.getOkHttpClient();
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
