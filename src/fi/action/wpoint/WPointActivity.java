package fi.action.wpoint;

import java.util.List;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;


public class WPointActivity extends MapActivity implements LocationListener {

    public MapView            mapView;
    private MyLocationOverlay myLocationOverlay;
    private LocationManager   locationManager;
    private Button            scanButton;
    public WifiManager        wifiManager;
    public BroadcastReceiver  scanReceiver;
    public List<Overlay>      mapOverlays;
    public double             currentLatitude;
    public double             currentLongitude;
    boolean                   originalWifiState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // HTTP requests should otherwise be run on separate thread (Honeycomb)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        // Wifi
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        originalWifiState = wifiManager.isWifiEnabled();

        // Map view
        setContentView(R.layout.main);
        mapView = (MapView) findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(mapView.getMaxZoomLevel()-1);
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocationOverlay);
        mapView.postInvalidate();
        
        // Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Scan button
        scanButton = (Button) findViewById(R.id.ButtonScan);
        scanButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Log.d("WPoint", "onClick() wifi.startScan()");
                wifiManager.startScan();
            }
        });
    }

    public void onResume() {
        super.onResume();
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        showLocation(lastLocation);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
              mapView.getController().setCenter(myLocationOverlay.getMyLocation());
            }
          });
        
        if (scanReceiver == null) {
            scanReceiver = new ScanReceiver(this);
            registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
        alertDialogBuilder.setMessage(R.string.gps_disabled)
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
        showLocation(location);
        
        String response = "";
        try {
            response = CustomHttpClient.executeHttpGet(
               "http://wpoint.herokuapp.com/api/v1/spots.json?" + 
                "latitude=" + location.getLatitude() +
                "&longitude=" + location.getLongitude());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        Log.d("WPoint", response);

        // TODO: Parse data from response
        // ...
        
        // TODO: Draw hotspots on map
        // ...
        
        /*
        // Mock
        GeoPoint hotspotLocation = new GeoPoint((int)(60.17*1E6), (int)(24.97*1E6));
        OverlayItem overlayitem = new OverlayItem(hotspotLocation, "SSID: Tonnikalapurkki (50dB)", "");
        
        // Draw spots
        Drawable drawable = wPoint.getResources().getDrawable(R.drawable.blue_dot);
        HotSpot spotOverlay = new HotSpot(drawable, wPoint);
        spotOverlay.add(overlayitem);
        wPoint.mapOverlays.add(spotOverlay);*/
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
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        mapView.getController().animateTo(new GeoPoint((int)(currentLatitude * 1E6), (int)(currentLongitude * 1E6)));
    }
}