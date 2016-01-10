package com.farr.android.farrapp.event;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.farr.android.farrapp.R;
import com.farr.android.farrapp.search.Item;

public class EventList extends AppCompatActivity {
    FloatingActionButton btnNewEvent;
    EventListFragment listFragment;
    EventEditFragment editFragment;

    boolean inEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        btnNewEvent = (FloatingActionButton) findViewById(R.id.new_event);
        btnNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEdit(null);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listFragment = new EventListFragment();
        listFragment.setItemClickListener(new EventListFragment.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                showEdit(item);
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.fragment, listFragment).commit();

        inEdit = false;
        editFragment = new EventEditFragment();
        editFragment.setDoneListener(new EventEditFragment.EventEditDone() {
            @Override
            public void onEditDone() {
                onBackPressed();
            }
        });
        setTitle("Event List");
    }

    @Override
    public void onBackPressed() {
        if(inEdit){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    inEdit = false;
                    setTitle("Event List");
                    listFragment = new EventListFragment();
                    listFragment.setItemClickListener(new EventListFragment.OnItemClickListener() {
                        @Override
                        public void onItemClick(Item item) {
                            showEdit(item);
                        }
                    });
                    getFragmentManager().beginTransaction().replace(R.id.fragment, listFragment).commit();
                    btnNewEvent.show();
                }
            });
        }else{
                finish();
        }
    }

            @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showEdit(final Item item){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("OWNER", "showEdit ");
                if(item!=null && item.name!=null) setTitle(item.name);
                else setTitle("New Event");
                if(!inEdit) {
                    inEdit = true;
                    getFragmentManager().beginTransaction().replace(R.id.fragment, editFragment).commit();
                    editFragment.setItem(item);
                    btnNewEvent.hide();
                }
            }
        });
    }
}
