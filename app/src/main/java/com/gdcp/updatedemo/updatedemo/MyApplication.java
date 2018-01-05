package com.gdcp.updatedemo.updatedemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by asus- on 2018/1/5.
 */

public class MyApplication extends Application{
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
