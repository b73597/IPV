package com.farr.android.farrapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.farr.android.farrapp.search.Item;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class CalendarFragment extends Fragment {
    ProgressDialog progressDialog;
    ArrayList<Item> mItems;
    ArrayList<ParseObject> mParseItems;
    ListView listView;
    CalendarListAdapter mAdapter;
    HashSet<String> mRelation;

    View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mRootView==null) {
            mRootView = inflater.inflate(R.layout.test_calendar_layout, container, false);
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(listView.CHOICE_MODE_MULTIPLE);
        progressDialog = ProgressDialog.show(getActivity(), "Searching...", "Populating data... Please wait...");

        mAdapter = new CalendarListAdapter(getActivity(),R.layout.calendar_menu_item);
        listView.setAdapter(mAdapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Places");
        //query.whereNear("location", new ParseGeoPoint(currentLat, currentLng));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(),"No results",Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    mItems = new ArrayList<Item>();
                    mParseItems = new ArrayList<ParseObject>();
                    for (ParseObject obj : objects) {
                        ParseGeoPoint point = obj.getParseGeoPoint("location");
                        Item item = new Item(obj.getString("name"), obj.getString("category"), obj.getString("description"), point.getLatitude(), point.getLongitude());
                        item.date = obj.getDate("event_date");
                        item.vicinity = obj.getString("description") + " | " + DateFormat.format("MM-dd hh:mm",item.date);
                        try {
                            item.joincount = obj.getRelation("join").getQuery().count();
                        } catch (ParseException e1) {
                        }
                        mItems.add(item);
                        mParseItems.add(obj);
                    }

                    mRelation = new HashSet<String>();
                    try {
                        List<ParseObject> joins = ParseUser.getCurrentUser().getRelation("join").getQuery().find();
                        for(ParseObject obj : joins) mRelation.add(obj.getObjectId());
                    } catch (ParseException e1) {
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();
                            mAdapter.addAll(mItems);
                            mAdapter.notifyDataSetChanged();
                            progressDialog.hide();
                        }
                    });
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRelation.contains(mParseItems.get(position).getObjectId())) {
                    ParseUser.getCurrentUser().getRelation("join").remove(mParseItems.get(position));
                    mRelation.remove(mParseItems.get(position).getObjectId());
                    ParseUser.getCurrentUser().saveInBackground();

                    mParseItems.get(position).getRelation("join").remove(ParseUser.getCurrentUser());
                    mParseItems.get(position).saveInBackground();

                    mAdapter.getItem(position).joincount--;
                } else {
                    ParseUser.getCurrentUser().getRelation("join").add(mParseItems.get(position));
                    mRelation.add(mParseItems.get(position).getObjectId());
                    ParseUser.getCurrentUser().saveInBackground();

                    mParseItems.get(position).getRelation("join").add(ParseUser.getCurrentUser());
                    mParseItems.get(position).saveInBackground();

                    mAdapter.getItem(position).joincount++;

                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra("title", mItems.get(position).name);
                    intent.putExtra("description", mItems.get(position).vicinity);
                    intent.putExtra("beginTime", mItems.get(position).date);
                    startActivity(intent);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    class CalendarListAdapter extends ArrayAdapter<Item>
    {
        Context mContext;
        int mResource;
        public CalendarListAdapter(Context context, int resource) {
            super(context, resource);
            mContext = context;
            mResource = resource;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = convertView;

            if(row == null)
            {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                row = inflater.inflate(mResource, parent, false);
            }
            Item info = getItem(position);
            CheckedTextView tv = (CheckedTextView) row.findViewById(android.R.id.text1);
            tv.setText(info.name + " ("+ info.joincount +" will join)", TextView.BufferType.SPANNABLE);
            ((TextView) row.findViewById(android.R.id.text2)).setText(info.vicinity);
            Spannable str = (Spannable) tv.getText();
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setChecked( mRelation.contains(mParseItems.get(position).getObjectId()) );
            return row;
        }
    }
}