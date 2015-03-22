package de.lmu.mvs.fpmonitor.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "fpDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String FINGERPRINT_TABLE = "fingerprints";

    private static final String FP_ID     = "_id";
    private static final String X         = "x_coordinate";
    private static final String Y         = "y_coordinate";
    private static final String DIR       = "direction";
    private static final String AP_INFO   = "ap_info";

    private static final String createFingerprintDB =
        "CREATE TABLE " + FINGERPRINT_TABLE +
                   " (" + FP_ID   + " TEXT PRIMARY KEY, "
                        + X       + " TEXT, "
                        + Y       + " TEXT, "
                        + DIR     + " TEXT, "
                        + AP_INFO + " TEXT)";


    /**
     * Main Constructor
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
     *  clear the Spot-Table and re-create an empty one
     */
    public void clearDB()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + FINGERPRINT_TABLE);
        db.execSQL(createFingerprintDB);
        db.close();
    }


    /**
     * Easy way of checking if there is any data inside the Table
     * @return true if Table is empty, false if there is Data inside
     */
    private boolean tableIsEmpty()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + FINGERPRINT_TABLE, null);

        return !mCursor.moveToFirst();
    }


    /**
     * Initialize the Cursor for reading out the Database
     * @return Cursor for accessing the Fingerprint-DB
     */
    private Cursor getCursor()
    {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {FP_ID, X, Y, DIR, AP_INFO};

        return db.query(FINGERPRINT_TABLE, columns, null, null, null, null, null);
    }


    /**
     * Convert an ArrayList of APInfos into a JSON-String for storing into theDatabase
     * @param apInfos ArrayList of APInfo
     * @return A JSON-Array of APInfo ArrayList for storing into DB
     */
    private JSONArray ArrayToJSON(ArrayList<APInfo> apInfos)
    {
        JSONArray apInfoArray = new JSONArray();
        JSONObject apData;

        for (int i=0; i<apInfos.size();i++)
        {
            try
            {
                apData = new JSONObject();

                apData.put("mac",apInfos.get(i).mac);
                apData.put("rssi",apInfos.get(i).rssi);

                apInfoArray.put(apData);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return apInfoArray;
    }


    /**
     * Converts the JSONArray-String from the Database into an APInfo ArrayList
     * @param apInfoJSON JSON-String of Array from DB
     * @return An ArrayList of APInfo for Fingerprint
     */
    private ArrayList<APInfo> getAPArray(String apInfoJSON)
    {
        ArrayList<APInfo> apInfos = new ArrayList<>();
        JSONArray apInfoArray;
        JSONObject apData;

        try
        {
            apInfoArray = new JSONArray(apInfoJSON);

            for (int i=0;i<apInfoArray.length();i++)
            {
                apData = apInfoArray.getJSONObject(i);

                apInfos.add(new APInfo(apData.getString("mac"),apData.getInt("rssi")));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return apInfos;
    }


    /**
     * Retrieve an ArrayList of all the Fingerprints stored
     * @return An ArrayList of all Fingerprints stored
     */
    public ArrayList<Fingerprint> getFingerprints()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = getCursor();

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();

        Fingerprint fp;
        int x,y;
        String d;
        ArrayList<APInfo> apInfos;

        if (!tableIsEmpty())
        {
            if (cr != null)
            {
                cr.moveToFirst();
            }

            do
            {
                x       = Integer.parseInt(cr.getString(1));
                y       = Integer.parseInt(cr.getString(2));
                d       = cr.getString(3);
                apInfos = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,apInfos);

                fingerprints.add(fp);

            } while (cr.moveToNext());
        }

        db.close();

        return fingerprints;
    }


    /**
     * Save a new Fingerprint into the Database
     * @param f The Fingerprint to be saved
     */
    public void saveFingerprint(Fingerprint f)
    {
        SQLiteDatabase db = getWritableDatabase();
        int pos = (int)DatabaseUtils.queryNumEntries(db, FINGERPRINT_TABLE);

        ContentValues fingerprintValues = new ContentValues();

        fingerprintValues.put(FP_ID,   "Position." + (pos+1));
        fingerprintValues.put(X,       Integer.toString(f.x_coordinate));
        fingerprintValues.put(Y,       Integer.toString(f.y_coordinate));
        fingerprintValues.put(DIR,     f.direction);
        fingerprintValues.put(AP_INFO, ArrayToJSON(f.apList).toString());

        db.insert(FINGERPRINT_TABLE,null,fingerprintValues);

        db.close();
    }















    public ArrayList<Fingerprint> getNorthFPs()
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cr = db.query
                (
                        FINGERPRINT_TABLE,
                        new String[] {FP_ID, X, Y, DIR, AP_INFO},
                        DIR + "='North'",
                        null, null, null, null, null
                );

        ArrayList<Fingerprint> northFPs = new ArrayList<>();

        Fingerprint fp;
        int x,y;
        String d;
        ArrayList<APInfo> apInfos;

        if (!tableIsEmpty())
        {
            if (cr != null)
            {
                cr.moveToFirst();
            }

            do
            {
                x       = Integer.parseInt(cr.getString(1));
                y       = Integer.parseInt(cr.getString(2));
                d       = cr.getString(3);
                apInfos = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,apInfos);

                northFPs.add(fp);

            } while (cr.moveToNext());
        }

        db.close();

        return northFPs;
    }

    public ArrayList<Fingerprint> getEastFPs()
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cr = db.query
                (
                        FINGERPRINT_TABLE,
                        new String[] {FP_ID, X, Y, DIR, AP_INFO},
                        DIR + "='East'",
                        null, null, null, null, null
                );

        ArrayList<Fingerprint> eastFPs = new ArrayList<>();

        Fingerprint fp;
        int x,y;
        String d;
        ArrayList<APInfo> apInfos;

        if (!tableIsEmpty())
        {
            if (cr != null)
            {
                cr.moveToFirst();
            }

            do
            {
                x       = Integer.parseInt(cr.getString(1));
                y       = Integer.parseInt(cr.getString(2));
                d       = cr.getString(3);
                apInfos = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,apInfos);

                eastFPs.add(fp);

            } while (cr.moveToNext());
        }

        db.close();

        return eastFPs;
    }

    public ArrayList<Fingerprint> getSouthFPs()
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cr = db.query
                (
                        FINGERPRINT_TABLE,
                        new String[] {FP_ID, X, Y, DIR, AP_INFO},
                        DIR + "='South'",
                        null, null, null, null, null
                );

        ArrayList<Fingerprint> southFPs = new ArrayList<>();

        Fingerprint fp;
        int x,y;
        String d;
        ArrayList<APInfo> apInfos;

        if (!tableIsEmpty())
        {
            if (cr != null)
            {
                cr.moveToFirst();
            }

            do
            {
                x       = Integer.parseInt(cr.getString(1));
                y       = Integer.parseInt(cr.getString(2));
                d       = cr.getString(3);
                apInfos = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,apInfos);

                southFPs.add(fp);

            } while (cr.moveToNext());
        }

        db.close();

        return southFPs;
    }

    public ArrayList<Fingerprint> getWestFPs()
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cr = db.query
                (
                        FINGERPRINT_TABLE,
                        new String[] {FP_ID, X, Y, DIR, AP_INFO},
                        DIR + "='West'",
                        null, null, null, null, null
                );

        ArrayList<Fingerprint> westFPs = new ArrayList<>();

        Fingerprint fp;
        int x,y;
        String d;
        ArrayList<APInfo> apInfos;

        if (!tableIsEmpty())
        {
            if (cr != null)
            {
                cr.moveToFirst();
            }

            do
            {
                x       = Integer.parseInt(cr.getString(1));
                y       = Integer.parseInt(cr.getString(2));
                d       = cr.getString(3);
                apInfos = getAPArray(cr.getString(4));

                fp      = new Fingerprint (x,y,d,apInfos);

                westFPs.add(fp);

            } while (cr.moveToNext());
        }

        db.close();

        return westFPs;
    }
}
