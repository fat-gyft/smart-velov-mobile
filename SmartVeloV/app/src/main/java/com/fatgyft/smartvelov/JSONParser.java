package com.fatgyft.smartvelov;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Felipe on 28-Apr-15.
 */
public class JSONParser {

    private static final String VELOV_POST_NUMBER = "number";
    private static final String VELOV_POST_NAME = "name";
    private static final String VELOV_POST_ADDRESS = "address";
    private static final String VELOV_POST_LATITUDE = "latitude";
    private static final String VELOV_POST_LONGITUDE = "longitude";



    public void getJSONFromURL(String url){

        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpPost httppost = new HttpPost(url);
        // Depends on your web service
        httppost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            // Oops
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }

        try {
            JSONObject jObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    public ArrayList<VeloVStation> parseVELOVPostes(JSONArray velovStations){

        ArrayList<VeloVStation> veloVStationsList = new ArrayList<VeloVStation>();

        try {

            System.out.println(velovStations);

            // looping through All velov stations
            for(int i = 0; i < velovStations.length(); i++) {

                JSONObject n = velovStations.getJSONObject(i);

                Integer number = n.getInt(VELOV_POST_NUMBER);
                String name = n.getString(VELOV_POST_NAME);
                String address = n.getString(VELOV_POST_ADDRESS);
                Double latitude = n.getDouble(VELOV_POST_LATITUDE);
                Double longitude = n.getDouble(VELOV_POST_LONGITUDE);

                veloVStationsList.add(new VeloVStation(number,name,address,latitude,longitude));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return veloVStationsList;
    }
}
