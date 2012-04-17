package fi.action.wpoint;

import java.util.List;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;


public class ScanReceiver extends BroadcastReceiver {

    WPointActivity wPoint;
    ScanResult     best;

    public ScanReceiver(WPointActivity wPoint) {
        super();
        this.wPoint = wPoint;
        best = null;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        Log.d("WPoint", "onReceive()");
        List<ScanResult> results = wPoint.wifiManager.getScanResults();
        best = null;
        for (ScanResult result : results) {
            if (best == null
                            || WifiManager.compareSignalLevel(best.level,
                                            result.level) < 0) best = result;
        }
        String message = String.format(
                        "%s networks found. %s is the strongest.",
                        results.size(), best.SSID);
        Log.d("WPoint", "onReceive() message: " + message);
        if (best != null) connectToDialog(best.SSID);
    }

    private void connectToBest() {
        if (best == null) return;
        WifiConfiguration WifiConf = new WifiConfiguration();
        WifiConf.SSID = '\"' + best.SSID + '\"';
        WifiConf.hiddenSSID = true;
        WifiConf.status = WifiConfiguration.Status.ENABLED;
        WifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        WifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        WifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        WifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        WifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
        WifiConf.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
        int res = wPoint.wifiManager.addNetwork(WifiConf);
        Log.d("WPoint WIFI", "add Network returned " + res);
        boolean b = wPoint.wifiManager.enableNetwork(res, true);
        Log.d("WPoint WIFI", "enableNetwork returned " + b);
    }

    public void connectToDialog(String hotspot) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(wPoint);
        alertDialogBuilder
                        .setMessage(R.string.ssid + hotspot)
                        .setCancelable(true)
                        .setPositiveButton(R.string.connect,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                connectToBest();
                                            }
                                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}