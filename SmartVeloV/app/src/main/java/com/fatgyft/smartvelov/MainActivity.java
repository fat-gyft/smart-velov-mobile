package com.fatgyft.smartvelov;

import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;


public class MainActivity extends ActionBarActivity {


    final static double MAP_DEFAULT_LATITUDE = 45.7527;
    final static double MAP_DEFAULT_LONGITUDE = 4.8494;


    private ProgressDialog pd;

    private MapView mapView;
    private IMapController mapController;
    private LocationManager locationManager;
    private MyLocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        locationManager = (LocationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        mapController = this.mapView.getController();
        //mapController.setCenter(new GeoPoint(45.7527,4.8494));
        mapController.setZoom(20);


        defineLocation();

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

    private void defineLocation() {
        Location location = null;

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
        }

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
