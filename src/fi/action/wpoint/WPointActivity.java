package fi.action.wpoint;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class WPointActivity extends MapActivity {
	private MapView map;
	private LocationManager locationManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // layout
        setContentView(R.layout.main);
        
        // GPS & WiFi on?
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
        	showDisabledAlertToUser();
        }
        
        map = (MapView) findViewById(R.id.MapView);
        
        // position Helsinki
        map.getController().setCenter(getPoint(60.17, 24.94));
        map.getController().setZoom(13);
        
        map.setBuiltInZoomControls(true); // zooming
                
        // spots
        List<Overlay> mapOverlays = map.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.blue_dot);
        Spot spotOverlay = new Spot(drawable, this);
        
        // test
        // N+ E+, Helsinki about 60 15N 25 30E 
        GeoPoint point = getPoint(60.17, 24.94);
        OverlayItem overlayitem = new OverlayItem(point, "SSID: Tonnikalapurkki (50dB)", "[Connect]");
        
        // load spots
        spotOverlay.add(overlayitem);
        mapOverlays.add(spotOverlay);
        
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    private GeoPoint getPoint(double lat, double lon) {
        return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
    }
    
	private void showDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("GPS or/and WiFi are disabled in your device. Would you like to enable it?")
		.setCancelable(false)
		.setPositiveButton("Goto Settings Page",
		new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				Intent callGPSSettingIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(callGPSSettingIntent);
			}
		});
		alertDialogBuilder.setNegativeButton("Exit",
		new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				System.exit(0);
			}
		});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
}