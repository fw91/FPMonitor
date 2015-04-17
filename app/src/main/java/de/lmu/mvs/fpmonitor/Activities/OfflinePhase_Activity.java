package de.lmu.mvs.fpmonitor.Activities;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.lmu.mvs.fpmonitor.Compass;
import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;
import de.lmu.mvs.fpmonitor.Database.WifiScan;
import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.R;

/**
 * This Activity is used for creating a Radio Map.
 * It shows a floor plan with key Positions on it where the recording has to take place.
 * The Record-Button collects all necessary data and stores it into the Database.
 * The Next-Button moves to the next Position.
 */
public class OfflinePhase_Activity extends ActionBarActivity
{
    MapView map;
    TextView tv;
    Button btn1, btn2;
    ProgressBar progress;

    final static String SCAN_SUCCESSFUL = "scan_successful";

    Intent scanSuccess = new Intent(SCAN_SUCCESSFUL);

    private float positions[][];
    private int posCtr;

    private boolean started;
    private boolean queryScanResults;

    private DatabaseHandler DH;

    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;

    private Compass compass;
    private int direction;
    private int dirCtr;

    private ArrayList<WifiScan> scansFinal;
    private int scanCtr;


    // This is required to get up-to-date ScanResults
    BroadcastReceiver recordingReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (started)
            {
                if (scanCtr <= 20)
                {
                    if (mWifiManager.startScan())
                    {
                        queryScanResults = true;
                    }
                }
                else
                {
                    queryScanResults = false;

                    progress.setVisibility(View.GONE);

                    recordFingerprint();

                    scanCtr = 1;

                    if (dirCtr == 3)
                    {
                        dirCtr = 0;

                        tv.setText("Record finished. Press Next.");
                        btn1.setVisibility(View.GONE);
                        btn2.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        dirCtr++;

                        tv.setText("Record successful. \nTurn Around. (" + (dirCtr+1) + "/4 Directions)");
                        btn1.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offlinephase);

        // Initialize Layout
        map       = (MapView)findViewById(R.id.wlan_view);
        tv        = (TextView)findViewById(R.id.recordTV);
        btn1      = (Button)findViewById(R.id.recordBtn);
        btn2      = (Button)findViewById(R.id.nextBtn);
        progress  = (ProgressBar)findViewById(R.id.progress);

        tv.setText("Press the Button to start recording.");
        btn1.setVisibility(View.GONE);

        // Initialize Coordinates
        positions = initCoordinates();
        dirCtr    = 0;
        posCtr    = 0;

        // Initialize Flags
        started          = false;
        queryScanResults = false;

        // Initialize SQLite-Database
        DH = new DatabaseHandler(getApplicationContext());

        // Initialize Wifi
        mWifiManager  = (WifiManager)getSystemService(WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();
        scanCtr       = 1;

        // Initialize Compass
        compass = new Compass(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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


        /*
         * Used for getting all the coordinates needed for the Database.
         *
        map.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i("Coords", "X=" + event.getX() + " Y=" + event.getY());
                map.setPosition(event.getX(),event.getY(),map.getWidth(),map.getHeight());
                map.invalidate();
            }
        });
         *
         */


        /*
         * Used for checking if Coordinates were set up correctly.
         */
        map.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                map.invalidate();

                if (posCtr < positions.length-1)
                {
                    posCtr++;
                }
                return false;
            }
        });
         /*
         */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_record, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.get_posCtr:
                Toast.makeText(getApplicationContext(),"Current Pos= " + (posCtr+1) + "/" + positions.length, Toast.LENGTH_SHORT).show();
                break;
            case R.id.set_posCtr:
                setPositionDialog();
                break;
            case R.id.exp_db:
                DH.exportDB(getApplicationContext());
                break;
            case R.id.del_db:
                clearDatabaseDialog();
                break;
            default:
                break;
        }
        return true;
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
        LocalBroadcastManager.getInstance(this).registerReceiver(recordingReceiver, new IntentFilter(SCAN_SUCCESSFUL));

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

