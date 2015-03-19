package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.lmu.mvs.fpmonitor.R;


public class Testing_Activity extends Activity implements SensorEventListener
{
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private String receivedWifi[];

    private ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        list = (ListView)findViewById(R.id.listView1);

        /*
        map = (MapView)findViewById(R.id.wlan_view);

        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.i("COORDINATES","X=" + event.getX() + " Y=" + event.getY());
                return false;
            }
        });*/

        activateWifiScan();

        //mWifiManager.startScan();
    }


    protected void onPause()
    {
        mSensorManager.unregisterListener(this);
        unregisterReceiver(mWifiReceiver);

        super.onPause();
    }


    protected void onResume()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(true);
        }

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        super.onResume();
    }


    @Override
    public void onSensorChanged(SensorEvent event)
    {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            Log.i("SENSOR", "ACCELEROMETER CALLED," + event.toString());
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            Log.i("SENSOR", "MAGNETIC FIELD CALLED," + event.toString());

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // not in use
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
     *
     */
    class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();

            Log.i("SCANRESULTS","SCANRESULTS RECEIVED");

            receivedWifi = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++)
            {
                receivedWifi[i] = "MAC: " + wifiScanList.get(i).BSSID +
                                "\nSignal: " + (100+wifiScanList.get(i).level) + "%\n";
            }

            list.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, receivedWifi));
        }
    }
}
