package com.farr.android.farrapp;


import android.app.Activity;
import android.os.Bundle;


import android.widget.CalendarView;
import android.widget.Toast;


public class CalendarActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        CalendarView calendarView=(CalendarView) findViewById(R.id.calendarView1);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {


            @Override
            public void onSelectedDayChange(CalendarView view,
                int year, int month,int dayOfMonth) {

                Toast.makeText(getApplicationContext(), "" + dayOfMonth, Toast.LENGTH_LONG).show();

            }
        });


    }


}

