package com.farr.android.farrapp.search;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.farr.android.farrapp.Consts;
import com.farr.android.farrapp.R;
import com.farr.android.farrapp.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemDisplayFragment extends Fragment implements OnMapReadyCallback {

    public ItemDisplayFragment(){

    }

    public void setItem(double currentLat, double currentLng, Item item){
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        mItem = item;
        updateViews();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.fragment_search_details, container, false);
        }catch(InflateException e){

        }
        txtLine1 = (TextView) rootView.findViewById(R.id.line1);
        txtLine2 = (TextView) rootView.findViewById(R.id.line2);
        txtLine3 = (TextView) rootView.findViewById(R.id.line3);
        rootView.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItem!=null) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hang out now!");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Let's hang out here http://maps.google.com/maps?z=12&t=m&q=loc:" + mItem.lat + "+" + mItem.lon);
                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                }
            }
        });
        updateViews();
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapFragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        if(mapFragment==null){
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.map,mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        updateViews();
    }
    void updateViews(){
        if(mItem!=null){
            if(txtLine1!=null) txtLine1.setText(mItem.name);
            if(txtLine2!=null) txtLine2.setText("Category: "+mItem.category);
            if(txtLine3!=null) txtLine3.setText("Distance: "+mItem.getDistance(currentLat,currentLng));
            if(googleMap!=null) {
                if(mCurrentRouteTask!=null)
                    mCurrentRouteTask.cancel(true);
                mCurrentRouteTask = new RouteTask(currentLat, currentLng, mItem.lat, mItem.lon);
                mCurrentRouteTask.execute();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        showMapNavigation();
    }

    void showMapNavigation() {
        updateViews();
    }

    class RouteTask extends AsyncTask<Object, Object, String> {
        String urlString;

        public RouteTask(double srcLat, double srcLng, double dstLat, double dstLng) {
            urlString = new StringBuilder()
                    .append("https://maps.googleapis.com/maps/api/directions/json")
                    .append("?origin=").append(srcLat).append(",").append(srcLng)
                    .append("&destination=").append(dstLat).append(",").append(dstLng)
                    .append("&sensor=false&mode=driving&alternatives=true")
                    .append("&key=").append(Consts.GOOGLE_MAPS_BROWSER_API_KEY).toString();
        }

        @Override
        protected void onPreExecute() {
            if(mRoutingDialog==null){
                mRoutingDialog = ProgressDialog.show(getActivity(), "Routing...", "Finding route...", false);
            }else{
                mRoutingDialog.show();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return Utils.httpGET(urlString);
        }

        @Override
        protected void onPostExecute(String response) {
            mRoutingDialog.hide();
            if(isCancelled()) return;
            try {
                JSONObject root = new JSONObject(response);
                JSONArray routesJSON = root.getJSONArray("routes");
                googleMap.clear();


                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(currentLat,currentLng))
                        .title("Current Location"));

                LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();

                for (int i = 0, ie = routesJSON.length(); i < ie; i++) {
                    JSONObject route = routesJSON.getJSONObject(i);
                    JSONObject overviewPolylines = route.getJSONObject("overview_polyline");
                    String pointsStringEncoded = overviewPolylines.getString("points");
                    List<LatLng> points = Utils.decodeGooglePoly(pointsStringEncoded);
                    if(points.size()>0) {
                        googleMap.addPolyline(new PolylineOptions()
                                        .color(Color.parseColor("#05b1fb")).width(13)
                                        .addAll(points)
                                        .geodesic(true)
                        );

                        googleMap.addMarker(new MarkerOptions()
                                .position(points.get(points.size() - 1))
                                .title(mItem.name));

                        for (LatLng point : points) boundBuilder.include(point);
                        break;//display only one route
                    }
                }

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 200);
                googleMap.moveCamera(cameraUpdate);
                googleMap.animateCamera(cameraUpdate, 1000, null);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }

    ProgressDialog mRoutingDialog;
    RouteTask mCurrentRouteTask;
    View rootView;
    Item mItem;
    TextView txtLine1, txtLine2, txtLine3;
    MapFragment mapFragment;
    GoogleMap googleMap;
    double currentLat, currentLng;
}
