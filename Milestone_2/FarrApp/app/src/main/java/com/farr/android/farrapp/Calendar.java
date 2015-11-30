package com.farr.android.farrapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;



public class Calendar extends ListActivity {

    ListView listView;
    String[] events= {
            "Free Apetizer - TODAY - Luigis Pizza",
            "Metallica - May 8 - C.U Stadium",
            "80's Dance - June 9 - Classicos Bar",
            "Folk Music - June 11 - Cultural Center",
            "Open Bar - October 10 - Eddy's Bar"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_calendar_layout);

        AppCompatCallback cb = new AppCompatCallback() {

            @Override
            public void onSupportActionModeStarted(ActionMode actionMode) {
            }
            @Override
            public void onSupportActionModeFinished(ActionMode actionMode) {
            }
            @Override
            public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback cb) {
                return null;
            }
        };
        AppCompatDelegate delegate = AppCompatDelegate.create(this, cb);
        delegate.onCreate(savedInstanceState);

        delegate.setContentView(R.layout.test_calendar_layout);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setChoiceMode(listView.CHOICE_MODE_MULTIPLE);

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, events));
    }
    public void onListItemClick(ListView parent, View view,int position,long id){

        if(listView.isItemChecked(position)){

            Toast.makeText(this, "Alarm ON", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Alarm OFF", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seach_result, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}





