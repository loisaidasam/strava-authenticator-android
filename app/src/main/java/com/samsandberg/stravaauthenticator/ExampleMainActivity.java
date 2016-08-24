package com.samsandberg.stravaauthenticator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ExampleMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.tv_access_token);
        Intent intent = getIntent();
        String accessToken = intent.getStringExtra(StravaAuthenticateActivity.EXTRA_ACCESS_TOKEN);
        tv.setText(accessToken);
    }
}
