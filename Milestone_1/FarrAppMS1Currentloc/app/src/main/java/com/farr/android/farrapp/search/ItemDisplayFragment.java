package com.farr.android.farrapp.search;

import android.app.Fragment;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.farr.android.farrapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.w3c.dom.Text;

import java.util.Map;

public class ItemDisplayFragment extends Fragment implements OnMapReadyCallback {
    public ItemDisplayFragment(){

    }

    public void setItem(Item item){
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
            if(txtLine2!=null) txtLine1.setText("Category: "+mItem.category);
            if(txtLine3!=null) txtLine1.setText("Distance: "+mItem.getDistance());
        }
    }

    View rootView;
    Item mItem;
    TextView txtLine1, txtLine2, txtLine3;
    MapFragment mapFragment;

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
