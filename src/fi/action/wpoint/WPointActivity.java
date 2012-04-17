package fi.action.wpoint;

import java.util.List;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;


public class WPointActivity extends MapActivity {

    private MapView           map;
    private MyLocationOverlay myLocationOverlay;
    private LocationManager   locationManager;
    private Button            scanButton;

    WifiManager               wifiManager;
    BroadcastReceiver         receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.main);
        map = (MapView) findViewById(R.id.MapView);
        map.setBuiltInZoomControls(true); // zooming

        // GPS & WiFi on? - not working properly (needs to check in some kind of
        // listener)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // / WIFI THINGS (still under dev)
        // Get WiFi status
        WifiInfo info = wifiManager.getConnectionInfo();
        Log.d("WPoint", "WiFi Status: " + info.toString());
        // List available networks (empty on emulator?)
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            Log.d("WPoint", config.toString());
        }
        // Register Broadcast Receiver
        if (receiver == null) {
            receiver = new ScanReceiver(this);
            registerReceiver(receiver, new IntentFilter(
                            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }

        // Show current location indicator
        myLocationOverlay = new MyLocationOverlay(this, map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);

        map.getController().setZoom(19);

        // Center the map to the user's location
        myLocationOverlay.runOnFirstFix(new Runnable() {

            public void run() {
                map.getController()
                                .animateTo(myLocationOverlay.getMyLocation());
            }
        });

        // spots
        List<Overlay> mapOverlays = map.getOverlays();
        Drawable drawable = this.getResources()
                        .getDrawable(R.drawable.blue_dot);
        Spot spotOverlay = new Spot(drawable, this);

        // test
        // N+ E+, Helsinki about 60 15N 25 30E
        GeoPoint point = getPoint(60.17, 24.94);
        OverlayItem overlayitem = new OverlayItem(point,
                        "SSID: Tonnikalapurkki (50dB)", "");

        // load spots
        spotOverlay.add(overlayitem);
        mapOverlays.add(spotOverlay);

        // button
        scanButton = (Button) findViewById(R.id.ButtonScan);
        scanButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Log.d("WPoint", "onClick() wifi.startScan()");
                wifiManager.startScan();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        if (receiver == null) {
            receiver = new ScanReceiver(this);
            registerReceiver(receiver, new IntentFilter(
                            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        checkPreconditions();
    }

    @Override
    public void onPause() {
        super.onPause();
        myLocationOverlay.disableCompass();
        myLocationOverlay.disableMyLocation();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private GeoPoint getPoint(double lat, double lon) {
        return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
    }

    private void checkPreconditions() {
        // WiFi (tested - cant be on with emulator)
        /*
         * if(!wifi.isWifiEnabled()) { showWiFiDisabledAlertToUser(); return; }
         */
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlertToUser();
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                        .setMessage(R.string.gps_disabled)
                        .setCancelable(false)
                        .setPositiveButton(R.string.goto_settings,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                Intent callGPSSettingIntent = new Intent(
                                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                startActivity(callGPSSettingIntent);
                                            }
                                        });
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

    private void showWiFiDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                        .setMessage(R.string.wifi_disabled)
                        .setCancelable(false)
                        .setPositiveButton(R.string.goto_settings,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                Intent callGPSSettingIntent = new Intent(
                                                                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                                                startActivity(callGPSSettingIntent);
                                            }
                                        });
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
}