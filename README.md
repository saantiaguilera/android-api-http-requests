# android-api-http-requests
OkHttp Request wrapper

Wrapper of OkHttp3 to make Network Calls from a Service (+ bg threads)

Uses Events (https://github.com/saantiaguilera/android-api-controllers-and-events)

Usage:
--------------------------------------------------------------------------------
-------------------------------------Set up-------------------------------------
--------------------------------------------------------------------------------

In an Activity (or something that you plan on binding with the service to make the network calls)
You should Override your onStart and onStop and call the HttpManager methods.
```Java
    private HttpManager httpManager;
    
    @Override
    protected void onStart() {
        super.onStart();

        httpManager.onStart(aContextWrapper);
    }

    @Override
    protected void onStop() {
        super.onStop();

        httpManager.onStop(aContextWrapper);
    }
```

For creating an instance of HttpManager, since its a Singleton use 
```Java
        httpManager = HttpManager.getInstance();
```

Since this uses the Event library, (you will be doing requests via events and responses can dispatch other events). You can have more than one EventManager (or EventBus if you are familiar with it)
So for each EventManager that will be able to "handle network connections" add it to the HttpManager.

```Java
        EventManager eventManager = new EventManager(mActivity, aTag);
        httpManager.addEventManager(eventManager);
```

--------------------------------------------------------------------------------
-------------------------------Creating a Request-------------------------------
--------------------------------------------------------------------------------

Simply create a Event class that subclassifies the RequestEvent class

Get Request Example:
```Java
public class GetRequestEvent extends RequestEvent<String> { 
//The generic value will be the expected class to be returned from the network call (in this case is a String)

    private int aParam;

    public GetRequestEvent(int aParam) {
      this.aParam = aParam;
    }

    @Override
    protected Request buildRequest() {
        //Here build the request, if you will use params for body headers wver just pass them from the constructor or via setters
        return new Request.Builder()
                .url("http://someurltohit.com/somepath/" + aParam)
                .get()
                .build();
    }

    @Override
    protected String parseResponse(@NonNull Response response) throws HttpParseException {
        try {
            //Parse the response if you plan on returning a particular class
            return response.body().string();
        } catch (IOException e) {
            throw new HttpParseException(e);
        }
    }

    @Override
    protected void onHttpRequestFailure(@NonNull EventListener dispatcher, @NonNull Exception exception) {
        //Do something if the request failed...
    }

    @Override
    protected void onHttpRequestSuccess(@NonNull EventListener dispatcher, String result) {
        //Do something if the request succeeded...
    }

}
```
