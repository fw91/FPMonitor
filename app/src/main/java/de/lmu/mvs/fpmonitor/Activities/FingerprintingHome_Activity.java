package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.lmu.mvs.fpmonitor.Compass;
import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;
import de.lmu.mvs.fpmonitor.DistanceReasoner;
import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.R;

/**
 * This Activity will show Your current Position on a floor Plan.
 * It is necessary to Record Your Database first, so there is data to evaluate.
 */
public class FingerprintingHome_Activity extends Activity
{
    final static String COMPASS_UPDATE = "compass_updated";

    MapView map;

    WifiManager mWifiManager;
    WifiReceiver mWifiReceiver;

    DistanceReasoner mDistanceReasoner;

    DatabaseHandler DH;

    Compass compass;
    String direction;

    BroadcastReceiver compassReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            direction = intent.getStringExtra("direction");
        }
    };

    ArrayList<Fingerprint> myFPs, northFPs, eastFPs, southFPs, westFPs;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fphome);

        map = (MapView)findViewById(R.id.wlan_view);

        mWifiManager      = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver     = new WifiReceiver();

        mDistanceReasoner = new DistanceReasoner();

        DH                = new DatabaseHandler(getApplicationContext());

        compass           = new Compass(getApplicationContext());

        direction = "North";

        if (initFPsDB())
        {
            Toast.makeText(getApplicationContext(),"Load complete.",Toast.LENGTH_SHORT).show();
            activateWifiScan();
        }

        //myFPs = initFPs();

        //activateWifiScan();
    }


    protected void onPause()
    {
        if (mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(false);
        }
        unregisterReceiver(mWifiReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(compassReceiver);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(compassReceiver, new IntentFilter(COMPASS_UPDATE));
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


    private ArrayList<Fingerprint> initFPs()
    {
        ArrayList<Fingerprint> testList = new ArrayList<>();

        ArrayList<APInfo> fp1ap = new ArrayList<>();
        ArrayList<APInfo> fp2ap = new ArrayList<>();

        APInfo fp11 = new APInfo("c0:25:06:3d:25:d9",-71);
        APInfo fp12 = new APInfo("88:25:2c:98:02:57",-86);
        APInfo fp21 = new APInfo("c0:25:06:3d:25:d9",-65);
        APInfo fp22 = new APInfo("88:25:2c:98:02:57",-86);

        fp1ap.add(fp11);
        fp1ap.add(fp12);
        fp2ap.add(fp21);
        fp2ap.add(fp22);

        Fingerprint fp1 = new Fingerprint(57,85,"temp",fp1ap);
        Fingerprint fp2 = new Fingerprint(123,85,"temp",fp2ap);

        testList.add(fp1);
        testList.add(fp2);

        return testList;
    }

    private boolean initFPsDB()
    {
        northFPs = DH.getNorthFPs();
        eastFPs  = DH.getEastFPs();
        southFPs = DH.getSouthFPs();
        westFPs  = DH.getWestFPs();


        Log.i("NORTH-FPS", northFPs.toString());
        Log.i("EAST-FPS",eastFPs.toString());
        Log.i("SOUTH-FPS",southFPs.toString());
        Log.i("WEST-FPS",westFPs.toString());


        return true;
    }


    class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();

            if (wifiScanList != null)
            {
                Fingerprint f;

                switch (direction)
                {
                    case "North":
                        f = mDistanceReasoner.compareClosest(northFPs, wifiScanList);
                        map.setPosition(f.x_coordinate,f.y_coordinate,map.getWidth(),map.getHeight());
                        map.invalidate();
                        break;
                    case "East":
                        f = mDistanceReasoner.compareClosest(eastFPs, wifiScanList);
                        map.setPosition(f.x_coordinate,f.y_coordinate,map.getWidth(),map.getHeight());
                        map.invalidate();
                        break;
                    case "South":
                        f = mDistanceReasoner.compareClosest(southFPs, wifiScanList);
                        map.setPosition(f.x_coordinate,f.y_coordinate,map.getWidth(),map.getHeight());
                        map.invalidate();
                        break;
                    case "West":
                        f = mDistanceReasoner.compareClosest(westFPs, wifiScanList);
                        map.setPosition(f.x_coordinate,f.y_coordinate,map.getWidth(),map.getHeight());
                        map.invalidate();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
