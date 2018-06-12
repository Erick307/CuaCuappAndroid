package com.ericksilva.cuacuapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ericksilva.cuacuapp.R;


public class MainActivity extends AppCompatActivity {

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context,MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
