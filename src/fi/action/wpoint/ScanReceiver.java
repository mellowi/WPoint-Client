package fi.action.wpoint;


import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ScanReceiver extends BroadcastReceiver {
	WPointActivity wPoint;

	public ScanReceiver(WPointActivity wPoint) {
		super();
		this.wPoint = wPoint;
	}

	@Override
	public void onReceive(Context c, Intent intent) {
		Log.d("WPoint", "onReceive()");
		List<ScanResult> results = wPoint.wifi.getScanResults();
		ScanResult bestSignal = null;
		for (ScanResult result : results) {
			if (bestSignal == null
					|| WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
				bestSignal = result;
		}
		String message = String.format("%s networks found. %s is the strongest.",
		results.size(), bestSignal.SSID);		
		Log.d("WPoint", "onReceive() message: " + message);
	}
}