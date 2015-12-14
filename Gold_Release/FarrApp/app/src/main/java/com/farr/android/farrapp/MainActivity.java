package com.farr.android.farrapp;

import android.app.Fragment;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    SlidingMenu mMenu;

    MainFragment mainFragment;
    SettingFragment settingFragment;
    CalendarFragment calendarFragment;

    Fragment currentFragment, pendingFragment;

    String lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_more);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMenu = new SlidingMenu(this);
        mMenu.setMode(SlidingMenu.LEFT);
        //mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mMenu.setShadowDrawable(R.drawable.shadow);
        mMenu.setBehindOffset(200);
        mMenu.setFadeDegree(0.35f);
        mMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mMenu.setMenu(R.layout.menu);

        ViewGroup vg = (ViewGroup) mMenu.getMenu().findViewById(R.id.menu_items_container);
        for(int i = 0, ie = vg.getChildCount(); i<ie; i++){
            vg.getChildAt(i).setOnClickListener(this);
        }

        if (mainFragment == null){
            mainFragment = new MainFragment();
            mainFragment.setRetainInstance(true);
        }
        if (settingFragment == null){
            settingFragment = new SettingFragment();
            settingFragment.setRetainInstance(true);
        }
        if (calendarFragment == null){
            calendarFragment = new CalendarFragment();
            calendarFragment.setRetainInstance(true);
        }

        if(savedInstanceState==null) {
            pendingFragment = mainFragment;
        }else{
            currentFragment = null;
            pendingFragment = null;
            String lastFragment = savedInstanceState.getString("fragment");
            if (MainFragment.class.toString().equals(lastFragment)){
                pendingFragment=mainFragment;
            }
            else if (SettingFragment.class.toString().equals(lastFragment)){
                pendingFragment=settingFragment;
            }
            else if (CalendarFragment.class.toString().equals(lastFragment)){
                pendingFragment=calendarFragment;
            } else {
                pendingFragment=mainFragment;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("fragment", lastFragment);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pendingFragment!=null && pendingFragment!=currentFragment){
            showFragment(pendingFragment);
        }
        pendingFragment = null;
    }

    @Override
    protected void onPause() {
        if(currentFragment!=null) {
            lastFragment = currentFragment.getClass().toString();
            pendingFragment = currentFragment;
        } else {
            lastFragment = "";
        }
        showFragment(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ParseUser.getCurrentUser()==null){
            finish();
            return;
        }
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menu_btn_all:
                mainFragment.searchType = Consts.GooglePlaceSearchTypes.cafe
                        + "|" + Consts.GooglePlaceSearchTypes.casino
                        + "|" + Consts.GooglePlaceSearchTypes.food
                        + "|" + Consts.GooglePlaceSearchTypes.restaurant
                ;
                setTitle("Enter search text");
                mMenu.showContent();
                showFragment(mainFragment);
                break;
            case R.id.menu_btn_beer:
                mainFragment.searchType = Consts.GooglePlaceSearchTypes.bar;
                setTitle("Search for Bars");
                mMenu.showContent();
                showFragment(mainFragment);
                break;
            case R.id.menu_btn_food:
                mainFragment.searchType = Consts.GooglePlaceSearchTypes.restaurant;
                setTitle("Search for Restaurants");
                mMenu.showContent();
                showFragment(mainFragment);
                break;
            case R.id.menu_btn_night:
                mainFragment.searchType = Consts.GooglePlaceSearchTypes.night_club;
                setTitle("Search for Nightclubs");
                mMenu.showContent();
                showFragment(mainFragment);
                break;
            case R.id.menu_btn_calendar:
                mMenu.showContent();
                showFragment(calendarFragment);
                break;
            case R.id.menu_btn_settings:
                mMenu.showContent();
                showFragment(settingFragment);
                break;
            case R.id.menu_btn_logout:
                ParseUser.logOut();
                finish();
                break;
        }
    }

    void showFragment(Fragment fragment){
        if(currentFragment!=fragment) {
            if(currentFragment!=null){
                getFragmentManager().beginTransaction()
                        .remove(currentFragment)
                        .addToBackStack(null)
                        .commit();
                if(fragment!=null) {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_fragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }else {
                if(fragment!=null) {
                    getFragmentManager()
                            .beginTransaction()
                            .add(R.id.main_fragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
            currentFragment = fragment;
        }
    }

    public static class MainFragment extends Fragment implements OnMapReadyCallback, LocationListener
    {
        Location lastKnownLocation = null;
        EditText searchInput;
        MapFragment mapFragment;
        String searchType;
        GoogleMap googleMap;
        NearbyEventTask currentTask;
        View createdView;
        HashMap<Marker, Item> mMarker2Item = new HashMap<>();

        public MainFragment(){
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if(createdView==null) createdView = inflater.inflate(R.layout.home_fragment, container, false);
            return createdView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onStart() {
            super.onStart();
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            searchInput = (EditText) getView().findViewById(R.id.search_input);


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

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            //Location location = locationManager.getLastKnownLocation(bestProvider); // FOR GOLD RELEASE
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // FOR SIMULATOR TEST
            locationManager.requestLocationUpdates(bestProvider,0, 0, this);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
        }

        @Override
        public void onResume() {
            super.onResume();

        }

        void processSearch(){
            if(lastKnownLocation==null){
                Toast.makeText(getActivity(),"Location unavaiable!", Toast.LENGTH_SHORT).show();
            }else {
                Intent searchIntent = new Intent(getActivity(), SearchResultActivity.class);
                searchIntent.putExtra(Consts.MESSAGE_SEARCH_INPUT, searchInput.getText().toString());
                searchIntent.putExtra(Consts.MESSAGE_SEARCH_LAT, lastKnownLocation.getLatitude());
                searchIntent.putExtra(Consts.MESSAGE_SEARCH_LNG, lastKnownLocation.getLongitude());
                searchIntent.putExtra(Consts.MESSAGE_SEARCH_TYPE, searchType);
                startActivity(searchIntent);
            }
        }

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
                        Item item = new Item(obj.getString("name"), obj.getString("category"), obj.getString("description"), point.getLatitude(), point.getLongitude());
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
                mMarker2Item.clear();
                LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();
                for(Item item: arrayList){
                    Marker newMarker = googleMap.addMarker(new MarkerOptions()
                            .position(item.location())
                            .title(item.name));
                    newMarker.setSnippet(item.vicinity);
                    mMarker2Item.put(newMarker,item);
                    boundBuilder.include(item.location());
                }
                try {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 200);
                    googleMap.moveCamera(cameraUpdate);
                    googleMap.animateCamera(cameraUpdate, 1000, null);
                }catch (Exception ex){

                }
            }
        }

        @Override
        public void onMapReady(final GoogleMap googleMap) {
            googleMap.setMyLocationEnabled(true);
            this.googleMap = googleMap;
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                // Use default InfoWindow frame
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                // Defines the contents of the InfoWindow
                @Override
                public View getInfoContents(Marker marker) {

                    // Getting view from the layout file info_window_layout
                    View v = getActivity().getLayoutInflater().inflate(R.layout.maps_infowindow, null);
                    v.setLayoutParams(new LinearLayout.LayoutParams((int) (mapFragment.getView().getMeasuredWidth() * .9), LinearLayout.LayoutParams.WRAP_CONTENT));

                    ((TextView) v.findViewById(R.id.title)).setText(marker.getTitle());
                    ((TextView) v.findViewById(R.id.desc)).setText(marker.getSnippet());

                    return v;

                }
            });

            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Item item = mMarker2Item.get(marker);
                    if(item!=null) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hang out now!");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Let's hang out here http://maps.google.com/maps?z=12&t=m&q=loc:" + item.lat + "+" + item.lon);
                        startActivity(Intent.createChooser(sharingIntent, "Share using"));
                    }
                }
            });


            popuplateNearbyEvents();
        }

        private void popuplateNearbyEvents() {
            if(currentTask!=null) currentTask.cancel(true);
            currentTask = new NearbyEventTask();
            currentTask.execute();
        }
    }
}