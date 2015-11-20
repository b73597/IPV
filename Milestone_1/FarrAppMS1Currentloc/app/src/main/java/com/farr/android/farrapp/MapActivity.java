package com.farr.android.farrapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchInput = (EditText) findViewById(R.id.search_input);
        searchType = (Spinner) findViewById(R.id.search_type);

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Intent searchIntent = new Intent(MapActivity.this, SearchResultActivity.class);
                    searchIntent.putExtra(Consts.MESSAGE_SEARCH_INPUT, searchInput.getText());
                    searchIntent.putExtra(Consts.MESSAGE_SEARCH_TYPE, searchType.getSelectedItem().toString());
                    startActivity(searchIntent);
                    return true;
                }
                return false;
            }
        });

        searchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 5:
                        //start activity on selection of any item you want, here I am assuming first item.
                        Intent intent = new Intent(MapActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setMyLocationEnabled(true);
        LatLng coffeeTree = new LatLng(-2.8992, -79.0153 );
        googleMap.addMarker(new MarkerOptions().position(coffeeTree).title("Coffee Tree"));

        LatLng cinemaCafe = new LatLng(-2.8892, -79.0133 );
        googleMap.addMarker(new MarkerOptions().position(cinemaCafe).title("Cinema Cafe"));

        LatLng prohibidoCC = new LatLng(-2.8997, -79.0227 );
        googleMap.addMarker(new MarkerOptions().position(prohibidoCC).title("Concierto de Metallica"));

        LatLngBounds CUENCA = new LatLngBounds(
                new LatLng(-2.900128, -79.005896), new LatLng(-2.900128
                , -79.005896
        ));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CUENCA.getCenter(), 12));
    }

    EditText searchInput;
    Spinner searchType;
    MapFragment mapFragment;


}
