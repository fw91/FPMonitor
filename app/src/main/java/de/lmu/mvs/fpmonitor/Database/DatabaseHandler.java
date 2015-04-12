package de.lmu.mvs.fpmonitor.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "fpDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String FINGERPRINT_TABLE = "fingerprints";

    private static final String FP_ID      = "_id";
    private static final String X          = "x_coordinate";
    private static final String Y          = "y_coordinate";
    private static final String DIR        = "direction";
    private static final String WIFI_SCANS = "wifi_scans";

    private static final String createFingerprintDB =
        "CREATE TABLE " + FINGERPRINT_TABLE +
                   " (" + FP_ID      + " TEXT PRIMARY KEY, "
                        + X          + " INTEGER, "
                        + Y          + " INTEGER, "
                        + DIR        + " INTEGER, "
                        + WIFI_SCANS + " TEXT)";


    /**
     * Main Constructor.
     *
     * @param ctx Application Context
     */
    public DatabaseHandler(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL(createFingerprintDB);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + FINGERPRINT_TABLE);

        onCreate(db);
    }


    /**
     *  Clear the Spot-Table and re-create an empty one.
     */
    public void clearDB()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + FINGERPRINT_TABLE);
        db.execSQL(createFingerprintDB);
        db.close();
    }


    /**
     * Easy way of checking if there is any data inside the Table.
     *
     * @return true if Table is empty, false if there is Data inside
     */
    private boolean tableIsEmpty()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + FINGERPRINT_TABLE, null);

        return !mCursor.moveToFirst();
    }


    /**
     * Retrieve an ArrayList of all the Fingerprints stored.
     *
     * @return An ArrayList of all Fingerprints stored
     */
    public ArrayList<Fingerprint> getFingerprints()
    {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {FP_ID, X, Y, DIR, WIFI_SCANS};
        Cursor cr = db.query(FINGERPRINT_TABLE, columns, null, null, null, null, null);

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();

        Fingerprint fp;
        int x,y,d;
        ArrayList<WifiScan> scans;

        if (!tableIsEmpty())
        {
            if (cr != null)
            {
                cr.moveToFirst();
            }

            do
            {
                x       = cr.getInt(1);
                y       = cr.getInt(2);
                d       = cr.getInt(3);
                scans   = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,scans);

                fingerprints.add(fp);

            } while (cr.moveToNext());
        }

        cr.close();
        db.close();

        return fingerprints;
    }


    /**
     * Retrieve an ArrayList of all the Fingerprints stored based on the Direction.
     *
     * @return An ArrayList of all Fingerprints stored
     */
    public ArrayList<Fingerprint> getFingerprints(int direction)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr;

        int minDirection, maxDirection;

        if (direction < 50)
        {
            minDirection = 360 + direction - 50;
            maxDirection = direction + 50;

            cr = db.query
                    (
                            FINGERPRINT_TABLE,
                            new String[] {FP_ID, X, Y, DIR, WIFI_SCANS},
                            DIR + " > " + minDirection + " OR " + DIR + " < " + maxDirection,
                            null, null, null, null, null
                    );
        }
        else if (310 < direction)
        {
            minDirection = direction - 50;
            maxDirection = direction - 360 + 50;

            cr = db.query
                    (
                            FINGERPRINT_TABLE,
                            new String[] {FP_ID, X, Y, DIR, WIFI_SCANS},
                            DIR + " > " + minDirection + " OR " + DIR + " < " + maxDirection,
                            null, null, null, null, null
                    );
        }
        else
        {
            minDirection = direction - 50;
            maxDirection = direction + 50;

            cr = db.query
                    (
                            FINGERPRINT_TABLE,
                            new String[] {FP_ID, X, Y, DIR, WIFI_SCANS},
                            DIR + " BETWEEN " + minDirection + " AND " + maxDirection,
                            null, null, null, null, null
                    );
        }

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();

        Fingerprint fp;
        int x,y,d;
        ArrayList<WifiScan> scans;

        if (cr.moveToFirst())
        {
            do
            {
                x       = cr.getInt(1);
                y       = cr.getInt(2);
                d       = cr.getInt(3);
                scans   = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,scans);

                fingerprints.add(fp);

            } while (cr.moveToNext());
        }

        cr.close();
        db.close();

        return fingerprints;
    }


    /**
     * Save a new Fingerprint into the Database.
     *
     * @param f The Fingerprint to be saved
     */
    public void saveFingerprint(Fingerprint f)
    {
        SQLiteDatabase db = getWritableDatabase();
        int pos = (int)DatabaseUtils.queryNumEntries(db, FINGERPRINT_TABLE);

        ContentValues fingerprintValues = new ContentValues();

        fingerprintValues.put(FP_ID,      "Fingerprint." + (pos+1));
        fingerprintValues.put(X,          f.x_coordinate);
        fingerprintValues.put(Y,          f.y_coordinate);
        fingerprintValues.put(DIR,        f.direction);
        fingerprintValues.put(WIFI_SCANS, ArrayToJSON(f.scans).toString());

        db.insert(FINGERPRINT_TABLE,null,fingerprintValues);

        db.close();
    }



    /**
     * Convert an ArrayList of WifiScans into a JSON-String for storing into the Database.
     *
     * @param wifiScans ArrayList of WifiScans
     * @return A JSON-Array of WifiScan-ArrayList for storing into DB
     */
    private JSONArray ArrayToJSON(ArrayList<WifiScan> wifiScans)
    {
        JSONArray wifiScanArray = new JSONArray();

        JSONObject scanData, apData;
        JSONArray apInfoArray;

        for (int i=0;i<wifiScans.size();i++)
        {
            try
            {
                scanData    = new JSONObject();
                apInfoArray = new JSONArray();

                for (int j=0;j<wifiScans.get(i).apInfoList.size();j++)
                {
                    apData = new JSONObject();

                    apData.put("mac",  wifiScans.get(i).apInfoList.get(j).mac);
                    apData.put("rssi", wifiScans.get(i).apInfoList.get(j).rssi);

                    apInfoArray.put(apData);
                }

                scanData.put("scan_id", wifiScans.get(i).id);
                scanData.put("ap_list", apInfoArray);

                wifiScanArray.put(scanData);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return wifiScanArray;
    }


    /**
     * Converts the JSONArray-String from the Database into an APInfo ArrayList.
     *
     * @param wifiScanJSON JSON-String of Array from DB
     * @return An ArrayList of APInfo for Fingerprint
     */
    private ArrayList<WifiScan> getAPArray(String wifiScanJSON)
    {
        ArrayList<WifiScan> scans = new ArrayList<>();
        ArrayList<APInfo> apInfoList;
        JSONArray wifiScanArray, apInfoArray;
        JSONObject scanData;

        try
        {
            wifiScanArray = new JSONArray(wifiScanJSON);

            for (int i=0;i<wifiScanArray.length();i++)
            {
                scanData = wifiScanArray.getJSONObject(i);

                apInfoList = new ArrayList<>();

                apInfoArray = scanData.getJSONArray("ap_list");

                for (int j=0;j<apInfoArray.length();j++)
                {
                    apInfoList.add(new APInfo(apInfoArray.getJSONObject(j).getString("mac"),
                                              apInfoArray.getJSONObject(j).getInt("rssi")));
                }

                scans.add(new WifiScan(scanData.getInt("scan_id"), apInfoList));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return scans;
    }


    /**
     * Export current Version of the Database.
     * @param ctx Application Context
     */
    public void exportDB(Context ctx)
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMM-HHmm", Locale.GERMANY);
        String dateAppendix = df.format(c.getTime());

        try
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite())
            {
                String currentDBPath = "//data//de.lmu.mvs.fpmonitor//databases//fpDatabase";
                String backupDBPath = "fpDatabase_"+dateAppendix+".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists())
                {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    Toast.makeText(ctx,"Export Successful",Toast.LENGTH_SHORT).show();
                    src.close();
                    dst.close();
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(ctx,"Export Failed",Toast.LENGTH_SHORT).show();
        }
    }
}
