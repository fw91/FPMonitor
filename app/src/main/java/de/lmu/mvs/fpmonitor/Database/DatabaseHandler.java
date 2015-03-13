package de.lmu.mvs.fpmonitor.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "fpDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String FINGERPRINT_TABLE = "fingerprints";

    private static final String FP_ID     = "_id";
    private static final String X         = "x_coordinate";
    private static final String Y         = "y_coordinate";
    private static final String AP_INFO   = "ap_info";

    private static final String createFingerprintDB =
        "CREATE TABLE " + FINGERPRINT_TABLE +
                   " (" + FP_ID   + " TEXT PRIMARY KEY, "
                        + X       + " TEXT, "
                        + Y       + " TEXT, "
                        + AP_INFO + " TEXT)";

    /**
     * Main Constructor
     *
     * @param ctx
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
}
