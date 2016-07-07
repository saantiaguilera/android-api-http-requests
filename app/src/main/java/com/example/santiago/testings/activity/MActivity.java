package com.example.santiago.testings.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.santiago.event.EventBus;
import com.example.santiago.event.anotation.EventMethod;
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

        //Stuff we should know about
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getHttpBus().dispatchEvent(new GetRequestEvent());
            }
        });

        EventBus.getHttpBus().addObservable(this);
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

}
