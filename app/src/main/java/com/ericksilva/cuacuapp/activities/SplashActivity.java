package com.ericksilva.cuacuapp.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.activities.login.LoginActivity;
import com.ericksilva.cuacuapp.activities.onbording.OnboardingActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        final Fabric fabric = new Fabric.Builder(this)
//                .kits(new Crashlytics())
//                .debuggable(true)           // Enables Crashlytics debugger
//                .build();
//        Fabric.with(fabric);

        new Handler().postDelayed(mRunnable, SPLASH_TIME_OUT);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {


//            Crashlytics.getInstance().crash();

            Intent intent;
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this,MainActivity.class);
            } else {
                // not signed in
                intent = new Intent(SplashActivity.this,OnboardingActivity.class);
            }
            startActivity(intent);
            finish();
        }
    };
}
