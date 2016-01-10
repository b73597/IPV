package com.farr.android.farrapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String httpGET(String urlString) {
        String response = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) stringBuffer.append(line);
            response = stringBuffer.toString();
            bufferedReader.close();
        } catch (Exception e) {
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try{
                httpURLConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }


    /**
     * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     */
    static class GooglePolyPointBuffer
    {
        String buffer;
        int index;
        int lat, lng;
        public GooglePolyPointBuffer(String buffer){
            this.buffer = buffer;
            this.index = 0;
            lat = lng = 0;
        }
        Integer nextCoord(){
            if(index>=buffer.length()) return null;
            int b, shift = 0, result = 0;
            do {
                b = buffer.charAt(index++) - 63;
                result |= (b&0x1f) << shift;
                shift += 5;
            } while (index < buffer.length() && b >= 0x20);
            return (result&1)==0 ? (result>>1) : ~(result>>1);
        }
        LatLng nextPoint(){
            if(index>=buffer.length()) return null;
            Integer dLat = nextCoord();
            Integer dLng = nextCoord();
            if(dLat==null || dLng==null) return null;
            lat += dLat; lng += dLng;
            return new LatLng(1e-5*lat,1e-5*lng);
        }
    }
    public static List<LatLng> decodeGooglePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        GooglePolyPointBuffer buf = new GooglePolyPointBuffer(encoded);
        LatLng point;
        for(;;){
            point = buf.nextPoint();
            if(point==null) break;
            poly.add(point);
        }
        return poly;
    }
}
