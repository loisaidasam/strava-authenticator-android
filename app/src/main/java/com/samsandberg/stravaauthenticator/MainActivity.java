package com.samsandberg.stravaauthenticator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.tv_access_token);
        String accessToken = savedInstanceState.getString(AuthenticateActivity.EXTRA_ACCESS_TOKEN);
        tv.setText(accessToken);
    }
}
