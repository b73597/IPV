package com.farr.android.farrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.farr.android.farrapp.search.Item;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {
    Location lastKnownLocation = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchInput = (EditText) findViewById(R.id.search_input);



        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    processSearch();
                    return true;
                }
                return false;
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        locationManager.requestLocationUpdates(bestProvider,0, 0, this);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_more);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMenu = new SlidingMenu(this);
        mMenu.setMode(SlidingMenu.LEFT);
        mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mMenu.setShadowDrawable(R.drawable.shadow);
        mMenu.setBehindOffset(200);
        mMenu.setFadeDegree(0.35f);
        mMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mMenu.setMenu(R.layout.menu);

        ViewGroup vg = (ViewGroup) mMenu.getMenu().findViewById(R.id.menu_items_container);
        for(int i = 0, ie = vg.getChildCount(); i<ie; i++){
            vg.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ParseUser.getCurrentUser()==null){
            finish();
            return;
        }
    }

    private void popuplateNearbyEvents() {
        if(currentTask!=null) currentTask.cancel(true);
        currentTask = new NearbyEventTask();
        currentTask.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mMenu.isMenuShowing()) {
            ParseUser.getCurrentUser().logOut();
        }else{
            mMenu.showMenu();
        }
    }

    @Override
    public boolean onNavigateUp() {
        //ParseUser.getCurrentUser().logOut();
        //return super.onNavigateUp();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        this.googleMap = googleMap;
        popuplateNearbyEvents();
    }

    void processSearch(){
        if(lastKnownLocation==null){
            Toast.makeText(getApplicationContext(),"Location unavaiable!", Toast.LENGTH_SHORT).show();
        }else {
            Intent searchIntent = new Intent(MainActivity.this, SearchResultActivity.class);
            searchIntent.putExtra(Consts.MESSAGE_SEARCH_INPUT, searchInput.getText().toString());
            searchIntent.putExtra(Consts.MESSAGE_SEARCH_LAT, lastKnownLocation.getLatitude());
            searchIntent.putExtra(Consts.MESSAGE_SEARCH_LNG, lastKnownLocation.getLongitude());
            searchIntent.putExtra(Consts.MESSAGE_SEARCH_TYPE, searchType);
            startActivity(searchIntent);
        }
    }

    EditText searchInput;
    MapFragment mapFragment;
    SlidingMenu mMenu;
    String searchType;
    GoogleMap googleMap;
    NearbyEventTask currentTask;

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation = location;
        popuplateNearbyEvents();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menu_btn_all:
                searchType = Consts.GooglePlaceSearchTypes.cafe
                        + "|" + Consts.GooglePlaceSearchTypes.casino
                        + "|" + Consts.GooglePlaceSearchTypes.food
                        + "|" + Consts.GooglePlaceSearchTypes.restaurant
                ;
                setTitle("Enter search text");
                mMenu.showContent();
                break;
            case R.id.menu_btn_beer:
                searchType = Consts.GooglePlaceSearchTypes.bar;
                setTitle("Search for Bars");
                mMenu.showContent();
                break;
            case R.id.menu_btn_food:
                searchType = Consts.GooglePlaceSearchTypes.restaurant;
                setTitle("Search for Restaurants");
                mMenu.showContent();
                break;
            case R.id.menu_btn_night:
                searchType = Consts.GooglePlaceSearchTypes.night_club;
                setTitle("Search for Nightclubs");
                mMenu.showContent();
                break;
            case R.id.menu_btn_calendar:
                startActivity(new Intent(this, Calendar.class));
                break;
            case R.id.menu_btn_logout:
                ParseUser.logOut();
                finish();
                break;
        }
    }

    class NearbyEventTask extends AsyncTask<String, Void, ArrayList<Item>>
    {
        Random r;
        public NearbyEventTask(){
            r = new Random();
        }
        public LatLng getRandomLocation(Location center, double radius) {
            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 111000;

            double u = r.nextDouble();
            double v = r.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double lat = w * Math.cos(t);
            double lon = w * Math.sin(t);

            double new_lat = lat / Math.cos(center.getLongitude());
            return new LatLng(new_lat + center.getLatitude(), lon + center.getLongitude());
        }
        @Override
        protected ArrayList<Item> doInBackground(String... params) {
            ArrayList<Item> list = new ArrayList<Item>();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Places");
            query.whereNear("location", new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
            try {
                List<ParseObject> objects = query.find();
                for(ParseObject obj : objects){
                    ParseGeoPoint point = obj.getParseGeoPoint("location");
                    Item item = new Item(obj.getString("name"), obj.getString("category"), point.getLatitude(), point.getLongitude());
                    item.vicinity = obj.getString("description") + " | "+obj.getDate("event_date");
                    list.add(item);
                }
            } catch (ParseException e) {
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> arrayList) {
            if(isCancelled()) return;
            googleMap.clear();
            LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();
            for(Item item: arrayList){
                googleMap.addMarker(new MarkerOptions()
                        .position(item.location())
                        .title(item.name))
                        .setSnippet(item.vicinity);
                boundBuilder.include(item.location());
            }
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 200);
            googleMap.moveCamera(cameraUpdate);
            googleMap.animateCamera(cameraUpdate, 1000, null);
        }
    }
}
