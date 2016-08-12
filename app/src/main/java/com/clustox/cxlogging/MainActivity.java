package com.clustox.cxlogging;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.clustox.cxlogging.logging.CXLogger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CXLogger.getInstance(this).log(TAG + "OnCreate", CXLogger.LogType.INFO);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CXLogger.getInstance(this).log(TAG + "onStop", CXLogger.LogType.INFO);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        CXLogger.getInstance(this).log(TAG + "onPostResume", CXLogger.LogType.INFO);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        CXLogger.getInstance(this).log(TAG + "onPostCreate", CXLogger.LogType.INFO);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CXLogger.getInstance(this).log(TAG + "onDestroy", CXLogger.LogType.INFO);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CXLogger.getInstance(this).log(TAG + "onSaveInstanceState", CXLogger.LogType.INFO);

    }

    @Override
    protected void onPause() {
        super.onPause();
        CXLogger.getInstance(this).log(TAG + "onPause", CXLogger.LogType.INFO);

    }

    @Override
    protected void onResume() {
        super.onResume();
        CXLogger.getInstance(this).log(TAG + "onResume", CXLogger.LogType.INFO);


    }
}
