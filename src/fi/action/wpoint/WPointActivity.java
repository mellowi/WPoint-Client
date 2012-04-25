package fi.action.wpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;


public class WPointActivity extends MapActivity implements LocationListener {

    public WifiManager        wifiManager;
    public Location           currentLocation;
    private MapView           mapView;
    private MyLocationOverlay myLocationOverlay;
    private LocationManager   locationManager;
    private BroadcastReceiver scanReceiver;
    private boolean           originalWifiState;
    private ProgressDialog dialog;

    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI
        setContentView(R.layout.main);
        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.waiting));
        dialog.show();
        
        // HTTP requests should really be run on a separate thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        // Wifi
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        originalWifiState = wifiManager.isWifiEnabled();

        // Map view
        mapView = (MapView) findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(mapView.getMaxZoomLevel()-1);
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocationOverlay);
        mapView.postInvalidate();
        
        // Location manager
        currentLocation = null;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)  {
        switch (item.getItemId()) {
            case R.id.scan:
                Toast.makeText(this, R.string.scanning , Toast.LENGTH_LONG).show();
                wifiManager.startScan();
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void onResume() {
        super.onResume();
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        showLocation(currentLocation);
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            1000,   // polling time in milliseconds
            1,      // change required in meters
            this
        );
        myLocationOverlay.enableMyLocation();
        if (scanReceiver == null) {
            scanReceiver = new ScanReceiver(this);
            registerReceiver(
               scanReceiver,
               new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            );
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    public void onPause() {
        super.onPause();
        wifiManager.setWifiEnabled(originalWifiState);
        locationManager.removeUpdates(this);
        myLocationOverlay.disableMyLocation();
        if (scanReceiver != null) {
            unregisterReceiver(scanReceiver);
            scanReceiver = null;
        }
    }

    public void onStop() {
        super.onStop();
        wifiManager.setWifiEnabled(originalWifiState);
        if (scanReceiver != null) {
            unregisterReceiver(scanReceiver);
            scanReceiver = null;
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
            .setMessage(R.string.gps_disabled)
            .setCancelable(false)
            .setPositiveButton(R.string.goto_settings,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                     }
                 }
            );
        alertDialogBuilder.setNegativeButton(R.string.exit,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    System.exit(0);
                }
            }
        );
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    
    public void onLocationChanged(Location location) {
        Log.d("WPoint", "Updating location.");
        dialog.hide();
        currentLocation = location;
        
        // Update user's current location on map
        showLocation(location);
        
        // Send request to API
        String response = null;
        try {
            response = HttpConnector.executeHttpGet(
                "http://wpoint.herokuapp.com/api/v1/spots.json?" + 
                "latitude=" + location.getLatitude() +
                "&longitude=" + location.getLongitude()
            );
        }
        catch (Exception e) {
            // TODO: Display error message that failed to connect the server
            e.printStackTrace();
        }

        // Set icon for hotspots
        Drawable icon = getResources().getDrawable(R.drawable.blue_dot);
        HotspotsOverlay spotOverlays = new HotspotsOverlay(icon, this);
        
        // Parse response JSON
        try {
            JSONArray array = new JSONArray(response);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject hotspot = array.getJSONObject(i);
                
                if (!hotspot.getBoolean("open")) {
                    continue;
                }
                JSONObject coordinates = hotspot.getJSONObject("location");
                String ssid            = hotspot.getString("ssid");
                Double latitude        = coordinates.getDouble("lat");
                Double longitude       = coordinates.getDouble("lng");

                GeoPoint hotspotLocation = new GeoPoint(
                    (int)(latitude*1E6),
                    (int)(longitude*1E6)
                );
                OverlayItem overlayitem = new OverlayItem(
                    hotspotLocation,
                    ssid,
                    "signal: n dbm"
                );
                spotOverlays.add(overlayitem);
            }
            mapView.getOverlays().add(spotOverlays);
        }
        catch (JSONException e) {
            // TODO: Display error message that failed to parse the response
            e.printStackTrace();
        }
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    protected boolean isRouteDisplayed() {
        return false;
    }
    
    private void showLocation(Location location) {
        if (location == null) {
            return;
        }
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        GeoPoint currentLocationGeoPoint = new GeoPoint(
            (int)(currentLatitude * 1E6),
            (int)(currentLongitude * 1E6)
        );
        mapView.getController().animateTo(currentLocationGeoPoint);
    }
}