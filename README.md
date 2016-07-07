#OkHttp Request wrapper w events

##Networking

###Set up

You should init the Http service once (You can do it n times, but with 1 its enough, consecuents will be ignored).

Its highly recommended to do it in a Application context (Or use a ContentProvider), but you can still do it in a Activity or anything that has a reference to a context.

Note that without initializing it, it may have unexpected behaviors
```Java
    //In a Application onCreate()
    EventBus._initHttpBus(this);
```

Since this uses the Event bus, requests are events and responses too. If a class wants to listen to http responses just suscribe it

```Java
    EventBus.getHttpBus().addObservable(this);
```

We will take care of the rest (even garbage collecting and avoiding memory leaks)


###Creating a Request


Simply create a Event class that subclassifies the RequestEvent class

Get Request Example:
```Java
public class GetRequestEvent extends RequestEvent<String> {
//The generic value will be the expected class to be returned from the network call (in this case is a String)

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
    //You can override this if you need it
    public RequestBody getBody() {
        return null;
    }

    @Nullable
    @Override
    //You can override this if you need it
    public Headers getHeaders() {
        return null;
    }

    @Override
    public String parseResponse(@NonNull Response response) throws HttpParseException {
        //Do something
    }

    @Override
    public void onHttpRequestFailure(@NonNull Exception exception) {
        //Do something
        EventBus.getHttpBus().dispatchEvent(new FailureEvent(exception));
    }

    @Override
    public void onHttpRequestSuccess(String result) {
        //Do something
        EventBus.getHttpBus().dispatchEvent(new SuccessEvent(result));
    }
}
```

###Executing

Just do
```Java
//Somewhere in a method...
EventBus.getHttpInstance().dispatchEvent(new GetRequestEvent());
```


##Events

Create somewhere an instance of an EventBus.
With this you will be able to start suscribing objects to "receive events" and also dispatching events to them!
```Java
eventBus = new EventBus(aContext);
```

If you want a class to start listening to events just
```Java
eventBus.addObservable(something);
//or...
eventBus.removeObservable(something);
```

Now this "something" is able to start receiving Events !! But where does he receives them?

Lets say we have OneEvent and TwoEvent. He can receives them like:
```Java
@EventMethod(OneEvent.class)
private void oneMethod() {
    //Do something
}

@EventMethod(TwoEvent.class)
private void anotherMethod(TwoEvent event) {
    //Do something
}
```
Note: Method can only have either 1 param of the particular Event type or none

Note: When I get more time I will try to support the repeatable anotation since its not available yet
(Kinda like the @RequiresPermission() does)

So... Now we know how to receive events and how to start listening. What about sending one ?
```Java
///Somewhere in a method...
eventBus.dispatchEvent(new OneEvent());
```
And this will alone call all the methods that have its anotation and are observing that eventManager instance.

Also if you want to execute that particular method in another thread just do
```Java
@EventAsync
@EventMethod(SomeEvent.class)
private void whenCallingThisFromADispatchOfAnEventItWillBeAsynchronous() {
  //Do something...
}
```
But if you dispatch another event inside there, be careful that the observables will still execute their code in the main thread!
(Unless they also specified @EventAsync)

Finally, what is SomeEvent ??
```Java
public class SomeEvent extends Event {
    int aParam;
    String anotherParam;

    public SomeEvent(int aParam, String anotherParam) {
      this.aParam = aParam;
      this.anotherParam = anotherParam;
    }

    //getters...
}
```
Its just a subclassification of Event. Although it can have its own logic, ideally it should only be able to carry data from some
place to another.

You also have some other features like "dispatchSticky" which dispatches an event to all the current observables + the new ones that suscribe later to the bus
```Java
eventBus.dispatchEventSticky(new SomeEvent());
```
