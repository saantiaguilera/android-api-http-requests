package com.example.santiago.http_requests.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import okio.Buffer;

public abstract class AmalgamaHttpRequest<E> extends OkHttpAsyncParsedRequest<E> {
	
	private Context context = null;
	
	private HttpRequestSuccessListener<E> successListener = null;
    private HttpRequestFailureListener<E> failureListener = null;
	
	public AmalgamaHttpRequest(Context context) {
		
		if(context == null)
			throw new NullPointerException("Context cannot be null in AmalgamaHttpRequest");
		
		this.context = context;
		
	}
	
	public Context getContext(){
		return context;
	}
	
	public boolean hasInternetConnection() {
		
	    ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    
	    return false;
	}
	
	public void setSuccessListener(HttpRequestSuccessListener<E> successListener){
		this.successListener = successListener;
	}

    public void setFailureListener(HttpRequestFailureListener<E> failureListener){
        this.failureListener = failureListener;
    }

	@Override
	public void execute() {
		
		if(hasInternetConnection())
			super.execute();
		else
			callFailureListener(null, new NoConnectionException("Context has no network connection"));
	}
	
	@Override
	protected Request getRequest() {
		
		Request result = null;
		
		try {

			Request.Builder requestBuilder = new Request.Builder();

			requestBuilder.url(getRequestUrl());

			Headers headers = getRequestHeaders();

			if(headers != null)
				requestBuilder.headers(headers);

			RequestBody body = getRequestBody();
			String method =getHttpMethod();

			requestBuilder.method(method, body);

			result = requestBuilder.build();

			//TODO borrar
			Log.w("url", getRequestUrl());
			Log.w("method", method);
			if(headers!=null){
				Log.w("header", headers.toString());
			} else {
				Log.w("header","null");
			}
			if(body!=null){
				Log.w("body", bodyToString(result));
			} else {
				Log.w("body","null");
			}
			//TODO borrar

			
		} catch (IllegalArgumentException exception){
			
			exception.printStackTrace();
			result = null;
			
		}
		
		return result;
	}
	
	@Override
	protected void onRequestCompleted(Response httpResponse, E parsedResponse, Exception exception) {
		
		if(exception == null) {
			
			callSuccessListener(parsedResponse);
			
		} else {
			
			callFailureListener(httpResponse, exception);
			
		}
		
	}
	
	protected abstract String getRequestUrl();
	
	protected abstract String getHttpMethod();
	
	protected RequestBody getRequestBody() {
		return null;
	}
	
	protected Headers getRequestHeaders() {
		return null;
	}
	
	protected void callSuccessListener(E parsedResponse){
		
		if(successListener != null)
			successListener.onHttpRequestSuccess(this, parsedResponse);
		
	}
	
	protected void callFailureListener(Response httpResponse, Exception exception){
		
		if(failureListener != null)
			failureListener.onHttpRequestFailure(this, httpResponse, exception);
		
	}

    public interface HttpRequestSuccessListener<E> {
        public void onHttpRequestSuccess(AmalgamaHttpRequest<E> request, E result);
    }

    public interface HttpRequestFailureListener<E> {
        public void onHttpRequestFailure(AmalgamaHttpRequest<E> request, Response httpResponse, Exception exception);
    }

	private String bodyToString(final Request request){

		try {
			final Request copy = request.newBuilder().build();
			final Buffer buffer = new Buffer();
			copy.body().writeTo(buffer);
			return buffer.readUtf8();
		} catch (final IOException e) {
			return "did not work";
		}
	}
	
}
