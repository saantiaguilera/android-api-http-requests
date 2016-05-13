package com.example.santiago.testings.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.santiago.event.EventManager;
import com.example.santiago.event.anotation.EventMethod;
import com.example.santiago.http.event.RequestManager;
import com.example.santiago.http.service.HttpService;
import com.example.santiago.http_requests.R;
import com.example.santiago.testings.event.FailureEvent;
import com.example.santiago.testings.event.GetRequestEvent;
import com.example.santiago.testings.event.SuccessEvent;

/**
 * Created by santiago on 13/05/16.
 */
public class MActivity extends Activity {

    private EventManager eventManager;
    private RequestManager requestManager;

    private TextView getButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_m);

        getButton = (TextView) findViewById(R.id.activity_m_get_request);

        requestManager = RequestManager.with(this);
        eventManager = new EventManager(this, this);
        eventManager.addObservable(this);

        requestManager.addEventManager(eventManager);

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventManager.dispatchEvent(new GetRequestEvent());
            }
        });
    }

    @EventMethod(SuccessEvent.class)
    private void onSuccess(SuccessEvent event) {
        Log.w(MActivity.class.getName(), event.getString());
        Toast.makeText(MActivity.this, event.getString(), Toast.LENGTH_SHORT).show();
    }

    @EventMethod(FailureEvent.class)
    private void onFailure(FailureEvent event) {
        Log.w(MActivity.class.getName(), event.getException().getMessage());
        Toast.makeText(MActivity.this, event.getException().getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestManager.onStop();
    }

}
