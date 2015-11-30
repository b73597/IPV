package com.farr.android.farrapp;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.farr.android.farrapp.search.Item;
import com.farr.android.farrapp.search.ItemDisplayFragment;
import com.farr.android.farrapp.search.ListDisplayFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    ProgressDialog progressDialog;
    double currentLat, currentLng;

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
                itemDistplayFragment.setItem(currentLat,currentLng,item);
                setTitle(item.name);
                getFragmentManager().beginTransaction().replace(R.id.fragment, itemDistplayFragment).commit();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.fragment, listDisplayFragment).commit();

        Intent intent = getIntent();
        if(intent.hasExtra(Consts.MESSAGE_SEARCH_TYPE) && intent.hasExtra(Consts.MESSAGE_SEARCH_INPUT)){
            String keyWord = intent.getStringExtra(Consts.MESSAGE_SEARCH_INPUT);
            String type = intent.getStringExtra(Consts.MESSAGE_SEARCH_TYPE);
            currentLat = intent.getDoubleExtra(Consts.MESSAGE_SEARCH_LAT,0.);
            currentLng = intent.getDoubleExtra(Consts.MESSAGE_SEARCH_LNG,0.);
            progressDialog = ProgressDialog.show(this,"Searching...","Populating data... Please wait...");
            new GooglePlacesSearchTask(keyWord,currentLat,currentLng,5000,type).execute();
        }else{
            finish();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        FragmentManager man = getFragmentManager();
        if(!isShowingList){
            setTitle(R.string.title_activity_search_result);
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
        listDisplayFragment.setResult(currentLat,currentLng,result);
    }


    class GooglePlacesSearchTask extends AsyncTask<Object,Integer, String>
    {
        String urlString = "";
        public GooglePlacesSearchTask(String keyword, double latitude, double longitude, double radius, String type){
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=" + latitude + "," + longitude);
            googlePlacesUrl.append("&radius=" + radius);
            googlePlacesUrl.append("&types=" + (type==null?"":type));
            googlePlacesUrl.append("&name=" + (keyword==null?"":keyword));
            googlePlacesUrl.append("&sensor=true");
            googlePlacesUrl.append("&key=" + Consts.GOOGLE_MAPS_BROWSER_API_KEY);
            urlString = googlePlacesUrl.toString();
        }

        @Override
        protected String doInBackground(Object... params) {
            return Utils.httpGET(urlString);
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject root = new JSONObject(response);
                JSONArray resultsJSON = root.getJSONArray("results");
                ArrayList<Item> result = new ArrayList<Item>();
                for(int i=0, ie=resultsJSON.length();i<ie;i++){
                    result.add(new Item(resultsJSON.getJSONObject(i)));
                }
                onSearchResultPopulated(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    boolean isShowingList;
    ListDisplayFragment listDisplayFragment;
    ItemDisplayFragment itemDistplayFragment;
}
