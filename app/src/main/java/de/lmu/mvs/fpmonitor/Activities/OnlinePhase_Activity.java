package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.lmu.mvs.fpmonitor.Compass;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.MyDistanceReasoner;
import de.lmu.mvs.fpmonitor.R;

/**
 * This Activity will show Your current Position on a floor Plan.
 * It is necessary to Record Your Database first, so there is data to evaluate.
 */
public class OnlinePhase_Activity extends Activity
{
    MapView map;

    WifiManager mWifiManager;
    WifiReceiver mWifiReceiver;

    MyDistanceReasoner myDistanceReasoner;

    DatabaseHandler DH;

    Compass compass;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlinephase);

        map                = (MapView)findViewById(R.id.wlan_view);

        mWifiManager       = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver      = new WifiReceiver();

        myDistanceReasoner = new MyDistanceReasoner();

        compass            = new Compass(getApplicationContext());

        DH                 = new DatabaseHandler(this);

        activateWifiScan();
    }


    protected void onPause()
    {
        if (mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(false);
        }
        unregisterReceiver(mWifiReceiver);
        compass.start();
        super.onPause();
    }


    protected void onResume()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(true);
        }

        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        compass.stop();

        super.onResume();
    }


    /**
     * Scans for available WifiNetworks in a period of 5 Seconds (5000ms)
     */
    private void activateWifiScan()
    {
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                mWifiManager.startScan();
            }
        },0,5000);
    }


    /**
     * A Local WifiReceiver for handling incoming Wifi-Signals.
     */
    class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();

            if (wifiScanList != null)
            {
                PointF mapPos = myDistanceReasoner.getPosition(DH.getProbFingerprints(compass.getDir()), wifiScanList);
                map.setPosition(mapPos.x, mapPos.y, map.getWidth(), map.getHeight());
                map.invalidate();
            }
        }
    }
}
