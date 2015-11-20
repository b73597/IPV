package com.farr.android.farrapp.search;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.farr.android.farrapp.R;

import java.util.ArrayList;

/**
 * Created by vhnvn on 11/11/15.
 */
public class ListDisplayFragment extends Fragment {
    SearchListAdapter mAdapter;
    ListView listView;
    final Handler handler = new Handler();

    public static interface OnItemClickListener
    {
        public void onItemClick(Item item);
    }

    OnItemClickListener mListener;

    public ListDisplayFragment(){

    }
    public void setItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_result, container, false);
        listView = (ListView)rootView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mListener!=null) mListener.onItemClick(mAdapter.getItem(position));
            }
        });
        if(mAdapter!=null) listView.setAdapter(mAdapter);
        return rootView;
    }


    public void setResult(final ArrayList<Item> result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapter == null) {
                    mAdapter = new SearchListAdapter(getActivity(), R.layout.list_result_item);
                    listView.setAdapter(mAdapter);
                }
                mAdapter.clear();
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public static class InfoViewHolder
    {
        TextView line_1;
        TextView line_2;
        TextView line_3;
    }

    class SearchListAdapter extends ArrayAdapter<Item >
    {
        Context mContext;
        int mResID;
        public SearchListAdapter(Context context, int res_id){
            super(context, res_id);
            mResID = res_id;
            mContext = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = convertView;
            InfoViewHolder holder = new InfoViewHolder();

            if(row == null)
            {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                row = inflater.inflate(mResID, parent, false);
                holder.line_1 = (TextView) row.findViewById(R.id.first_line);
                holder.line_2 = (TextView) row.findViewById(R.id.second_line);
                holder.line_3 = (TextView) row.findViewById(R.id.third_line);
                row.setTag(holder);
            }
            else
            {
                holder = (InfoViewHolder) row.getTag();
            }
            Item info = getItem(position);
            holder.line_1.setText(info.name);
            holder.line_2.setText("Category: " + info.category);
            holder.line_3.setText("Distance: " + info.getDistance());
            return row;
        }

    }
}
