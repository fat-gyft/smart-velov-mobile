package com.fatgyft.smartvelov;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fatgyft.smartvelov.bluetooth.BluetoothArduinoService;
import com.fatgyft.smartvelov.bluetooth.Constants;
import com.fatgyft.smartvelov.decoder.JSONParser;
import com.fatgyft.smartvelov.path.Instruction;
import com.fatgyft.smartvelov.path.InstructionPoint;
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
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends ActionBarActivity {


    final static double MAP_DEFAULT_LATITUDE = 45.7527;
    final static double MAP_DEFAULT_LONGITUDE = 4.8494;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    private Vibrator vibrator;

    private SearchManager searchManager;
    private SearchView searchView;

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
    private TextView accuracyText;

    private ArrayList<ItemizedIconOverlay> velovStationsOverlayList;
    private ItemizedIconOverlay searchedLocationPinOverlay;

    private ArrayList<Marker> markersInTheMap;
    private ArrayList<InstructionPoint> instructionPointList;
    private ArrayList<Polyline> roadOverlays;
    private  boolean followLocationIsTrue;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    private boolean bluetoothActivatedByApp = false;

    private MenuItem bluetoothMenuItem;

    private boolean deviceConnected = false;

    BluetoothArduinoService bluetoothService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);


        vibrator = (Vibrator)getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        mapView = (MapView) this.findViewById(R.id.mapview);
        showCurrentLcationBtn = (ImageButton) this.findViewById(R.id.centerOnLocation);
        accuracyText = (TextView) this.findViewById(R.id.accuracyText);
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
        velovStationsOverlayList = new ArrayList<ItemizedIconOverlay>();

        velovStations = getVelovStations();
        instructionPointList = new ArrayList<InstructionPoint>();
        roadOverlays = new ArrayList<Polyline>();

        Drawable velovMarker = this.getResources().getDrawable(R.drawable.stationmarker);

        for( VeloVStation v : velovStations){
            displayMarkers(new GeoPoint(v.getLatitude(), v.getLongitude()), velovMarker, "" + v.getNumber(),
                    v.getAddress(), "velovStation");
        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        locationManager.getBestProvider(criteria, true);

        currentLocation = defineLocation();
        currentLocationMarker = this.getResources().getDrawable(R.drawable.marker);
        displayMarkers(new GeoPoint(currentLocation), currentLocationMarker, getResources().getString(R.string.currentLocation),
                getResources().getString(R.string.currentLocationAcc) + currentLocation.getAccuracy(), "currentLocation");

        mapController.animateTo(new GeoPoint(this.defineLocation()));

        new getRouteAsyncTask().execute();

        View.OnLongClickListener listener = new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if(followLocationIsTrue==false) {
                    vibrator.vibrate(100);
                    mapController.animateTo(new GeoPoint(defineLocation()));

                    if (currentLocationOverlay!=null) {
                        mapView.getOverlays().remove(currentLocationOverlay);
                    }
                    currentLocationMarker = getResources().getDrawable(R.drawable.marker_red);
                    displayMarkers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                            getResources().getString(R.string.currentLocationAcc) + currentLocation.getAccuracy(), "currentLocation");
                    showCurrentLcationBtn.setImageResource(R.drawable.currentlocation_red);
                    followLocationIsTrue = true;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    mapView.invalidate();

                }else{
                    vibrator.vibrate(100);
                    followLocationIsTrue = false;
                    currentLocationMarker = getResources().getDrawable(R.drawable.marker);
                    if (currentLocationOverlay!=null) {
                        mapView.getOverlays().remove(currentLocationOverlay);
                    }
                    displayMarkers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                            getResources().getString(R.string.currentLocationAcc) + currentLocation.getAccuracy(), "currentLocation");

                    showCurrentLcationBtn.setImageResource(R.drawable.currentlocation);
                    locationListener.onLocationChanged(defineLocation());
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
                    mapView.invalidate();
                }
                return true;
            }
        };

        showCurrentLcationBtn.setOnLongClickListener(listener);

        //_____________________Bluetooth______________________

        //get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!mBluetoothAdapter.isEnabled()){
            bluetoothActivatedByApp=true;
            mBluetoothAdapter.enable();
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.getItem(0).setEnabled(mBluetoothAdapter.isEnabled());
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Get the SearchView and set the searchable configuration
        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new searchLocationAsyncTask().execute(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//              if (searchView.isExpanded() && TextUtils.isEmpty(newText)) {
                    System.out.println("Search text changed");
//              }
                return true;
            }

        });

        bluetoothMenuItem = menu.findItem(R.id.action_bluetooth);

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
            Log.e("Bluetooth", "Connection");
            connectBluetooth();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean connectBluetooth(){

        BluetoothAdapter mBlueAdapter = BluetoothAdapter.getDefaultAdapter();;
        BluetoothDevice mBlueRobo = null;

        Set<BluetoothDevice> paired = mBlueAdapter.getBondedDevices();
        if (paired.size() > 0) {
            for (BluetoothDevice d : paired) {
                if (d.getName().equals("HC-05")) {
                    mBlueRobo = d;
                    break;
                }
            }
        }

        //BluetoothDevice device = mBlueAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        // Initialize the BluetoothChatService to perform bluetooth connections
        bluetoothService = new BluetoothArduinoService(this, mHandler);
        bluetoothService.connect(mBlueRobo);

        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("on stop");
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

    /**
     *
     * @param geoPoint
     * @param myCurrentLocationMarker
     * @param title
     * @param desc
     */
    public void displayMarkers(GeoPoint geoPoint, Drawable myCurrentLocationMarker, final String title, final String desc, final String type){

        OverlayItem myLocationOverlayItem = null;
        myLocationOverlayItem = new OverlayItem(title, desc, geoPoint);

        myLocationOverlayItem.setMarker(changeIconSize(myCurrentLocationMarker, type));

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(myLocationOverlayItem);

        ItemizedIconOverlay itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        clearMarkers();
                        if (type.equals("velovStation")){
                            String get = "https://api.jcdecaux.com/vls/v1/stations/";
                            get+=item.getTitle();
                            get +="?contract=Lyon&apiKey=e68ce8d7e0e4089ce1dd29e42e3fff8d285001ca";
                            new getDynamicInfoVelovTask().execute(get, item.getTitle());
                        }
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().add(itemizedIconOverlay);
        saveOverlayItem(type, itemizedIconOverlay);

    }


    public void displayInstructionMarker(GeoPoint geoPoint, Drawable myCurrentLocationMarker, InstructionPoint ip){

        OverlayItem myLocationOverlayItem = null;
        myLocationOverlayItem = new OverlayItem(""+ip.getSign(), "", geoPoint);

        myLocationOverlayItem.setMarker(changeIconSize(myCurrentLocationMarker, "instruction"));

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(myLocationOverlayItem);

        ItemizedIconOverlay itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().add(itemizedIconOverlay);

        ip.setItem(itemizedIconOverlay);

    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @param startPoint
     * @param title
     */
    public void displayPopUp(GeoPoint startPoint, String title, Drawable d, String info, boolean bigIcon){

        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (bigIcon) {
            startMarker.setIcon(biggerIcon(d));
        } else {
            startMarker.setIcon(d);
        }
        startMarker.setTitle(title);
        startMarker.setSubDescription(info);
        mapView.getOverlays().add(startMarker);
        markersInTheMap.add(startMarker);
        mapView.invalidate();
    }

    /**
     *
     */
    private void clearMarkers(){
        for(Marker m : markersInTheMap) {
            m.closeInfoWindow();
            mapView.getOverlays().remove(m);
        }
        markersInTheMap.clear();
    }

    /**
     *
     * @return
     */
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
                mapController.animateTo(new GeoPoint(defineLocation()));
                if (currentLocationOverlay!=null) {
                    mapView.getOverlays().remove(currentLocationOverlay);
                }
                displayMarkers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                        getResources().getString(R.string.currentLocationAcc) + currentLocation.getAccuracy(), "currentLocation");
                mapView.invalidate();
                break;

        }
    }

    /**
     *
     * @param d
     * @param type
     * @return
     */
    public Drawable changeIconSize(Drawable d, String type){

        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        // Scale it to size x size
        if ("velovStation".equals(type)) {
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
        }else if("currentLocation".equals(type)){
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
        }else if("searchBoxLocationPin".equals(type)){
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 60, 60, true));
        }else if("interestPoint".equals(type)){
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
        }else if("instruction".equals(type)){
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 15, 15, true));
        }

        return d;
    }

    public Drawable biggerIcon(Drawable d){
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        Integer newWidth= (int)Math.round(d.getIntrinsicWidth()*1.4);
        Integer newHeight= (int)Math.round(d.getIntrinsicHeight() * 1.4);
        d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true));
        return d;
    }


    /**
     *
     */
    public void drawPath(){

        clearPaths();

        Road road;

        int j=0;
        for(Path p: paths) {
            if (p.getPoints_encoded()) {
                road = roadManager.getRoad(p.getPoints());
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
                if (j==1) {
                    roadOverlay.setColor(Color.GREEN);
                }else{
                    roadOverlay.setColor(Color.BLUE);
                }
                roadOverlays.add(roadOverlay);
                j++;
                mapView.getOverlays().add(roadOverlay);
                int i=0;
                for(Instruction instruction : p.getInstructions()){
                    ArrayList<GeoPoint> pointsSubList = new ArrayList<GeoPoint>(p.getPoints().subList(instruction.getInterval().first,instruction.getInterval().second));
                    Drawable d = getResources().getDrawable(R.drawable.instruction);
                    for(GeoPoint point: pointsSubList){
                        InstructionPoint instructionPoint = new InstructionPoint(i++,instruction.getSign(),point);
                        instructionPointList.add(instructionPoint);
                        displayInstructionMarker(point, d, instructionPoint);
                    }
                }
                mapView.zoomToBoundingBox(new BoundingBoxE6(p.getBbox().second.getLongitudeE6(),p.getBbox().second.getLatitudeE6(), p.getBbox().first.getLongitudeE6(),p.getBbox().first.getLatitudeE6()));

                signToInfo();

                mapView.invalidate();
            }
        }

    }

    public void saveOverlayItem(String type, ItemizedIconOverlay o){
        if("velovStation".equals(type)){
            velovStationsOverlayList.add(o);
        }else if("currentLocation".equals(type)){
            currentLocationOverlay=o;
        }else if("searchBoxLocationPin".equals(type)){
            searchedLocationPinOverlay=o;
        }
    }


    public void navigate(Location location){
        boolean finihedPath=false;
        for(InstructionPoint ip : instructionPointList){

            float [] f = new float[3];
            location.distanceBetween(ip.getPoint().getLatitude(),ip.getPoint().getLongitude(),location.getLatitude(),location.getLongitude(),f);
            if(f[0]<10){
                if (!ip.isOnLocationPoint()) {
                    ip.setIsOnLocationPoint(true);
                    Drawable d = getResources().getDrawable(R.drawable.instruction_red);
                    ip.getItem().getItem(0).setMarker(changeIconSize(d, "instruction"));
                    sendInstruction(ip.getSign());

                    displayInstructionToast(ip.getImage(), ip.getSignText());

                    if(ip.equals(instructionPointList.get(instructionPointList.size()-1))){
                        finihedPath=true;
                    }
                }
            }
        }

        if(finihedPath==true){
            Toast.makeText(getApplicationContext(), "You reached your destination",
                    Toast.LENGTH_LONG).show();
            clearPaths();
        }
    }

    public VeloVStation getVelovStationFromNumber(int n){
        VeloVStation veloVStation = null;
        for(VeloVStation v : velovStations){
            if(v.getNumber()==n){
                return v;
            }
        }
        return veloVStation;
    }

    public void clearPaths(){
        for(InstructionPoint ip : instructionPointList){
            mapView.getOverlays().remove(ip.getItem());
        }
        instructionPointList.clear();
        for(Polyline p : roadOverlays){
            mapView.getOverlays().remove(p);
        }
        roadOverlays.clear();
    }

    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getResources().getString(R.string.backButton))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(bluetoothActivatedByApp){
            mBluetoothAdapter.disable();
        }
        if(bluetoothService != null){
            bluetoothService.stop();
        }
    }


    //_____________________OnLocationChanged______________________

    public class MyLocationListener implements LocationListener {


        public void onLocationChanged(Location location) {

            if (currentLocationOverlay!=null) {
                mapView.getOverlays().remove(currentLocationOverlay);
            }

            displayMarkers(new GeoPoint(defineLocation()), currentLocationMarker, getResources().getString(R.string.currentLocation),
                    getResources().getString(R.string.currentLocationAcc) + currentLocation.getAccuracy(), "currentLocation");

            if(followLocationIsTrue){
                navigate(location);
                mapController.animateTo(new GeoPoint(location));
                accuracyText.setText(getResources().getString(R.string.currentLocationAcc)+
                        (double)Math.round(location.getAccuracy() * 100) / 100D);
            }else{
                accuracyText.setText("");
            }

        }

        public void onProviderDisabled(String provider) {

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.turnOnGPS),
                    Toast.LENGTH_LONG).show();

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    //_____________________getRouteAsyncTask______________________

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
                    progress.setMessage(getResources().getString(R.string.retrieveVelovStaticInfo));
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
            json = serviceHandler.makeServiceCall(ServiceHandler.ROUTE_URL, ServiceHandler.GET);

            try {
                JSONObject jsonObject = new JSONObject(json);
                paths = jsonParser.parsePath(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //_____________________searchLocationAsyncTask______________________

    private class searchLocationAsyncTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progress;
        private GeoPoint point;
        private String query;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress = new ProgressDialog(MainActivity.this);
                    progress.setMessage(getResources().getString(R.string.tryAddress));
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

                    if (point!=null) {
                        if (searchedLocationPinOverlay!=null) {
                            mapView.getOverlays().remove(searchedLocationPinOverlay);
                        }
                        Drawable d = getResources().getDrawable(R.drawable.pin);
                        displayMarkers(point, d, query, "This is my searched location", "searchBoxLocationPin");
                        String destination = ""+point.getLatitude()+","+point.getLongitude();
                        GeoPoint cLoc = new GeoPoint(defineLocation());
                        String origin = ""+cLoc.getLatitude()+","+cLoc.getLongitude();
                        new getDynamicInfoPathTask().execute(origin,destination);
                        mapView.invalidate();
                        System.out.println(query + "   " + point.getLatitude()+ " "  + point.getLongitude());
                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.unknownAddress),
                                Toast.LENGTH_LONG).show();
                    }

                }
            });
        }

        @Override
        protected Void doInBackground(String... querys) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            List<Address> addresses;
            try {
                query=querys[0];
                addresses = geocoder.getFromLocationName(query, 1);
                if(addresses.size() > 0) {
                    double latitude= addresses.get(0).getLatitude();
                    double longitude= addresses.get(0).getLongitude();
                    point= new GeoPoint(latitude,longitude);
                }else {
                    point = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //_____________________getDynamicInfoVelovTask______________________

    private class getDynamicInfoVelovTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progress;
        private String json;
        private VeloVStation v;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress = new ProgressDialog(MainActivity.this);
                    progress.setMessage(getResources().getString(R.string.retrieveVelovDynamicInfo));
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

                    Drawable d = getResources().getDrawable(R.drawable.stationmarker);

                    displayPopUp(new GeoPoint(v.getLatitude(), v.getLongitude()), v.getName(),
                            changeIconSize(d,"velovStation"),getResources().getString(R.string.availableBikes)+ v.getAvailable_bikes() +
                                    getResources().getString(R.string.availableBikeStands)+ v.getAvailable_bike_stands(), true);

                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                }
            });
        }

        @Override
        protected Void doInBackground(String... infos) {
            ServiceHandler serviceHandler = new ServiceHandler();
            json = serviceHandler.makeServiceCall(infos[0], ServiceHandler.GET);

            try {
                JSONObject jsonObject = new JSONObject(json);
                Integer velovNumber = Integer.parseInt(infos[1]);
                v = jsonParser.parseVELOVDynamicInfo(jsonObject, getVelovStationFromNumber(velovNumber));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    //_____________________getDynamicInfoPathTask______________________

    private class getDynamicInfoPathTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progress;
        private String json;
        private boolean success=true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress = new ProgressDialog(MainActivity.this);
                    progress.setMessage("Getting path to destination");
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
                    if (success==true) {
                        drawPath();
                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.errorGettingPath),
                                Toast.LENGTH_LONG).show();
                    }
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                }
            });
        }

        @Override
        protected Void doInBackground(String... infos) {
            ServiceHandler serviceHandler = new ServiceHandler();

            String origin = infos[0];
            String destination = infos[1];
            String url = "http://aurelienbertron.fr/api/route/velov/"+origin+"/"+destination;
            System.out.println(url);
            System.out.println(json);
            json = serviceHandler.makeServiceCall(url, ServiceHandler.GET);

            try {
                JSONObject jsonObject = new JSONObject(json);
                paths = jsonParser.parsePath(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
                success=false;
            }

            return null;
        }
    }

    public void sendInstruction(int id){
        int[] correspondance = {1,1,2,3,4,5,5,6,7,8};

        if(deviceConnected){
            bluetoothService.write(("" + correspondance[id + 3]).getBytes());
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = MainActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothArduinoService.STATE_CONNECTED:
                            deviceConnected = true;
                            Toast.makeText(MainActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                            bluetoothMenuItem.setEnabled(false);
                            bluetoothMenuItem.setIcon(R.drawable.ic_action_bluetooth_connected);
                            sendInstruction(4);
                            //tv.setText("\nConnected");
                            break;
                        case BluetoothArduinoService.STATE_CONNECTING:
                            //tv.setText("\nConnecting");
                            break;
                        case BluetoothArduinoService.STATE_LISTEN:
                        case BluetoothArduinoService.STATE_NONE:
                            //tv.setText("\nNot connected");
                            break;
                    }
                    break;
            }
        }
    };


    private void signToInfo(){
        for (InstructionPoint ip: instructionPointList) {
            switch (ip.getSign())
            {
                case Instruction.CONTINUE_ON_STREET:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.FINISH:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.TURN_LEFT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.TURN_SHARP_LEFT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.TURN_SLIGHT_LEFT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.TURN_RIGHT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.TURN_SHARP_RIGHT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.TURN_SLIGHT_RIGHT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.USE_ROUNDABOUT:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
                case Instruction.VIA_REACHED:
                    ip.setImage(getResources().getDrawable(R.drawable.reached_green));
                    ip.setSignText("CONTINUE_ON_STREET");
                    break;
            }
        }
    }

    public void displayInstructionToast(Drawable d, String t){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.toast_text);
        text.setText(t);
        ImageView imgView = (ImageView) layout.findViewById(R.id.toast_img);
        imgView.setImageDrawable(d);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

}
