package fi.action.wpoint;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;


public class HotSpot extends ItemizedOverlay<OverlayItem> {
    
    private ArrayList<OverlayItem> spots;
    private Context                context;

    
    public HotSpot(Drawable icon, Context context) {
        super(boundCenterBottom(icon));
        this.context = context;
        this.spots   = new ArrayList<OverlayItem>();
    }

    public void add(OverlayItem overlay) {
        spots.add(overlay);
        populate();
    }

    protected boolean onTap(int index) {
        OverlayItem item = spots.get(index);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(item.getTitle());
        // dialog.setMessage(item.getSnippet());
        dialog.setPositiveButton(R.string.connect,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // TODO
                }
            }
        );
        dialog.show();
        return true;
    }

    protected OverlayItem createItem(int i) {
        return spots.get(i);
    }

    public int size() {
        return spots.size();
    }

}