package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.lmu.mvs.fpmonitor.Compass;
import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
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
    TextView tv, compassTv;
    Button btn1, btn2, btn3, btn4;
    ProgressBar progress;

    final static String SCAN_SUCCESSFUL = "scan_successful";
    final static String COMPASS_UPDATE = "compass_updated";

    Intent scanSuccess = new Intent(SCAN_SUCCESSFUL);

    private float positions[][];
    private int posCtr;

    private boolean started;
    private boolean queryScanResults;

    private DatabaseHandler DH;

    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private Compass compass;
    private String direction;
    private String[] directions = {"North","East","South","West"};
    private int dirCtr;

    private ArrayList<APInfo> currentAPList;


    // This is required to get up-to-date ScanResults
    BroadcastReceiver recordingReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // TODO Add some Exit-Strategy if there are no Wifi Signals to receive. Necessary??

            if (started)
            {
                progress.setVisibility(View.GONE);
                queryScanResults = false;

                if (!direction.equals(directions[dirCtr]))
                {
                    tv.setText("Record failed. Try again. \nPlease point Your Device " + directions[dirCtr] + ".");
                    btn1.setVisibility(View.VISIBLE);
                }
                else
                {
                    compassTv.setTextColor(Color.RED);
                    recordFingerprint();

                    if (dirCtr == 3)
                    {
                        dirCtr = 0;

                        tv.setText("Record finished. Press Next.");
                        btn1.setVisibility(View.GONE);
                        btn2.setVisibility(View.VISIBLE);
                        compassTv.setVisibility(View.GONE);
                    }
                    else
                    {
                        dirCtr++;

                        tv.setText("Record successful. \nPoint Your Device " + directions[dirCtr] + ".");
                        btn1.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };


    // This is required to get up-to-date CompassDirection (small delay)
    BroadcastReceiver compassReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            direction = intent.getStringExtra("direction");

            if (direction.equals(directions[dirCtr]))
            {
                compassTv.setTextColor(Color.GREEN);
            }
            else
            {
                compassTv.setTextColor(Color.RED);
            }

            compassTv.setText(direction);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radiomaphome);

        // Initialize Layout
        map       = (MapView)findViewById(R.id.wlan_view);
        tv        = (TextView)findViewById(R.id.recordTV);
        btn1      = (Button)findViewById(R.id.recordBtn);
        btn2      = (Button)findViewById(R.id.nextBtn);
        btn3      = (Button)findViewById(R.id.showDB);
        btn4      = (Button)findViewById(R.id.deleteDB);
        progress  = (ProgressBar)findViewById(R.id.progress);
        compassTv = (TextView)findViewById(R.id.compassTV);

        tv.setText("Press the Button to start recording.");
        btn1.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);

        // Initialize Coordinates
        positions = initCoordinates();
        posCtr    = 0;

        // Initialize Flags
        started          = false;
        queryScanResults = false;

        // Initialize SQLite-Database
        DH = new DatabaseHandler(getApplicationContext());

        // Initialize Wifi
        mWifiManager  = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();

        // Initialize Compass
        compass = new Compass(getApplicationContext());
        dirCtr = 0;

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


    // TODO Add onStart() and onDestroy()?? Necessary?


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
        LocalBroadcastManager.getInstance(this).registerReceiver(recordingReceiver, new IntentFilter(SCAN_SUCCESSFUL));
        LocalBroadcastManager.getInstance(this).registerReceiver(compassReceiver, new IntentFilter(COMPASS_UPDATE));

        compass.start();

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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(recordingReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(compassReceiver);

        compass.stop();

        super.onPause();
    }


    /**
     * Handle the events for Button1.
     * If record is pressed, we want to retrieve the current Signals and store them into the Database.
     * Therefore activate queryScanResults.
     */
    private void ButtonOneHandler()
    {
        if (mWifiManager.startScan())
        {
            queryScanResults = true;
        }

        tv.setText("Recording Fingerprint...");
        btn1.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
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
            tv.setText("Walk to the Position and press Record. \nPoint Your Device North.");
            btn1.setVisibility(View.VISIBLE);
            btn2.setText("Next");
            btn2.setVisibility(View.GONE);
            btn3.setVisibility(View.GONE);
            btn4.setVisibility(View.GONE);
            compassTv.setVisibility(View.VISIBLE);

            map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
            map.invalidate();

            started = true;
        }
        else
        {
            if (posCtr==positions.length-1)
            {
                map.setPosition(0,0,map.getWidth(),map.getHeight());
                map.invalidate();

                tv.setText("Recording finished.");
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                // TODO Add an Activity for showing Database on the Device? Alternative: Export DB?
                //btn3.setVisibility(View.VISIBLE);
                btn4.setVisibility(View.VISIBLE);
                compassTv.setVisibility(View.GONE);
            }
            else
            {
                posCtr++;

                map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                map.invalidate();

                tv.setText("Walk to the Position and press Record. \nPoint Your Device North.");
                btn1.setVisibility(View.VISIBLE);
                btn2.setVisibility(View.GONE);
                compassTv.setVisibility(View.VISIBLE);
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
     */
    private void recordFingerprint()
    {
        int x    = (int)positions[posCtr][0];
        int y    = (int)positions[posCtr][1];

        Fingerprint currentFP = new Fingerprint(x,y,direction,currentAPList);

        DH.saveFingerprint(currentFP);

        //Log.i("Fingerprint DATA", currentFP.toString());
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
                DH.clearDB();
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
                    ArrayList<APInfo> mAPList = new ArrayList<>();

                    for(int i = 0; i < wifiScanList.size(); i++)
                    {
                        APInfo mAP = new APInfo(wifiScanList.get(i).BSSID, wifiScanList.get(i).level);
                        mAPList.add(mAP);
                    }

                    currentAPList = mAPList;

                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(scanSuccess);
                }
            }
        }
    }
}