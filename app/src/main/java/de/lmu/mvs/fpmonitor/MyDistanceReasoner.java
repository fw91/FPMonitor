package de.lmu.mvs.fpmonitor;

import android.graphics.PointF;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;


public class MyDistanceReasoner
{
    public MyDistanceReasoner()
    {

    }


    /**
     * This method returns an approximation of the current Position.
     * It calls the kNN-Method to get "kValue"-nearest Neighbors.
     * To each of those Fingerprints a weight, based on their Euclidean Distances, is assigned.
     * The final Position is computed and returned.
     *
     * @param fps ArrayList of Fingerprints from the DB
     * @param scanResults Incoming Scan
     * @return An approximate Location (X/Y-Coordinates)
     */
    public PointF getPosition(ArrayList<Fingerprint> fps, List<ScanResult> scanResults)
    {
        int kValue = 4;

        float distances[] = new float[kValue];
        float weights[]   = new float[kValue];
        float x = 0;
        float y = 0;

        ArrayList<Fingerprint> nearest_fps = getKNN(fps,scanResults,kValue);

        // convert ScanResults into APInfo
        ArrayList<APInfo> measures = new ArrayList<>(scanResults.size());
        APInfo m;

        for(ScanResult sr : scanResults)
        {
            m = new APInfo(sr.BSSID, sr.level);
            measures.add(m);
        }

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
     * @param scanResults Incoming Scan
     * @param number Variable of how "many" Nearest Neighbors to compute
     * @return An ArrayList of Fingerprints containing the "number" closest
     */
    private ArrayList<Fingerprint> getKNN(ArrayList<Fingerprint> fps, List<ScanResult> scanResults, int number)
    {
        // convert ScanResults into APInfo
        ArrayList<APInfo> measures = new ArrayList<>(scanResults.size());
        APInfo m;

        for(ScanResult sr : scanResults)
        {
            m = new APInfo(sr.BSSID, sr.level);
            measures.add(m);
        }

        // Get Median of all Fingerprints (for Deterministic FP)
        ArrayList<Fingerprint> medianFps = new ArrayList<>();

        for (Fingerprint f : fps)
        {
            // TODO
            //medianFps.add(calculateMedian(f));
        }

        // Sort all the Fingerprints based on their Distances
        LinkedList<Fingerprint> closest_matches_sorted = new LinkedList<>(medianFps);
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

        // TODO
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
     * This method takes all the Data from the DB, and calculates the Median Value for each unique
     * MAC-Address.
     * This is specifically used for Deterministic-Fingerprinting.
     *
     * @param f The Fingerprint
     * @return The same Fingerprint with rounded APInfo
     */
    // TODO
    /*
    private Fingerprint calculateMedian(Fingerprint f)
    {
        ArrayList<String> macIDs = new ArrayList<>();
        ArrayList<APInfo> roundedAPInfo = new ArrayList<>();
        APInfo tempAPInfo;

        // Get all unique MACs
        for (int i=0;i<f.apList.size();i++)
        {
            if (!macIDs.contains(f.apList.get(i).mac))
            {
                macIDs.add(f.apList.get(i).mac);
            }
        }

        // For each unique MAC found, get the Median
        for (String mac : macIDs)
        {
            double median = 0;
            int ctr       = 0;

            for (int i=0;i<f.apList.size();i++)
            {
                if (mac.equals(f.apList.get(i).mac))
                {
                    median += f.apList.get(i).rssi;
                    ctr++;
                }
            }

            median = (median/ctr);

            tempAPInfo = new APInfo(mac,(int)median);
            roundedAPInfo.add(tempAPInfo);
        }

        return new Fingerprint(f.x_coordinate,f.y_coordinate,f.direction,roundedAPInfo);
    }*/




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
