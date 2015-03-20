package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;
import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.R;

/**
 * This Activity is used for creating a Radio Map.
 * It shows a floor plan with key Positions on it where the recording has to take place.
 * The Record-Button collects all necessary data and stores it into the Database.
 * The Next-Button moves to the next Position.
 */
public class RadioMapHome_Activity extends Activity
{
    MapView map;
    TextView tv;
    Button btn1, btn2, btn3, btn4;

    private float positions[][];
    private int posCtr;

    private boolean recorded;
    private boolean started;
    private boolean wifiReceived;

    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private Fingerprint currentFP;
    private ArrayList<APInfo> currentAPList;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radiomaphome);

        // Initialize Layout
        map      = (MapView)findViewById(R.id.wlan_view);
        tv       = (TextView)findViewById(R.id.recordTV);
        btn1     = (Button)findViewById(R.id.recordBtn);
        btn2     = (Button)findViewById(R.id.nextBtn);
        btn3     = (Button)findViewById(R.id.showDB);
        btn4     = (Button)findViewById(R.id.deleteDB);

        btn1.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
        tv.setText("Press the Button to start recording.");

        // Initialize Coordinates
        positions = initCoordinates();
        posCtr    = 0;

        // Initialize Flags
        recorded     = true;
        started      = false;
        wifiReceived = false;

        // Initialize Wifi
        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();

        // Set OnClickListeners
        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ButtonOneHandler();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ButtonTwoHandler();
            }
        });

        btn4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clearDatabaseDialog();
            }
        });


        /*
         * A way of getting all the coordinates needed for the Database
         *
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("Coords","X="+event.getX()+" Y="+event.getY());
                map.setPosition(event.getX(),event.getY(),map.getWidth(),map.getHeight());
                map.invalidate();
                return false;
            }
        });
         *
         */
    }


    /**
     * Whenever this Activity gets resumed (or started).
     * Make sure Wifi is enabled.
     * Register Receivers.
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
     * Unregister Receivers.
     */
    protected void onPause()
    {
        unregisterReceiver(mWifiReceiver);

        super.onPause();
    }


    /**
     * Handle the events for Button1.
     * Gather information required via recordFingerprint() and store into the Database.
     */
    private void ButtonOneHandler()
    {
        // TODO implement Fingerprint-Recording-Function
        // recordFingerprint();

        Toast.makeText(getApplicationContext(),"Fingerprint Recorded",Toast.LENGTH_SHORT).show();
        tv.setText("Press Next.");
        recorded = true;
    }


    /**
     * Handle the events for Button2.
     * On Activity Start: "Start Recording" - Start recording procedure
     * Otherwise        : "Next" - Update Position on the Map.
     * On Finish        : Finish recording procedure
     */
    private void ButtonTwoHandler()
    {
        if (!started)
        {
            btn1.setVisibility(View.VISIBLE);
            btn2.setText("Next");
            btn3.setVisibility(View.GONE);
            btn4.setVisibility(View.GONE);
            tv.setText("Walk to the Position and press Record.");
            started = true;

            map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
            map.invalidate();

            posCtr++;
            recorded = false;
        }
        else
        {
            if (!recorded)
            {
                Toast.makeText(getApplicationContext(),"Please record the FP",Toast.LENGTH_SHORT).show();
            }
            else
            {
                map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                map.invalidate();

                if (posCtr==positions.length-1)
                {
                    btn1.setVisibility(View.GONE);
                    btn2.setVisibility(View.GONE);
                    //btn3.setVisibility(View.VISIBLE);
                    btn4.setVisibility(View.VISIBLE);

                    map.setPosition(0,0,map.getWidth(),map.getHeight());
                    map.invalidate();

                    tv.setText("Recording finished.");
                }
                else
                {
                    posCtr++;
                    recorded = false;
                    tv.setText("Walk to the Position and press Record.");
                }
            }
        }
    }


    /**
     * Initialize the set of Coordinates to be printed on the Map.
     * @return an Array of Coordinates
     */
    private float[][] initCoordinates()
    {
        float[][] homeCoordinates =
                {{57,85},{123,85},{57,139},{123,139},                         // Room1
                 {41,203},{107,203},{41,255},{107,255},                       // Room2
                 {254,33},                                                    // Room3
                 {220,83},{220,136},{220,184},                                // Room4 etc.
                 {177,249},
                 {236,249},
                 {308,50},{381,50},{308,125},{381,125},
                 {456,58},{456,120},
                 {302,203},{376,203},{445,203},{302,252},{376,252},{445,252}};

        return homeCoordinates;
    }


    /**
     * Get Wifi-Data and store the Fingerprint.
     * @return true, if record was successful | false, if record failed
     */
    private boolean recordFingerprint()
    {
        mWifiManager.startScan();

        if (wifiReceived)
        {
            int x = (int)positions[posCtr][0];
            int y = (int)positions[posCtr][1];
            int d = 0;

            currentFP = new Fingerprint(x,y,d,currentAPList);

            //DH.saveFingerprint(currentFP);
        }
        else
        {
            return false;
        }

        wifiReceived = false;

        return true;
    }



    /**
     * AlertDialog for clearing the DB.
     */
    private void clearDatabaseDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Clear Fingerprint Database?");
        builder.setMessage("You will lose all stored Fingerprints!");
        builder.setPositiveButton("Yes", new Dialog.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //DH.clearDB();
                dialog.cancel();
                Toast.makeText(getApplicationContext(),"Database cleared",Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNegativeButton("Cancel", new Dialog.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
    }


    /**
     * A Local Wifi Receiver to handle incoming Wifi Signals.
     * It retrieves the Data and extracts incoming MAC/RSSI-Information and automatically puts it
     * into the Database.
     */
    class WifiReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();

            if (wifiScanList != null)
            {
                wifiReceived = true;

                ArrayList<APInfo> mAPList = new ArrayList<>();

                for(int i = 0; i < wifiScanList.size(); i++)
                {
                    APInfo mAP = new APInfo(wifiScanList.get(i).BSSID, wifiScanList.get(i).level);
                    mAPList.add(mAP);
                }
                currentAPList = mAPList;
            }
        }
    }
}