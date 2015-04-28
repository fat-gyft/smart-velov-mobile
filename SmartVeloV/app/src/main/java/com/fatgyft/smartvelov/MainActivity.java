package com.fatgyft.smartvelov;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {


    final static double MAP_DEFAULT_LATITUDE = 45.7527;
    final static double MAP_DEFAULT_LONGITUDE = 4.8494;


    private ProgressDialog pd;

    private MapView mapView;
    private IMapController mapController;
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private Location currentLocation;

    private ItemizedIconOverlay currentLocationOverlay;
    private ResourceProxy resourceProxy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        resourceProxy = new ResourceProxyImpl(getApplicationContext());
        locationManager = (LocationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        mapController = this.mapView.getController();

        mapController.setZoom(20);


        currentLocation = defineLocation();
        Drawable currentLocationMarker = this.getResources().getDrawable(R.drawable.marker);
        ArrayList<GeoPoint> currentLocationList = new ArrayList<GeoPoint>();
        currentLocationList.add(new GeoPoint(currentLocation));
        display_markers(currentLocationList , currentLocationMarker, "Current Location", "This is my location");

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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (pd != null)
            pd.dismiss();
    }

    private Location defineLocation() {
        Location location = null;
//Is it better to use another method to get the current location??
        for (String provider : locationManager.getProviders(true)) {
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                //location.setLatitude(MAP_DEFAULT_LATITUDE);
                //location.setLongitude(MAP_DEFAULT_LONGITUDE);
                locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                mapController.setCenter(new GeoPoint(location));
                Toast.makeText(getApplicationContext(), "Current Location Accuracy : " + location.getAccuracy(),
                        Toast.LENGTH_LONG).show();
                break;
            }
        }


        if (location == null) {
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MAP_DEFAULT_LATITUDE);
            location.setLongitude(MAP_DEFAULT_LONGITUDE);
            mapController.setCenter(new GeoPoint(location));
            Toast.makeText(getApplicationContext(), "Failed  to get current location, please turn on the GPS",
                    Toast.LENGTH_LONG).show();
            mapController.setZoom(12);
        }
        return location;
    }

    public void display_markers(ArrayList<GeoPoint> geoPoints, Drawable myCurrentLocationMarker, String title, String desc){

        OverlayItem myLocationOverlayItem = null;
        for (GeoPoint g : geoPoints) {
            myLocationOverlayItem = new OverlayItem(title, desc, g);
        }

        //myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.marker);

        Bitmap bitmap = ((BitmapDrawable) myCurrentLocationMarker).getBitmap();
        // Scale it to 50 x 50
        myCurrentLocationMarker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));

        myLocationOverlayItem.setMarker(myCurrentLocationMarker);

        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(myLocationOverlayItem);

        currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().add(this.currentLocationOverlay);

    }


    public class MyLocationListener implements LocationListener {


        public void onLocationChanged(Location location) {

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
