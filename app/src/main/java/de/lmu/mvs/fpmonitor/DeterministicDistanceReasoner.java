package de.lmu.mvs.fpmonitor;

import android.content.Context;
import android.graphics.PointF;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;


/**
 * DistanceReasoner Implementation based on algorithm suggested by SmartPos for Deterministic DB.
 */
public class DeterministicDistanceReasoner
{
    DatabaseHandler DH;
    ArrayList<Fingerprint> dbFingerprints;

    public DeterministicDistanceReasoner(Context ctx)
    {
        DH = new DatabaseHandler(ctx);
        dbFingerprints = DH.getDetFingerprints();
    }


    /**
     * This method returns an approximation of the current Position.
     * It calls the kNN-Method to get "kValue"-nearest Neighbors.
     * To each of those Fingerprints a weight, based on their Euclidean Distances, is assigned.
     * The final Position is computed and returned.
     *
     * @param direction Current Direction the Device is facing
     * @param scanResults Incoming Scan
     * @return An approximate Location (X/Y-Coordinates)
     */
    public PointF getPosition(int direction, List<ScanResult> scanResults)
    {
        int kValue = 4;

        float distances[] = new float[kValue];
        float weights[]   = new float[kValue];
        float x = 0;
        float y = 0;

        int minDirection, maxDirection;

        // Calculate min/max Direction (360°)
        if (direction < 50)
        {
            minDirection = direction + 360 - 50;
            maxDirection = direction + 50;
        }
        else if (310 < direction)
        {
            minDirection = direction - 50;
            maxDirection = direction - 360 + 50;
        }
        else
        {
            minDirection = direction - 50;
            maxDirection = direction + 50;
        }

        ArrayList<Fingerprint> currentFingerprints = new ArrayList<>();

        for (Fingerprint f : dbFingerprints)
        {
            if (minDirection < f.direction && f.direction < maxDirection)
            {
                currentFingerprints.add(f);
            }
        }

        // convert ScanResults into APInfo
        ArrayList<APInfo> measures = new ArrayList<>(scanResults.size());
        APInfo m;

        for(ScanResult sr : scanResults)
        {
            m = new APInfo(sr.BSSID, sr.level);
            measures.add(m);
        }

        ArrayList<Fingerprint> nearest_fps = getKNN(currentFingerprints,measures,kValue);

        // get distances for each FP
        for (int i=0;i<nearest_fps.size();i++)
        {
            distances[i] = distance(nearest_fps.get(i),measures);
        }

        // assign weights to each FP
        for (int i=0;i<nearest_fps.size();i++)
        {
            float tempWeight = 0;

            for (int j=0;j<nearest_fps.size();j++)
            {
                tempWeight += 1/distances[j];
            }

            weights[i] = 1/(distances[i] * tempWeight);
        }

        // get approximate x-/y-coordinates
        for (int i=0;i<nearest_fps.size();i++)
        {
            x += nearest_fps.get(i).x_coordinate * weights[i];
            y += nearest_fps.get(i).y_coordinate * weights[i];
        }

        return new PointF(x,y);
    }


    /**
     * This method represents the k-Nearest Neighbors algorithm.
     * It computes the closest matches of Fingerprints from the Database compared to the last scan
     * based on RSSI values and the number of Fingerprints to be returned.
     *
     * @param fps ArrayList of Fingerprints from the DB
     * @param measures Incoming Scan
     * @param number Variable of how "many" Nearest Neighbors to compute
     * @return An ArrayList of Fingerprints containing the "number" closest
     */
    private ArrayList<Fingerprint> getKNN(ArrayList<Fingerprint> fps, ArrayList<APInfo> measures, int number)
    {
        // Sort all the Fingerprints based on their Distances
        LinkedList<Fingerprint> closest_matches_sorted = new LinkedList<>(fps);
        ArrayList<Fingerprint> n_closest_matches = new ArrayList<>(number);
        final ArrayList<APInfo> measures_final = (ArrayList<APInfo>) measures.clone();

        Collections.sort(
                closest_matches_sorted,
                new Comparator<Fingerprint>()
                    {
                        @Override
                        public int compare(Fingerprint f1, Fingerprint f2)
                        {
                            float d1 = distance(f1, measures_final);
                            float d2 = distance(f2, measures_final);

                            if (d1 < d2)
                            {
                                return -1;
                            }
                            if (d1 > d2)
                            {
                                return 1;
                            }
                            else
                            {
                                return 0;
                            }
                        }
                    });

        for(int i=0;i<number;i++)
        {
            n_closest_matches.add(closest_matches_sorted.removeFirst());
        }

        return n_closest_matches;
    }


    /**
     * Euclidean Distance.
     *
     * @param f The Fingerprint
     * @param measures Incoming Measures
     * @return The Euclidean Distance of all MACs RSSI-Values combined
     */
    private float distance(Fingerprint f, ArrayList<APInfo> measures)
    {
        float distance = 0f;

        for(APInfo m : f.scans.get(0).apInfoList)
        {
            for(APInfo n : measures)
            {
                if(m.mac.equals(n.mac))
                {
                    distance += (m.rssi-n.rssi) * (m.rssi-n.rssi);

                    break;
                }
            }
        }

        distance = (float)Math.sqrt(distance);

        return distance;
    }


    /**
     * Get the Fingerprint with the minimal Distance to the received ScanResults using Euclidean Distance.
     * This method is very prone to failure, therefore it should not be used.
     *
     * @param fps ArrayList of Fingerprints from the DB
     * @param scanResults Incoming Scan
     * @return The Fingerprint matching the criteria
     */
    public Fingerprint getClosestMatch(ArrayList<Fingerprint> fps, List<ScanResult> scanResults)
    {
        ArrayList<APInfo> measures = new ArrayList<>(scanResults.size());
        APInfo m;

        for(ScanResult sr : scanResults)
        {
            m = new APInfo(sr.BSSID, sr.level);
            measures.add(m);
        }

        int index = 0;
        float min_distance = -1f;

        for(int i=0;i<fps.size();i++)
        {
            Fingerprint f = fps.get(i);
            float d = distance(f, measures);

            if(d < min_distance || min_distance < 0)
            {
                min_distance = d;
                index = i;
            }
        }

        return fps.get(index);
    }
}
