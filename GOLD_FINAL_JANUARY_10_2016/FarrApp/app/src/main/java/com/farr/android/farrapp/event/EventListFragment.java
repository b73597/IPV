package com.farr.android.farrapp.event;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.farr.android.farrapp.R;
import com.farr.android.farrapp.search.Item;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class EventListFragment extends Fragment {

    ListView listView;
    EventListAdapter mAdapter;

    public static interface OnItemClickListener
    {
        public void onItemClick(Item item);
    }

    OnItemClickListener mListener;
    public void setItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public EventListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.event_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(listView!=null){
            reloadList();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mListener != null) {
                        Item item = mAdapter.getItem(position);
                        mListener.onItemClick(item);
                    }
                }
            });
        }
    }

    void reloadList(){
        ParseQuery.getQuery("Places").whereEqualTo("owner", ParseUser.getCurrentUser()).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    Toast.makeText(getActivity(),"Something got wrong",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }else{
                    ArrayList<Item> listItems = new ArrayList<Item>();
                    for(ParseObject obj : objects){
                        ParseGeoPoint point = obj.getParseGeoPoint("location");
                        Item item = new Item(obj.getString("name"), obj.getString("category"), obj.getString("description"), point.getLatitude(), point.getLongitude());
                        item.vicinity = obj.getString("description") + " | "+obj.getDate("event_date");
                        item.id = obj.getObjectId();
                        item.tags = obj.getString("name_search");
                        item.date = obj.getDate("event_date");
                        item.description = obj.getString("description");

                        listItems.add(item);
                    }
                    final ArrayList<Item> listItemsFinal = listItems;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter == null) {
                                mAdapter = new EventListAdapter(getActivity(), R.layout.list_result_item);
                                listView.setAdapter(mAdapter);
                            }
                            mAdapter.clear();
                            mAdapter.addAll(listItemsFinal);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public static class InfoViewHolder
    {
        ImageView iconView;
        TextView line_1;
        TextView line_2;
        TextView line_3;
        TextView line_4;
        ImageDownloaderTask lazyIconDownloader;
    }

    class EventListAdapter extends ArrayAdapter<Item >
    {
        Context mContext;
        int mResID;
        public EventListAdapter(Context context, int res_id){
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
                holder.line_4 = (TextView) row.findViewById(R.id.fourth_line);
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
            holder.line_3.setText("");
            holder.line_4.setText("Description: " + info.vicinity);
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
        if(url.startsWith("file://")){
            try {
                InputStream inputStream = new URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mDownloadIconCache.put(url, bitmap);
                return bitmap;
            } catch (IOException e) {
                return null;
            }
        }else {
            HttpURLConnection urlConnection = null;
            try {
                URL uri = new URL(url);
                urlConnection = (HttpURLConnection) uri.openConnection();
                int statusCode = urlConnection.getResponseCode();
                if (statusCode != SC_OK) return null;
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    mDownloadIconCache.put(url, bitmap);
                    return bitmap;
                }
            } catch (Exception e) {
                urlConnection.disconnect();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        return null;
    }
}
