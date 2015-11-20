package com.farr.android.farrapp;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.farr.android.farrapp.search.Item;
import com.farr.android.farrapp.search.ItemDisplayFragment;
import com.farr.android.farrapp.search.ListDisplayFragment;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        isShowingList = true;
        itemDistplayFragment = new ItemDisplayFragment();
        listDisplayFragment = new ListDisplayFragment();
        listDisplayFragment.setItemClickListener(new ListDisplayFragment.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                isShowingList = false;
                getFragmentManager().beginTransaction().replace(R.id.fragment, itemDistplayFragment).commit();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.fragment, listDisplayFragment).commit();

        Intent intent = getIntent();
        if(intent.hasExtra(Consts.MESSAGE_SEARCH_TYPE) && intent.hasExtra(Consts.MESSAGE_SEARCH_INPUT)){
            progressDialog = ProgressDialog.show(this,"Searching...","Populating data... Please wait...");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Item> result = new ArrayList<Item>();
                    result.add(new Item("Eddy's Bar", "bar"));
                    result.add(new Item("Livenite Club", "club"));
                    result.add(new Item("Julio's Restaurance", "retaurance"));
                    onSearchResultPopulated(result);
                }
            }, 3000);
        }else{
            finish();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        FragmentManager man = getFragmentManager();
        if(!isShowingList){
            getFragmentManager().beginTransaction().replace(R.id.fragment, listDisplayFragment).commit();
            isShowingList = true;
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

    void onSearchResultPopulated(ArrayList<Item> result){
        progressDialog.hide();
        listDisplayFragment.setResult(result);
    }

    boolean isShowingList;
    ListDisplayFragment listDisplayFragment;
    ItemDisplayFragment itemDistplayFragment;
}
