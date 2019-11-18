package com.wozzytheprogrammer.kwproperty.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wozzytheprogrammer.kwproperty.R;

public class SplashActivity extends AppCompatActivity {
    private TextView poweredByText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        poweredByText = findViewById(R.id.poweredByText);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent welcomeIntent = new Intent(SplashActivity.this, LauncherActivity.class);
                    startActivity(welcomeIntent);
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
