package com.farr.android.farrapp;

import android.app.Application;

import com.parse.Parse;

public class FarrApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "QuFydL2EEzFdGQPqnaINyiFmWuXLQa3O0Nw7UjlY", "jGUGbNA4MmeblTfIWf6P5ASRUaiIl0yAAF5VXgPo");
    }
}
