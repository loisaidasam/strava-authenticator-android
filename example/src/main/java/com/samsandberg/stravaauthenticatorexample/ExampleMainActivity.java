package com.samsandberg.stravaauthenticatorexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.samsandberg.stravaauthenticator.StravaAuthenticateActivity;


public class ExampleMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.tv_access_token);

        // Here's how you get the access token passed to the view:
        String accessToken = StravaAuthenticateActivity.getStravaAccessToken(this);
        String text = "Access Token:\n\n" + accessToken;
        tv.setText(text);
    }
}