        tv.setText("...");
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
            tv.setText("Walk to the Position and press Record.");
            btn1.setVisibility(View.VISIBLE);
            btn2.setText("Next");
            btn2.setVisibility(View.GONE);

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
            }
            else
            {
                posCtr++;

                map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                map.invalidate();

                tv.setText("Walk to the Position and press Record.");
                btn1.setVisibility(View.VISIBLE);
                btn2.setVisibility(View.GONE);
            }
        }
    }


    /**
     * Get Wifi-Data and store the Fingerprint.
     */
    private void recordFingerprint()
    {
        int x    = (int)positions[posCtr][0];
        int y    = (int)positions[posCtr][1];

        direction = direction/scanCtr;

        Fingerprint currentFP = new Fingerprint(x, y, direction, scansFinal);

        DH.saveFingerprint(currentFP);
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
     * AlertDialog for setting the current Position.
     */
    private void setPositionDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Set Position");
        alert.setMessage("Insert Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String value = input.getText().toString();
                if (Integer.parseInt(value)<0 || Integer.parseInt(value)>positions.length-1)
                {
                    Toast.makeText(getApplicationContext(),"Error, Position does not exist!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    posCtr = Integer.parseInt(value);
                    dirCtr = 0;

                    map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                    map.invalidate();

                    btn1.setVisibility(View.VISIBLE);
                    btn2.setVisibility(View.GONE);

                    Toast.makeText(getApplicationContext(),"Position set to "+value,Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // Canceled.
            }
        });

        alert.show();
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
                    if (scanCtr == 1)
                    {
                        scansFinal = new ArrayList<>();
                        direction = 0;
                    }

                    tv.setText("Recording Fingerprint... (" + scanCtr + "/20)");

                    ArrayList<APInfo> tempAPList = new ArrayList<>();

                    direction += compass.getDir();

                    for(int i = 0; i < wifiScanList.size(); i++)
                    {
                        APInfo tempAP = new APInfo(wifiScanList.get(i).BSSID, wifiScanList.get(i).level);
                        tempAPList.add(tempAP);
                    }

                    scansFinal.add(new WifiScan(scanCtr, tempAPList));

                    scanCtr++;

                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(scanSuccess);
                }
            }
        }
    }


    /**
     * Initialize the set of Coordinates to be printed on the Map.
     *
     * @return an Array of Coordinates
     */
    private float[][] initCoordinates()
    {
        // Coordinates for my Home-Environment
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

        // Coordinates to test functionality
        float[][] testCoordinates =
                {{299,120},{325,120}};

        // Coordinates for first Recording-Session
        float[][] recordCoordinates1 =                                          // 50 Fingerprints
                {{299,120},{325,120},{299,141},{325,141},                       // G 002
                 {393,144},{419,144},                                           // G 001
                 {299,184},{325,184},{299,205},{325,205},                       // G 004
                 {393,184},{419,184},{393,205},{419,205},                       // G 003
                 {299,237},{325,237},{299,258},{325,258},                       // G 006
                 {393,237},{419,237},{393,258},{419,258},                       // G 005
                 {299,289},{325,289},{299,310},{325,310},                       // G 008
                 {393,289},{419,289},{393,310},{419,310},                       // G 007
                 {299,348},{325,348},{312,378},{299,409},{325,409},             // G 010
                 {393,348},{419,348},{406,378},{393,409},{419,409},             // G 009

                 {359,138},{359,168},{359,198},{359,228},{359,258},             // Gang
                 {359,288},{359,318},{359,348},{359,378},{359,408}};

        // Coordinates for second Recording-Session
        float[][] recordCoordinates2 =                                          // 34 Fingerprints
                {{360,63},{360,94},{381, 63},{381, 94},                         // Mittelstück
                 {408,68},{429,86},{408,103},                                   // Treppen
                 {295,65},{295,84},                                             // Klo links
                 {328,65},{328,84},                                             // Klo rechts

                 {15, 34},{45, 34},{75, 34},{105,34},{135,34},                  // Gang oben
                 {165,34},{195,34},{225,34},{255,34},{285,34},
                 {315,34},{345,34},{375,34},{405,34},{435,34},
                 {465,34},{495,34},{525,34},{555,34},{585,34},
                 {615,34},{645,34},{675,34}};

        return recordCoordinates1;
    }
}