package com.fatgyft.smartvelov;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fatgyft.smartvelov.decoder.JSONParser;
import com.fatgyft.smartvelov.path.Path;
import com.fatgyft.smartvelov.request.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {


    final static double MAP_DEFAULT_LATITUDE = 45.7527;
    final static double MAP_DEFAULT_LONGITUDE = 4.8494;


    private ProgressDialog pd;
    private Vibrator vibrator;

    private JSONParser jsonParser;

    private MapView mapView;
    private IMapController mapController;
    private RoadManager roadManager;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private Location currentLocation;

    private ArrayList<VeloVStation> velovStations;
    private ArrayList<Path> paths;

    private ItemizedIconOverlay currentLocationOverlay;
    private ResourceProxy resourceProxy;

    private Drawable currentLocationMarker;
    private ImageButton showCurrentLcationBtn;

    private ArrayList<Marker> markersInTheMap;
    private  boolean followLocationIsTrue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator)getSystemService(getApplicationContext().VIBRATOR_SERVICE);

        mapView = (MapView) this.findViewById(R.id.mapview);
        showCurrentLcationBtn = (ImageButton) this.findViewById(R.id.centerOnLocation);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        resourceProxy = new ResourceProxyImpl(getApplicationContext());
        locationManager = (LocationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        roadManager = new OSRMRoadManager();
        mapController = this.mapView.getController();
        mapController.setZoom(20);

        jsonParser = new JSONParser();
        paths = new ArrayList<Path>();
        markersInTheMap = new ArrayList<Marker>();


        velovStations = getVelovStations();

        Drawable velovMarker = this.getResources().getDrawable(R.drawable.stationmarker);

        for( VeloVStation v : velovStations){
            display_markers(new GeoPoint(v.getLatitude(),v.getLongitude()) , velovMarker, v.getName(),
                    v.getAddress());
        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        currentLocation = defineLocation();
        currentLocationMarker = this.getResources().getDrawable(R.drawable.marker);
        display_markers(new GeoPoint(currentLocation) , currentLocationMarker, getResources().getString(R.string.currentLocation),
                getResources().getString(R.string.currentLocationDesc));

        mapController.setCenter(new GeoPoint(this.defineLocation()));

        new getRouteAsyncTask().execute();

        View.OnLongClickListener listener = new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if(followLocationIsTrue==false) {
                    vibrator.vibrate(100);
                    mapController.setCenter(new GeoPoint(defineLocation()));
                    mapView.getOverlays().remove(currentLocationOverlay);
                    currentLocationMarker = getResources().getDrawable(R.drawable.marker_red);
                    display_markers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                            getResources().getString(R.string.currentLocationDesc));
                    showCurrentLcationBtn.setImageResource(R.drawable.currentlocation_red);
                    followLocationIsTrue = true;
                    mapView.invalidate();

                }else{
                    vibrator.vibrate(100);
                    followLocationIsTrue = false;
                    currentLocationMarker = getResources().getDrawable(R.drawable.marker);
                    mapView.getOverlays().remove(currentLocationOverlay);
                    display_markers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                            getResources().getString(R.string.currentLocationDesc));
                    showCurrentLcationBtn.setImageResource(R.drawable.currentlocation);
                    mapView.invalidate();
                }
                return true;
            }
        };

        showCurrentLcationBtn.setOnLongClickListener(listener);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_bluetooth){
            
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("on stop");
        if (pd != null)
            pd.dismiss();
        if (locationManager != null)
            System.out.println("desactive updates");
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("on restart");
        if (locationManager != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private Location defineLocation() {
        Location location = null;
//Is it better to use another method to get the current location??
        for (String provider : locationManager.getProviders(true)) {
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                //mapController.setCenter(new GeoPoint(location));
                //Toast.makeText(getApplicationContext(), getResources().getString(R.string.currentLocationAcc) + location.getAccuracy(),
                //        Toast.LENGTH_LONG).show();
                break;
            }
        }


        if (location == null) {
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MAP_DEFAULT_LATITUDE);
            location.setLongitude(MAP_DEFAULT_LONGITUDE);
            //mapController.setCenter(new GeoPoint(location));
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.currentLocationFailed),
                    Toast.LENGTH_LONG).show();
            mapController.setZoom(13);
        }
        return location;
    }

    public void display_markers(GeoPoint geoPoint, Drawable myCurrentLocationMarker, String title, String desc){

        OverlayItem myLocationOverlayItem = null;
        myLocationOverlayItem = new OverlayItem(title, desc, geoPoint);

        myLocationOverlayItem.setMarker(changeIconSize(myCurrentLocationMarker, 60));

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(myLocationOverlayItem);

        currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        clearMarkers();
                        displayPopUp(new GeoPoint(item.getPoint().getLatitude(), item.getPoint().getLongitude()), item.getTitle());
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().add(this.currentLocationOverlay);

    }

    public ArrayList<VeloVStation> getVelovStations(){

        ArrayList<VeloVStation> veloVStations = null;

        try {
            String jsonString = loadJSONFromAsset();
            JSONArray jsonArray = new JSONArray(jsonString);
            veloVStations = jsonParser.parseVELOVPostes(jsonArray);

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return veloVStations;

    }

    public void displayPopUp(GeoPoint startPoint, String title){

        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle(title);
        mapView.getOverlays().add(startMarker);
        markersInTheMap.add(startMarker);

    }

    private void clearMarkers(){
        for(Marker m : markersInTheMap)
            mapView.getOverlays().remove(m);
    }

    public String loadJSONFromAsset() {

        String json = "";

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("lyon.json")));
            String line;
            while((line = reader.readLine()) != null){
                json += line;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }


    public void onClick(View view){
        switch(view.getId()){
            case R.id.centerOnLocation:
                vibrator.vibrate(50);
                mapController.setCenter(new GeoPoint(defineLocation()));
                mapView.getOverlays().remove(currentLocationOverlay);
                display_markers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                        getResources().getString(R.string.currentLocationDesc));
                mapView.invalidate();
                drawPath();
                break;

        }
    }

    public Drawable changeIconSize(Drawable d, int size){

        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        // Scale it to size x size
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, size, size, true));

        return d;
    }

    public void drawPath(){

        Road road;

        int color=Color.BLUE;

        for(Path p: paths) {
            if (p.getPoints_encoded()) {
               road = roadManager.getRoad(p.getPoints());
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
                roadOverlay.setColor(color++);
                mapView.getOverlays().add(roadOverlay);
                mapView.invalidate();
            }
        }

    }



    public class MyLocationListener implements LocationListener {


        public void onLocationChanged(Location location) {

            mapView.getOverlays().remove(currentLocationOverlay);

            display_markers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                    getResources().getString(R.string.currentLocationDesc));

            if(followLocationIsTrue){
                mapController.setCenter(new GeoPoint(location));
            }

        }

        public void onProviderDisabled(String provider) {

            Toast.makeText(getApplicationContext(), "Turn on the gps",
                    Toast.LENGTH_LONG).show();

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private class getRouteAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;
        private String json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress = new ProgressDialog(MainActivity.this);
                    progress.setMessage("Trying to get json");
                    progress.show();
                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ServiceHandler serviceHandler = new ServiceHandler();
            json = serviceHandler.makeServiceCall("http://private-5543d-smartvelov.apiary-mock.com/route", ServiceHandler.GET);

            try {
                JSONObject jsonObject = new JSONObject(json);
                paths = jsonParser.parsePath(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
