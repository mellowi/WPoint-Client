package fi.action.wpoint;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private WPointActivity   wPoint;
    private ScanResult       bestHotspot;
    private List<ScanResult> scanResults;

    
    public ScanReceiver(WPointActivity wPoint) {
        super();
        this.wPoint      = wPoint;
        this.bestHotspot = null;
    }

    
    public void onReceive(Context c, Intent intent) {
        bestHotspot = null;
 
        scanResults = wPoint.wifiManager.getScanResults();
        if (scanResults.size() == 0) {
            return;
        }
        
        JSONObject jsonPayload = new JSONObject();
        try {
            JSONArray jsonResultsArray = new JSONArray();
            for (ScanResult result : scanResults) {
                boolean isOpen = false;
                if (!result.capabilities.contains("WPA") &&
                    !result.capabilities.contains("WEP")) {
                    isOpen = true;
                }
    
                JSONObject jsonHotspotHash = new JSONObject();
                jsonHotspotHash.put("ssid",  result.SSID);
                jsonHotspotHash.put("bssid", result.BSSID);
                jsonHotspotHash.put("dbm",   result.level);
                jsonHotspotHash.put("open",  isOpen);
                jsonResultsArray.put(jsonHotspotHash);
    
                // decide the best
                if (bestHotspot == null ||
                    WifiManager.compareSignalLevel(bestHotspot.level, result.level) < 0) {
                    bestHotspot = result;
                }
            }

            jsonPayload.put("latitude", wPoint.currentLatitude);
            jsonPayload.put("longitude", wPoint.currentLongitude);
            jsonPayload.put("results", jsonResultsArray);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("WPoint", "Sending: " + jsonPayload.toString());
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("data", jsonPayload.toString()));
        String response = null;
        try {
           response = HttpConnector.executeHttpPost(
                           "http://wpoint.herokuapp.com/api/v1/report.json",
                           postParameters
                      );
        }
        catch (Exception e) {
            // TODO: Try again n times
            e.printStackTrace();
        }
        
        Log.d("WPoint", "Received: " + response);
        
        if (bestHotspot != null) {
            // connectToDialog(bestHotspot.SSID);
        }
    }

    private void connectToBest() {
        if (bestHotspot == null) {
            return;
        }
        WifiConfiguration WifiConf = new WifiConfiguration();
        WifiConf.SSID = '\"' + bestHotspot.SSID + '\"';
        WifiConf.hiddenSSID = true;
        WifiConf.status = WifiConfiguration.Status.ENABLED;
        WifiConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        WifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        WifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        WifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        WifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        WifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
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
                    public void onClick(DialogInterface dialog,int id) {
                        connectToBest();
                    }
                }
            );
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}