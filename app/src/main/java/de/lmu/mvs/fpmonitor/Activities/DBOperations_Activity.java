package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;
import de.lmu.mvs.fpmonitor.Database.WifiScan;
import de.lmu.mvs.fpmonitor.R;


/**
 * This Activity is for the Developer only.
 * It is used to manage, refactor, reformat, export, import.
 */
public class DBOperations_Activity extends Activity
{
    DatabaseHandler DH;
    private ArrayList<Fingerprint> dbFingerprints;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dboperations);

        DH = new DatabaseHandler(this);

        dbFingerprints = new ArrayList<>();

        Button importBtn    = (Button)findViewById(R.id.importBtn);   // Import the DB from Assets
        Button exportBtn    = (Button)findViewById(R.id.exportBtn);   // Export the DB onto the Device
        Button loadBtn      = (Button)findViewById(R.id.loadBtn);     // Load the ProbFPs for Refactor
        Button refactorBtn  = (Button)findViewById(R.id.refactorBtn); // Temporary Function for Refactoring the DB
        Button beaconsBtn   = (Button)findViewById(R.id.beaconsBtn);  // Read and Store the FPs from the BeaconScans (Assets)
        Button clearBtn     = (Button)findViewById(R.id.clearBtn);    // Useless right now


        importBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                executeImport();
            }
        });


        exportBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                executeExport();
            }
        });


        loadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(),"Feature Disabled.",Toast.LENGTH_SHORT).show();
                //executeLoad();
            }
        });


        refactorBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(),"Feature Disabled.",Toast.LENGTH_SHORT).show();
                //executeRefactor();
            }
        });


        beaconsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeBeacons();
            }
        });


        clearBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(),"Feature Disabled.",Toast.LENGTH_SHORT).show();
                //executeClear();
            }
        });
    }


    /**
     * Handle Import Btn.
     * Import the SQLite-Database from the Assets folder.
     */
    private void executeImport()
    {
        DH.importDB();
    }


    /**
     * Handle Export Button.
     * Export the Database onto the Device.
     */
    private void executeExport()
    {
        DH.exportDB();
    }


    /**
     * Handle Beacons Button.
     * Take the prepared .txt Files from Assets and store their content in SQL.
     */
    private void executeBeacons()
    {
        try
        {
            String[] filenames = getApplicationContext().getAssets().list("BeaconScans");

            for (String file : filenames)
            {
                // Log.e("Filename",file);
                InputStream is    = getApplicationContext().getAssets().open("BeaconScans/" + file);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                ArrayList<APInfo> apInfoArrayListFinal = new ArrayList<>();
                ArrayList<APInfo> apInfoArrayListRaw   = new ArrayList<>();
                ArrayList<String> macIDs               = new ArrayList<>();
                ArrayList<WifiScan> scans              = new ArrayList<>();

                String line;

                while ((line=br.readLine())!=null)
                {
                    String[] input = line.split(",");
                    apInfoArrayListRaw.add(new APInfo(input[2], Integer.parseInt(input[1])));
                }

                for (int i=0;i<apInfoArrayListRaw.size();i++)
                {
                    if (!macIDs.contains(apInfoArrayListRaw.get(i).mac))
                    {
                        macIDs.add(apInfoArrayListRaw.get(i).mac);
                    }
                }

                for (String mac : macIDs)
                {
                    double median = 0;
                    int ctr       = 0;

                    for (int i=0;i<apInfoArrayListRaw.size();i++)
                    {
                        if (mac.equals(apInfoArrayListRaw.get(i).mac))
                        {
                            median += apInfoArrayListRaw.get(i).rssi;
                            ctr++;
                        }
                    }

                    median = (median/ctr);

                    apInfoArrayListFinal.add(new APInfo(mac, (int) median));
                }

                scans.add(new WifiScan(1,apInfoArrayListFinal));

                DH.saveBeaconData(file,scans);

                Log.i("SQL-DB","Fingerprint \"" + file + "\" stored to SQL");

                //Fingerprint fp = new Fingerprint(0,0,0,scans);
                //Log.d("Fingerprint Data",fp.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Handle the Load Btn.
     * Load the Probabilistic Table for further refactoring. (Has to be done separately, or error)
     */
    private void executeLoad()
    {
        dbFingerprints = DH.getProbFingerprints();

        Log.e("FP_Size", dbFingerprints.size() + " Fingerprints loaded");

        for (Fingerprint f : dbFingerprints)
        {
            //Log.e("FP_Data", f.toString());
        }
    }


    /**
     * Handle the Refactor Btn.
     * Take Data from Prob-Table, sort the content, calculate median-values and store into Det-Table.
     * Additionally store into CSV-Table.
     */
    private void executeRefactor()
    {
        //DH.importDB();
        //Log.e("REFACTOR","Database Imported");

        dbFingerprints = DH.getProbFingerprints();
        Log.e("REFACTOR", dbFingerprints.size() + " Fingerprints Loaded");

        dbFingerprints = sortList(dbFingerprints);
        Log.e("REFACTOR","Fingerprints sorted");

        DH.clearProbTable();
        Log.e("REFACTOR","Table cleared");

        Log.e("REFACTOR","Start Storing");
        for (Fingerprint f : dbFingerprints)
        {
            switch (f.direction)
            {
                case 30:
                    f.direction = 1;
                    break;
                case 120:
                    f.direction = 2;
                    break;
                case 210:
                    f.direction = 3;
                    break;
                case 300:
                    f.direction = 4;
                    break;
                default:
                    break;
            }

            DH.saveProbFingerprint(f);
            DH.saveDetFingerprint(calculateMedian(f));
            DH.saveFingerprintCSV(calculateMedian(f));
        }
        Log.e("REFACTOR","End Storing");

        DH.exportDB();
        Log.e("REFACTOR","Database Exported");
    }


    /**
     * Handle Clear Btn.
     * Wipe the Det-Table.
     * Useless right now.
     */
    private void executeClear()
    {
        DH.clearDetTable();
        Toast.makeText(getApplicationContext(),"Modify table cleared",Toast.LENGTH_SHORT).show();
    }


    /**
     * Calculate Median.
     * @param f
     * @return
     */
    private Fingerprint calculateMedian(Fingerprint f)
    {
        ArrayList<String> macIDs = new ArrayList<>();
        ArrayList<APInfo> roundedAPInfo = new ArrayList<>();
        ArrayList<WifiScan> wifiScans = new ArrayList<>();
        APInfo tempAPInfo;

        // Get all unique MACs
        for (int i=0;i<f.scans.size();i++)
        {
            for (int j=0;j<f.scans.get(i).apInfoList.size();j++)
            {
                if (!macIDs.contains(f.scans.get(i).apInfoList.get(j).mac))
                {
                    macIDs.add(f.scans.get(i).apInfoList.get(j).mac);
                }
            }
        }

        // For each unique MAC found, get the Median
        for (String mac : macIDs)
        {
            double median = 0;
            int ctr       = 0;

            for (int i=0;i<f.scans.size();i++)
            {
                for (int j=0;j<f.scans.get(i).apInfoList.size();j++)
                {
                    if (mac.equals(f.scans.get(i).apInfoList.get(j).mac))
                    {
                        median += f.scans.get(i).apInfoList.get(j).rssi;
                        ctr++;
                    }
                }
            }

            median = (median/ctr);

            tempAPInfo = new APInfo(mac,(int)median);
            roundedAPInfo.add(tempAPInfo);
        }

        wifiScans.add(new WifiScan(1,roundedAPInfo));

        return new Fingerprint(f.x_coordinate,f.y_coordinate,f.direction,wifiScans);
    }


    /**
     *
     * @param fingerprints
     * @return
     */
    private ArrayList<Fingerprint> sortList(ArrayList<Fingerprint> fingerprints)
    {
        Collections.sort(fingerprints, new Comparator <Fingerprint>()
        {
            @Override
            public int compare(Fingerprint fp1, Fingerprint fp2)
            {
                if (fp1.y_coordinate != fp2.y_coordinate)
                {
                    return fp1.y_coordinate - fp2.y_coordinate;
                }
                else
                {
                    return fp1.x_coordinate - fp2.x_coordinate;
                }
            }
        });

        return fingerprints;
    }
}
