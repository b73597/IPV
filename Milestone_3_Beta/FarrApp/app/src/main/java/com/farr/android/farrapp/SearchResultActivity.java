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
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    ProgressDialog progressDialog;
    double currentLat, currentLng;
    ArrayList<Item> googleSearchResult, parseSearchResult;

    Integer nTasks;

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
            if(keyWord==null) keyWord = "";
            String type = intent.getStringExtra(Consts.MESSAGE_SEARCH_TYPE);
            if(type==null) type = "";

            currentLat = intent.getDoubleExtra(Consts.MESSAGE_SEARCH_LAT, 0.);
            currentLng = intent.getDoubleExtra(Consts.MESSAGE_SEARCH_LNG, 0.);
            progressDialog = ProgressDialog.show(this, "Searching...", "Populating data... Please wait...");

            googleSearchResult = null;
            parseSearchResult = null;

            new GooglePlacesSearchTask(keyWord,currentLat,currentLng,5000,type).execute();

            ArrayList<ParseQuery<ParseObject> > queries = new ArrayList<>();
            queries.add(ParseQuery.getQuery("Places").whereContains("name_search", keyWord.toLowerCase()));
            queries.add(ParseQuery.getQuery("Places").whereContains("category", keyWord.toLowerCase()));
            ParseQuery<ParseObject> query = ParseQuery.or(queries);
            query.whereWithinKilometers("location", new ParseGeoPoint(currentLat, currentLng), 5000.);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e!=null) onParseSearchResultPopulated(new ArrayList<ParseObject>());
                    else onParseSearchResultPopulated(objects);
                }
            });
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

    synchronized void  onSearchResultPopulated(){
        if(googleSearchResult!=null && parseSearchResult!=null) {
            ArrayList<Item> result = new ArrayList<>();
            result.addAll(parseSearchResult);
            result.addAll(googleSearchResult);

            progressDialog.hide();
            listDisplayFragment.setResult(currentLat, currentLng, result);
        }
    }

    synchronized void onGoogleSearchResultPopulated(ArrayList<Item> result){
        googleSearchResult = result;
        onSearchResultPopulated();
    }

    synchronized void onParseSearchResultPopulated(List<ParseObject> result){
        parseSearchResult = new ArrayList<>();
        for(ParseObject obj : result){
            ParseGeoPoint point = obj.getParseGeoPoint("location");
            Item item = new Item(obj.getString("name"), obj.getString("category"), point.getLatitude(), point.getLongitude());
            parseSearchResult.add(item);
        }
        onSearchResultPopulated();
    }


    class GooglePlacesSearchTask extends AsyncTask<Object,Integer, String>
    {
        String urlString = "";
        public GooglePlacesSearchTask(String keyword, double latitude, double longitude, double radius, String type){
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=" + latitude + "," + longitude);
            googlePlacesUrl.append("&radius=" + radius);
            try {
                googlePlacesUrl.append("&type=" + URLEncoder.encode(type == null ? "" : type, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
            try {
                googlePlacesUrl.append("&name=" + URLEncoder.encode(keyword == null ? "" : keyword, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
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
            ArrayList<Item> result = new ArrayList<Item>();
            try {
                JSONObject root = new JSONObject(response);
                JSONArray resultsJSON = root.getJSONArray("results");
                for(int i=0, ie=resultsJSON.length();i<ie;i++){
                    result.add(new Item(resultsJSON.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {

            }
            onGoogleSearchResultPopulated(result);
        }
    }

    boolean isShowingList;
    ListDisplayFragment listDisplayFragment;
    ItemDisplayFragment itemDistplayFragment;
}
