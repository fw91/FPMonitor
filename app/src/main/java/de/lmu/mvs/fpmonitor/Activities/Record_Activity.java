package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.R;


public class Record_Activity extends Activity
{
    private MapView map;
    private TextView tv;
    private Button btn1;
    private ProgressBar pb;

    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private float[][] mapPositions =
            {{250,37},{328,55},{356,44},{369,77},{551,32},{621,43},{311,137},{385,156},{362,243},
             {319,186},{319,206},{304,286},{404,300},{360,369},{316,362},{297,412},{356,401},
             {406,354},{427,381},{389,397}};

    private boolean started;
    private boolean queryScanResults;

    private int posCtr;

    CountDownTimer countdown = new CountDownTimer(20000, 1000)
    {
        public void onTick(long millisUntilFinished)
        {
            tv.setText("Seconds remaining: " + millisUntilFinished / 1000);
        }

        public void onFinish()
        {
            tv.setText("Record complete.\n" +
                       "Now execute Bcmon terminal (for 20 Seconds).\n" +
                       "After You're done, walk to the next Position and record again.");
            btn1.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
            queryScanResults = false;
            posCtr++;
            map.setPosition(mapPositions[posCtr][0],mapPositions[posCtr][1],map.getWidth(),map.getHeight());
            map.invalidate();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        map  = (MapView)findViewById(R.id.wlan_view);
        tv   = (TextView)findViewById(R.id.recordTV);
        btn1 = (Button)findViewById(R.id.recordBtn);
        pb   = (ProgressBar)findViewById(R.id.progress);

        mWifiManager  = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();

        started = false;
        queryScanResults = false;
        posCtr = 0;

        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ButtonOneHandler();
            }
        });

        map.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                map.setPosition(mapPositions[posCtr][0],mapPositions[posCtr][1],map.getWidth(),map.getHeight());
                map.invalidate();

                if (posCtr < mapPositions.length-1)
                {
                    posCtr++;
                }
                return false;
            }
        });
    }


    /**
     * Whenever this Activity gets resumed (or started).
     * Make sure Wifi is enabled.
     * Register Receivers.
     * Register Compass.
     */
    protected void onResume()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(true);
        }

        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        super.onResume();
    }


    /**
     * Whenever this Activity gets paused (or destroyed).
     * Disable Wifi.
     * Unregister Receivers.
     * Disable Compass.
     */
    protected void onPause()
    {
        if (mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(false);
        }

        unregisterReceiver(mWifiReceiver);

        super.onPause();
    }


    private void ButtonOneHandler()
    {
        if (!started)
        {
            tv.setText("Walk to the Position and press Record.");
            btn1.setText("Record Wifi-Scan");
            map.setPosition(mapPositions[posCtr][0],mapPositions[posCtr][1],map.getWidth(),map.getHeight());
            map.invalidate();
            started = true;
        }
        else
        {
            countdown.start();
            queryScanResults = true;
            btn1.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
        }
    }


    /**
     * A Local Wifi Receiver to handle incoming Wifi Signals.
     * It sends out a Broadcast whenever asked for it via queryScanResults, if the current Signals
     * should be stored into the Database.
     */
    class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();

            if (wifiScanList != null)
            {
                if (queryScanResults)
                {
                    Log.i("LOG","WifiScan incoming");
                }
            }
        }
    }
}
