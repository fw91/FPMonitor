package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import de.lmu.mvs.fpmonitor.R;


public class CommonFP_Activity extends Activity
{
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    String wifis[];
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_fingerprinting);

        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();

        list = (ListView)findViewById(R.id.listView1);

        wifiManager.startScan();
    }

    protected void onPause()
    {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    protected void onResume()
    {
        registerReceiver(wifiReceiver, new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }



    class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            wifis = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++)
            {
                wifis[i] = ((wifiScanList.get(i)).toString());
            }

            list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1,wifis));
        }
    }
}
