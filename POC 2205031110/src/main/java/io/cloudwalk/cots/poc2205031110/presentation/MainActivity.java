package io.cloudwalk.cots.poc2205031110.presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import io.cloudwalk.cots.poc2205031110.Kernel;
import io.cloudwalk.cots.poc2205031110.R;

public class MainActivity extends AppCompatActivity {
    private static final String
            TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Kernel.create(); // 2022-05-06: DEMO-only
    }
}
