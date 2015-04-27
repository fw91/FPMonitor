package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;
import de.lmu.mvs.fpmonitor.Database.WifiScan;
import de.lmu.mvs.fpmonitor.R;


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

        Button importBtn    = (Button)findViewById(R.id.importBtn);
        Button exportBtn    = (Button)findViewById(R.id.exportBtn);
        Button loadBtn      = (Button)findViewById(R.id.loadBtn);
        Button refactorBtn  = (Button)findViewById(R.id.refactorBtn);
        Button clearBtn     = (Button)findViewById(R.id.clearBtn);


        importBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DH.importDB();
            }
        });


        exportBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DH.exportDB();
            }
        });


        loadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dbFingerprints = DH.getProbFingerprints();

                Log.e("FP_Size", dbFingerprints.size() + " Fingerprints loaded");

                for (Fingerprint f : dbFingerprints)
                {
                    //Log.e("FP_Data", f.toString());
                }
            }
        });


        refactorBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
        });


        clearBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DH.clearDetTable();
                Toast.makeText(getApplicationContext(),"Modify table cleared",Toast.LENGTH_SHORT).show();
            }
        });
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

    private ArrayList<Fingerprint> getTestList()
    {
        ArrayList<Fingerprint> testList = new ArrayList<>();
        ArrayList<APInfo> apInfoTestList = new ArrayList<>();
        ArrayList<WifiScan> scansList = new ArrayList<>();

        apInfoTestList.add(new APInfo("abc",2));
        scansList.add(new WifiScan(1,apInfoTestList));

        testList.add(new Fingerprint(3,10,0,scansList));
        testList.add(new Fingerprint(6,5,0,scansList));
        testList.add(new Fingerprint(5,7,0,scansList));
        testList.add(new Fingerprint(2,5,0,scansList));
        testList.add(new Fingerprint(4,5,0,scansList));
        testList.add(new Fingerprint(7,10,0,scansList));
        testList.add(new Fingerprint(6,8,0,scansList));

        return testList;
    }
}
