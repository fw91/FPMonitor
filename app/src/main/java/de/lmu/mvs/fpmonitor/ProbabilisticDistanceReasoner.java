package de.lmu.mvs.fpmonitor;

import android.content.Context;

import java.util.ArrayList;

import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;

/**
 * DistanceReasoner Implementation for Probabilistic DB.
 */
public class ProbabilisticDistanceReasoner
{
    DatabaseHandler DH;
    ArrayList<Fingerprint> dbFingerprints;


    public ProbabilisticDistanceReasoner(Context ctx)
    {
        DH = new DatabaseHandler(ctx);
        dbFingerprints = DH.getProbFingerprints();
    }


}
