package de.lmu.mvs.fpmonitor;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.lmu.mvs.fpmonitor.Database.APInfo;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;

public class DistanceReasoner
{

    public Fingerprint compareClosest(ArrayList<Fingerprint> fingerprints, List<ScanResult> aps)
    {
        ArrayList<APInfo> measures = new ArrayList<>(aps.size());
        APInfo m;

        for(ScanResult sr : aps)
        {
            m = new APInfo(sr.BSSID, sr.level);
            measures.add(m);
        }

        float min_distance = -1f;
        int closest_index = 0;

        for(int i=0;i<fingerprints.size();i++)
        {
            Fingerprint f = fingerprints.get(i);
            float d = distance(f, measures);

            if(d < min_distance || min_distance < 0)
            {
                min_distance = d;
                closest_index = i;
            }
        }

        return fingerprints.get(closest_index);
    }


    public ArrayList<Fingerprint> compareClosest(ArrayList<Fingerprint> fingerprints, List<ScanResult> aps, int number)
    {
        ArrayList<APInfo> measures = new ArrayList<>(aps.size());
        LinkedList<Fingerprint> closest_matches_sorted = new LinkedList<>(fingerprints);

        ArrayList<Fingerprint> n_closest_matches = new ArrayList<>(number);

        APInfo m;

        for(ScanResult sr : aps)
        {
            m = new APInfo(sr.BSSID, sr.level);
            measures.add(m);
        }

        final ArrayList<APInfo> measures_final = (ArrayList<APInfo>) measures.clone();

        Collections.sort(closest_matches_sorted, new Comparator<Fingerprint>()
        {
            @Override
            public int compare(Fingerprint f1, Fingerprint f2)
            {
                float d1 = distance(f1, measures_final);
                float d2 = distance(f2, measures_final);

                if(d1 < d2)
                {
                    return -1;
                }
                if(d1 > d2)
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


    private float distance(Fingerprint f, ArrayList<APInfo> measures)
    {
        float distance = 0f;

        for(APInfo m : f.apList)
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
}

/*
// TODO USE INSIDE FINGERPRINTING ACTIVITY
class WifiReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.e("WifiReceiver", "received scan...");
			if (wifi != null) {
				List<ScanResult> aps = wifi.getScanResults();
		        Fingerprint f = new Fingerprint((int)record_x, (int)record_y, view.level, (int)heading, new ArrayList<Measurement>());
		        for(ScanResult ap : aps){
		            f.measures.add(new Measurement(ap.BSSID, ap.level));
				}

		        if(record){
		        	view.fingerprints.add(f);
		        	record = false;
//		        	Log.e("WifiReceiver", "added fingerprint...");
		        	view.invalidate();
		        }
		        if(positioning){
		        	view.position = r.compareClosest(view.fingerprints, aps);
//		        	Log.e("WifiReceiver", "computed position...");
		        	view.invalidate();
		        	wifi.startScan();
		        }
			}
		}
	}
 */
