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
	private MapView map;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // layout
        setContentView(R.layout.main);
        map = (MapView) findViewById(R.id.mapview);
        
        // position Helsinki
        map.getController().setCenter(getPoint(60.17, 24.94));
        map.getController().setZoom(13);
        
        map.setBuiltInZoomControls(true); // zooming
        
        // spots
        List<Overlay> mapOverlays = map.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
        Spot spotOverlay = new Spot(drawable, this);
        
        // test
        // N+ E+, Helsinki about 60 15N 25 30E 
        GeoPoint point = getPoint(60.17, 24.94);
        OverlayItem overlayitem = new OverlayItem(point, "Morjes!", "Helsinki City!");
        
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
}