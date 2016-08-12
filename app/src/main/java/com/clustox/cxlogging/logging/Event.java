package com.clustox.cxlogging.logging;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by Johar on 8/9/2016.
 */

public final class Event {
    private String mLogType;
    private String mMethodName;
    private String mTimeStamp;
    private String mDeviceId;
    private String mDeviceName;
    private String mOperatingSystemVersion;
    private String mPlatform;

    public Event(@NonNull String logType, @NonNull String methodName, Context context) {
        this.mLogType = logType;
        this.mMethodName = methodName;
        this.mTimeStamp = Calendar.getInstance().getTime().toString();
        this.mDeviceName = Build.MODEL;
        this.mDeviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.mOperatingSystemVersion = Build.VERSION.RELEASE;
        this.mPlatform = "Android";
    }

    public String getLogType() {
        return mLogType;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getOperatingSystemVersion() {
        return mOperatingSystemVersion;
    }

    public String getPlatform() {
        return mPlatform;
    }
}
