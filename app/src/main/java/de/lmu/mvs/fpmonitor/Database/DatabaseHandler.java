package de.lmu.mvs.fpmonitor.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private final Context context;

    private static final String DATABASE_NAME = "fpDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String PROBABILISTIC_FINGERPRINTS_TABLE = "probabilistic_fingerprints";
    private static final String DETERMINISTIC_FINGERPRINTS_TABLE = "deterministic_fingerprints";
    private static final String CSV_FORMATTED_TABLE              = "csv_formatted_fingerprints";
    private static final String TESTING_TABLE                    = "testing_table";

    private static final String FP_ID      = "_id";
    private static final String X          = "x_coordinate";
    private static final String Y          = "y_coordinate";
    private static final String DIR        = "direction";
    private static final String WIFI_SCANS = "wifi_scans";

    private static final String createProbabilisticTable =
        "CREATE TABLE " + PROBABILISTIC_FINGERPRINTS_TABLE +
                   " (" + FP_ID      + " TEXT PRIMARY KEY, "
                        + X          + " INTEGER, "
                        + Y          + " INTEGER, "
                        + DIR        + " INTEGER, "
                        + WIFI_SCANS + " TEXT)";

    private static final String createDeterministicTable =
        "CREATE TABLE " + DETERMINISTIC_FINGERPRINTS_TABLE +
                   " (" + FP_ID      + " TEXT PRIMARY KEY, "
                        + X          + " INTEGER, "
                        + Y          + " INTEGER, "
                        + DIR        + " INTEGER, "
                        + WIFI_SCANS + " TEXT)";

    private static final String createCSVFormattedTable =
        "CREATE TABLE " + CSV_FORMATTED_TABLE +
                   " (" + FP_ID      + " TEXT PRIMARY KEY, "
                        + X          + " INTEGER, "
                        + Y          + " INTEGER, "
                        + DIR        + " INTEGER, "
                        + WIFI_SCANS + " TEXT)";

    private static final String createTestingTable =
        "CREATE TABLE " + TESTING_TABLE +
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
        this.context = ctx;
    }


    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL(createProbabilisticTable);
        db.execSQL(createDeterministicTable);
        db.execSQL(createCSVFormattedTable);
        db.execSQL(createTestingTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + PROBABILISTIC_FINGERPRINTS_TABLE);

        onCreate(db);
    }



    // ****************************************************************************************** //
    // *** General Functions ******************************************************************** //
    // ****************************************************************************************** //


    /**
     * Copy Your Database from Assets-Folder onto the device.
     */
    public void importDB()
    {
        try
        {
            InputStream myInput = context.getAssets().open("firsttest.db");

            String outFileName = "data//data//de.lmu.mvs.fpmonitor//databases//fpDatabase";

            OutputStream myOutput = new FileOutputStream(outFileName);


            byte[] buffer = new byte[1024];
            int length;

            while ((length = myInput.read(buffer))>0)
            {
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();

            Toast.makeText(context, "Import Successful", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(context,"Import Failed",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Export current Version of the Database onto the Device.
     */
    public void exportDB()
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
                    Toast.makeText(context,"Export Successful",Toast.LENGTH_SHORT).show();
                    src.close();
                    dst.close();
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context,"Export Failed",Toast.LENGTH_SHORT).show();
        }
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



    // ****************************************************************************************** //
    // *** Probabilistic-Table Functions ******************************************************** //
    // ****************************************************************************************** //


    /**
     *  Clear the Spot-Table and re-create an empty one.
     */
    public void clearProbTable()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + PROBABILISTIC_FINGERPRINTS_TABLE);
        db.execSQL(createProbabilisticTable);
        db.close();
    }


    /**
     * Easy way of checking if there is any data inside the Table.
     *
     * @return true if Table is empty, false if there is Data inside
     */
    private boolean probTableIsEmpty()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + PROBABILISTIC_FINGERPRINTS_TABLE, null);

        return !mCursor.moveToFirst();
    }


    /**
     * Retrieve an ArrayList of all the Fingerprints stored.
     *
     * @return An ArrayList of all Fingerprints stored
     */
    public ArrayList<Fingerprint> getProbFingerprints()
    {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {FP_ID, X, Y, DIR, WIFI_SCANS};
        Cursor cr = db.query(PROBABILISTIC_FINGERPRINTS_TABLE, columns, null, null, null, null, null);

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();

        Fingerprint fp;
        int x,y,d;
        ArrayList<WifiScan> scans;

        if (!probTableIsEmpty())
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
    public ArrayList<Fingerprint> getProbFingerprints(int direction)
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
                            PROBABILISTIC_FINGERPRINTS_TABLE,
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
                            PROBABILISTIC_FINGERPRINTS_TABLE,
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
                            PROBABILISTIC_FINGERPRINTS_TABLE,
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
    public void saveProbFingerprint(Fingerprint f)
    {
        SQLiteDatabase db = getWritableDatabase();
        int pos = (int)DatabaseUtils.queryNumEntries(db, PROBABILISTIC_FINGERPRINTS_TABLE);

        ContentValues fingerprintValues = new ContentValues();

        fingerprintValues.put(FP_ID,      (pos+1));
        fingerprintValues.put(X,          f.x_coordinate);
        fingerprintValues.put(Y,          f.y_coordinate);
        fingerprintValues.put(DIR,        f.direction);
        fingerprintValues.put(WIFI_SCANS, ArrayToJSON(f.scans).toString());

        db.insert(PROBABILISTIC_FINGERPRINTS_TABLE,null,fingerprintValues);

        db.close();
    }



    // ****************************************************************************************** //
    // *** Deterministic-Table Functions ******************************************************** //
    // ****************************************************************************************** //


    /**
     *  Clear the Modify-Table and re-create an empty one.
     */
    public void clearDetTable()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DETERMINISTIC_FINGERPRINTS_TABLE);
        db.execSQL(createDeterministicTable);
        db.close();
    }


    /**
     * Easy way of checking if there is any data inside the Table.
     *
     * @return true if Table is empty, false if there is Data inside
     */
    private boolean detTableIsEmpty()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + DETERMINISTIC_FINGERPRINTS_TABLE, null);

        return !mCursor.moveToFirst();
    }


    public ArrayList<Fingerprint> getDetFingerprints()
    {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {FP_ID, X, Y, DIR, WIFI_SCANS};
        Cursor cr = db.query(DETERMINISTIC_FINGERPRINTS_TABLE, columns, null, null, null, null, null);

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();

        Fingerprint fp;
        int x,y,d;
        ArrayList<WifiScan> scans;

        if (!detTableIsEmpty())
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
     * Save a new Fingerprint into the Database.
     *
     * @param f The Fingerprint to be saved
     */
    public void saveDetFingerprint(Fingerprint f)
    {
        SQLiteDatabase db = getWritableDatabase();
        int pos = (int)DatabaseUtils.queryNumEntries(db, DETERMINISTIC_FINGERPRINTS_TABLE);

        ContentValues fingerprintValues = new ContentValues();

        fingerprintValues.put(FP_ID,      (pos+1));
        fingerprintValues.put(X,          f.x_coordinate);
        fingerprintValues.put(Y,          f.y_coordinate);
        fingerprintValues.put(DIR,        f.direction);
        fingerprintValues.put(WIFI_SCANS, ArrayToJSON(f.scans).toString());

        db.insert(DETERMINISTIC_FINGERPRINTS_TABLE,null,fingerprintValues);

        db.close();
    }



    // ****************************************************************************************** //
    // *** CSV-Formatted-Table Functions ******************************************************** //
    // ****************************************************************************************** //


    /**
     * This Method stores the Fingerprints into the Modify-Table.
     * Additionally the Scans are re-formatted, so that the CSV-Sheet, which can be extracted via
     * the SQLite Browser, can easily be set up for a proper view.
     *
     * Further steps:
     * - Go to SQLite Browser and export the .csv sheet
     * - Open a new Excel Document
     * - Data -> From Text (select the .csv sheet)
     * - select "separated" -> next -> choose "comma" as separation -> finish
     * - Start -> "replace" -> replace " and ,,,, with empty space
     * - finally re-format as You wish and save as .pdf document
     *
     * @param f The Fingerprint to be saved
     */
    public void saveFingerprintCSV(Fingerprint f)
    {
        SQLiteDatabase db = getWritableDatabase();
        int pos = (int)DatabaseUtils.queryNumEntries(db, CSV_FORMATTED_TABLE);

        ContentValues fingerprintValues = new ContentValues();

        fingerprintValues.put(FP_ID,      (pos+1));
        fingerprintValues.put(X,          f.x_coordinate);
        fingerprintValues.put(Y,          f.y_coordinate);
        fingerprintValues.put(DIR,        f.direction);

        String csvValues = "\"";

        for (int i=0;i<f.scans.size();i++)
        {
            for (int j=0;j<f.scans.get(i).apInfoList.size();j++)
            {
                csvValues += ",,,,MAC= " + f.scans.get(i).apInfoList.get(j).mac;
                csvValues += " | RSSI= " + f.scans.get(i).apInfoList.get(j).rssi;

                if (i==f.scans.size()-1&&j==f.scans.get(i).apInfoList.size()-1)
                {
                    csvValues += "\"";
                }
                else
                {
                    csvValues += "\n";
                }
            }
        }

        fingerprintValues.put(WIFI_SCANS, csvValues);

        Log.i("DB","Fingerprint"+(pos+1)+" saved.");

        db.insert(CSV_FORMATTED_TABLE,null,fingerprintValues);

        db.close();
    }



    // ****************************************************************************************** //
    // *** Testing-Table Functions ************************************************************** //
    // ****************************************************************************************** //

    // not yet in use
}
