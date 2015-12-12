package com.farr.android.farrapp.search;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Item {
    public Item(){

    }
    public Item(JSONObject r){
        try {
            lat = r.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            lon = r.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            String iconStr = r.getString("icon");
            if(iconStr!=null && iconStr.length()>0) icon = iconStr;
            id = r.getString("id");
            name = r.getString("name");
            vicinity = r.getString("vicinity");
            JSONArray arr = r.getJSONArray("types");
            category = "";
            if(arr!=null){
                for(int i=0, ie=arr.length();i<ie;i++){
                    if(category.length()>0) category += ", ";
                    category += arr.getString(i);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public Item(String name, String category, String description, double lat, double lon){
        this.name = name;
        this.category = category;
        this.lat = lat;
        this.lon = lon;
        this.vicinity = description;
    }
    String getDistance(double targetLat, double targetLon){
        float [] dist = new float[1];
        Location.distanceBetween(lat,lon,targetLat,targetLon,dist);
        return String.format("%.2f km",dist[0]/1000);
    }
    public LatLng location(){
        return new LatLng(lat,lon);
    }
    public  String name = "";
    public  String category = "";
    public  String icon = "file://android_asset/heart.png";
    public  String id = "";
    public  String vicinity = "";
    public Date date = new Date();
    public double lat = 0;
    public double lon = 0;
}
