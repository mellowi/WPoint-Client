package fi.action.wpoint;

import java.util.List;
import android.app.AlertDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;


public class WPointActivity extends MapActivity implements LocationListener {
    private MapView           mapView;
    private MyLocationOverlay locationOverlay;
    private LocationManager   locationManager;
    private Button            scanButton;
    private MapController     mapController;
    public  WifiManager       wifiManager;
    public  BroadcastReceiver scanReceiver;
    boolean originalWifiState;

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wifi
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        originalWifiState = wifiManager.isWifiEnabled();
        
        // Map view
        setContentView(R.layout.main);
        mapView = (MapView)findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        List<Overlay> mapOverlays = mapView.getOverlays();

        // Location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 30, this);
        
        // Current location indicator
        locationOverlay = new MyLocationOverlay(this, mapView);
        mapOverlays.add(locationOverlay);
        mapController.setZoom(19);
        
        // TODO: Read hotspots via JSON API
        // ...
        
        GeoPoint hotspotLocation = new GeoPoint((int)(60.17*1E6), (int)(24.97*1E6));
        OverlayItem overlayitem = new OverlayItem(hotspotLocation, "SSID: Tonnikalapurkki (50dB)", "");
        
        // Draw spots
        Drawable drawable = this.getResources().getDrawable(R.drawable.blue_dot);
        Spot spotOverlay = new Spot(drawable, this);
        spotOverlay.add(overlayitem);
        mapOverlays.add(spotOverlay);

        // TODO: Scan hotspots every x minutes
        // ...
        

        
        // Scan button
        scanButton = (Button)findViewById(R.id.ButtonScan);
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
        locationOverlay.enableCompass();
        locationOverlay.enableMyLocation();
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
        locationOverlay.disableCompass();
        locationOverlay.disableMyLocation();
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
            });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    
    public void onLocationChanged(Location location) {
        int latitude = (int)(location.getLatitude() * 1E6);
        int longitude = (int)(location.getLongitude() * 1E6);
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        mapController.animateTo(currentLocation);
        mapView.invalidate();
    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }


    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
}