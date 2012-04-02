package fi.action.wpoint;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class WPointActivity extends MapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // layout
        setContentView(R.layout.main);
        
        // zooming
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        // spots
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
        Spot spotOverlay = new Spot(drawable, this);
        
        // test
        // N+ E+, Helsinki about 60 15N 25 30E 
        GeoPoint point = new GeoPoint(60150000,25030000);
        OverlayItem overlayitem = new OverlayItem(point, "Morjes!", "Helsinki City!");
        
        // load spots
        spotOverlay.add(overlayitem);
        mapOverlays.add(spotOverlay);
        
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}