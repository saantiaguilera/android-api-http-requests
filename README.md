#OkHttp Requests on Event Bus + Sugars! 

(Supports handling of configuration changes of activities without fragment shitty retaininstance or memmory kills)

Simple OkHttp wrapper for easier networking operations (although you can use it as complex as you want, customizing your own dispatchers/cache/authenticators/interceptors/etc) + my own bus of events (because its way too old (a lot before EventBus was created)) and since I had always use it, I just keep updating it and using it for all my apps. Its really easy to swap it for the common event bus tho, so requests about it are welcome.

Also it features asynchronous operations (everything about the okhttp things are done asynchronously in a threadpool + http requests are done in the threadpool provided by the okhttp dispatcher (which you can customize it as  you please if needed). 

Parsing of the response is done async also, but the callback of success/failure is done in the main thread (to keep in mind)

This library also helps with some of the usual problems android devs face every day, like configuration changes of activities. Http responses can survive over a configuration change, since we are running over a bus + service. So you can always dispatch events to the bus and the new recreated activity, if listening to the bus (which is most certainly since you will probably register in a lifecycle method), will still receive the response.

Im on jCenter/Maven !! :D 

##How to get it

In your project gradle make sure you have
```Java
allprojects {
	repositories {
		jcenter()
	}
}
```

In your application gradle add 
```Java
dependencies {
  compile 'com.saantiaguilera:HttpBusLib:1.0.3'
}
```

Be sure you have in your application gradle
```Java
repositories {
	mavenCentral()	
}
```

##Networking

###Set up

Since this uses the Event bus, requests are events and responses too. If a class wants to listen to http responses just suscribe it

```Java
    HttpBus.getInstance().addObservable(this);
```

We will take care of the rest (even garbage collecting, cache for avoiding over battery consumption/extra processing and of course avoiding memory leaks)

Of course, you can always remove an observable by yourself to fasten up :)

###Creating a Request

Simply create a Event class that subclassifies the RequestEvent class

Get Request Example:
```Java
public class GetRequestEvent extends RequestEvent<String> {
//The generic value will be the expected class to be returned from the network call (in this case is a String)

	/*-------------- The following methods run on a background thread, so you can do heavy operations like reading files or wver if needed ---------*/

    @NonNull
    @Override
    public String getUrl() {
        return "http://the.url.com";
    }

    @NonNull
    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Nullable
    @Override
    //This is optional. You can override this if you need it (unless its mandatory like in a POST)
    public RequestBody getBody() {
        return null;
    }

    @Nullable
    @Override
    //Same. This is optional.
    public Headers getHeaders() {
        return null;
    }

    @Override
    public String parseResponse(@NonNull Response response) throws HttpParseException {
        //Do something
    }

	/*-------- The following methods run on the main UI, so you can post to views or whatever (although I dislike that idea) ------------*/

    @Override
	 //This is optional, override if you want to do something when failing a request
    public void onHttpRequestFailure(@NonNull Exception exception) {
        //Do something
        HttpBus.getInstance().dispatchEvent(new FailureEvent(exception));
    }

    @Override
	 //This is optional, override if you want to do something when succeding a request
    public void onHttpRequestSuccess(String result) {
        //Do something
        HttpBus.getInstance().dispatchEvent(new SuccessEvent(result));
    }
}
```

###Executing

Just do
```Java
//Somewhere in a method...
HttpBus.getInstance().dispatchEvent(new GetRequestEvent());
```

You can configure the Http client as much as you like (although I didnt do all of them because of laziness (just the ones I used the most)). If you need one of them tell me and I will ofc add them.

You can for example add an interceptor by creating an event of HttpInterceptorEvent.

The features available are:
- Authenticator
- Cache
- Cancel a request
- Cancel all requests
- Cookies 
- Dispatcher
- Interceptor
- Timeouts
- Sticky headers (Headers that are for all requests)

In a RequestEvent, you can optionally override the method overrideClient(OkHttp.Builder builder) to make your own okhttp client (the param gives you the current okhttp client) and execute that single call with the defined client. 

Bear in mind that it will only apply to the give request. This isnt for all future request. (You can do that by subclassification if needed)
