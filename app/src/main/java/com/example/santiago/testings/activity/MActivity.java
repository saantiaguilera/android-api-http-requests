package com.example.santiago.testings.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.santiago.event.EventManager;
import com.example.santiago.event.anotation.EventMethod;
import com.example.santiago.http.http.HttpManager;
import com.example.santiago.http_requests.R;
import com.example.santiago.testings.event.FailureEvent;
import com.example.santiago.testings.event.GetRequestEvent;
import com.example.santiago.testings.event.SuccessEvent;

/**
 * Testing purposes
 *
 * Created by santiago on 13/05/16.
 */
public class MActivity extends Activity {

    private EventManager eventManager;
    private HttpManager httpManager;

    private TextView getButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Normal stuff we should know
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        setContentView(R.layout.activity_m);

        getButton = (TextView) findViewById(R.id.activity_m_get_request);

        //Get the http manager instance we will be using in this activity
        httpManager = HttpManager.getInstance();

        //Stuff we should know about (else go to the Controllers-and-events github repo)
        eventManager = new EventManager(this, this);
        eventManager.addObservable(this);

        //Add the eventmanager that will be able to dispatch events from the network
        httpManager.addEventManager(eventManager);

        //Stuff we should know about
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventManager.dispatchEvent(new GetRequestEvent());
            }
        });
    }


    //Method to show when succeeds and its output
    @EventMethod(SuccessEvent.class)
    private void onSuccess(SuccessEvent event) {
        if (event.getString() != null) {
            Log.w(MActivity.class.getName(), event.getString());
            Toast.makeText(MActivity.this, event.getString(), Toast.LENGTH_SHORT).show();
        }
    }

    //Method to show when fails and its output
    @EventMethod(FailureEvent.class)
    private void onFailure(FailureEvent event) {
        if (event.getException().getMessage() != null) {
            Log.w(MActivity.class.getName(), event.getException().getMessage());
            Toast.makeText(MActivity.this, event.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Start the http manager
        httpManager.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop the http manager
        httpManager.onStop(this);
    }

}
