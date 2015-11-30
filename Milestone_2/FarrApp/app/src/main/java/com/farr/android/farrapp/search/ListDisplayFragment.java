package com.farr.android.farrapp.search;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.farr.android.farrapp.R;
import com.google.android.gms.gcm.Task;

import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static org.apache.http.HttpStatus.*;

/**
 * Created by vhnvn on 11/11/15.
 */
public class ListDisplayFragment extends Fragment {
    SearchListAdapter mAdapter;
    ListView listView;
    final Handler handler = new Handler();
    double currentLat = 0, currentLng = 0;

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


    public void setResult(double currentLat, double currentLng, final ArrayList<Item> result) {
        this.currentLat = currentLat;
        this.currentLng = currentLng;
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
        ImageView iconView;
        TextView line_1;
        TextView line_2;
        TextView line_3;
        ImageDownloaderTask lazyIconDownloader;
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
                holder.iconView = (ImageView) row.findViewById(R.id.thumbImage);
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
            if(info.icon!=null){
                if(holder.lazyIconDownloader!=null){
                    holder.lazyIconDownloader.cancel(false);
                }
                holder.lazyIconDownloader = new ImageDownloaderTask(holder.iconView,position);
                holder.lazyIconDownloader.execute(info.icon);
            }else{
                Drawable placeholder = getContext().getResources().getDrawable(android.R.drawable.picture_frame);
                holder.iconView.setImageDrawable(placeholder);
            }
            holder.line_1.setText(info.name);
            holder.line_2.setText("Category: " + info.category);
            holder.line_3.setText("Distance: " + info.getDistance(currentLat,currentLng));
            return row;
        }

    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private int position;

        public ImageDownloaderTask(ImageView imageView,int position) {
            this.imageView = imageView;
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
                return;
            }

            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Drawable placeholder = imageView.getContext().getResources().getDrawable(android.R.drawable.picture_frame);
                    imageView.setImageDrawable(placeholder);
                }
            }
        }
    }

    static HashMap<String, Bitmap> mDownloadIconCache = new HashMap<>();
    private Bitmap downloadBitmap(String url) {
        if(mDownloadIconCache.containsKey(url)) return mDownloadIconCache.get(url);
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != SC_OK) return null;
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mDownloadIconCache.put(url,bitmap);
                return bitmap;
            }
        } catch (Exception e) {
            urlConnection.disconnect();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
