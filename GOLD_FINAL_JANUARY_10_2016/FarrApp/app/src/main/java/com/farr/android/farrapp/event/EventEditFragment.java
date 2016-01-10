package com.farr.android.farrapp.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.farr.android.farrapp.R;
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
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class EventEditFragment extends Fragment implements OnMapReadyCallback, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    interface EventEditDone
    {
        void onEditDone();
    }

    private EventEditDone doneListener;

    public void setDoneListener(EventEditDone doneListener){
        this.doneListener = doneListener;
    }


    Item mItem;
    View rootView;
    MapFragment mapFragment;
    private static final String TIME_PATTERN = "HH:mm";

    TextInputLayout mEditName;
    TextInputLayout mEditCategory;
    TextInputLayout mEditDescription;
    TextInputLayout mEditDate,mEditTime;
    TextInputLayout mEditTag;
    GoogleMap googleMap;

    private Calendar calendar;
    private DateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public EventEditFragment(){}
    public void setItem(Item item){
        mItem = item;
        if(mItem==null){
            mItem = new Item();
        }
        updateViews();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapFragment = (MapFragment)getChildFragmentManager().findFragmentById(R.id.mapEdit);
        if(mapFragment==null){
            mapFragment = MapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.mapEdit,mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        updateViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView!=null){
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }else {
            try {
                rootView = inflater.inflate(R.layout.fragment_event_edit, container, false);
            } catch (InflateException e) {

            }
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, null);

        calendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());

        mEditName = (TextInputLayout) rootView.findViewById(R.id.name);
        mEditCategory = (TextInputLayout) rootView.findViewById(R.id.category);
        mEditDate = (TextInputLayout) rootView.findViewById(R.id.date);
        mEditTime = (TextInputLayout) rootView.findViewById(R.id.time);
        mEditDescription = (TextInputLayout) rootView.findViewById(R.id.description);
        mEditTag = (TextInputLayout) rootView.findViewById(R.id.tags);

        rootView.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject event;
                if(mItem.id!=null){
                    event = ParseObject.createWithoutData("Places",mItem.id);
                }else{
                    event = ParseObject.create("Places");
                }
                event.put("owner", ParseUser.getCurrentUser());
                event.put("name", mEditName.getEditText().getText().toString());
                event.put("category", mEditCategory.getEditText().getText().toString());
                event.put("location", new ParseGeoPoint(mItem.lat, mItem.lon));
                event.put("description", mEditDescription.getEditText().getText().toString());
                event.put("event_date", mItem.date);
                event.put("name_seach", mEditTag.getEditText().getText().toString());
                event.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e!=null) Log.e("Save",e.toString());
                        if(doneListener!=null) doneListener.onEditDone();
                    }
                });
            }
        });

        rootView.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ParseObject event;
                if(mItem.id!=null){
                    event = ParseObject.createWithoutData("Places",mItem.id);
                    event.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e!=null) Log.e("Delete", e.toString());
                            if(doneListener!=null) doneListener.onEditDone();
                        }
                    });
                }else{
                    if(doneListener!=null) doneListener.onEditDone();
                }
            }
        });

        mEditDate.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    new DatePickerDialog(getActivity(), EventEditFragment.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mEditTime.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    new TimePickerDialog(getActivity(), EventEditFragment.this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });


        Log.d("OWNER", "/onViewCreated");
        updateViews();
    }

    void updateViews(){
        if(getActivity()==null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("OWNER", "updateViews");
                if (mItem != null) {
                    if (mEditName != null) {
                        mEditName.getEditText().setText(mItem.name);
                        Log.d("OWNER", "-> Name " + mItem.name);
                    }
                    if (mEditCategory != null) mEditCategory.getEditText().setText(mItem.category);
                    if (mEditTag != null) mEditTag.getEditText().setText(mItem.tags);
                    if (mEditDescription != null) mEditDescription.getEditText().setText(mItem.description);
                    if (calendar != null) {
                        calendar.setTime(mItem.date);
                        updateTime();
                    }
                }
                updateMap(true);
            }
        });
    }

    void updateTime(){
        if(getActivity()==null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mEditDate != null) {
                    mEditDate.getEditText().setText(dateFormat.format(calendar.getTime()));
                    if (mEditDate.getEditText().hasFocus()) {
                        mEditDate.getEditText().clearFocus();
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditDate.getEditText().getWindowToken(), 0);
                    }
                }
                if (mEditTime != null) {
                    mEditTime.getEditText().setText(timeFormat.format(calendar.getTime()));
                    if (mEditTime.getEditText().hasFocus()) {
                        mEditTime.getEditText().clearFocus();
                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditTime.getEditText().getWindowToken(), 0);
                    }
                }
                if (calendar != null && mItem != null) {
                    mItem.date = calendar.getTime();
                }
            }
        });
    }

    void updateMap(boolean centerMap){
        if(getActivity()==null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (googleMap != null) {
                    googleMap.clear();
                    if (mItem != null) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(mItem.location()));
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder().include(mItem.location()).build(), 200);
                        googleMap.moveCamera(cameraUpdate);
                        googleMap.animateCamera(cameraUpdate, 1000, null);
                    }
                }
            }
        }
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updateMap( true );

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mItem.lat = latLng.latitude;
                mItem.lon = latLng.longitude;
                updateMap( false );

            }
        });
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(-2.896617,
                        -79.007621));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);

        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        updateTime();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        updateTime();
    }
}
