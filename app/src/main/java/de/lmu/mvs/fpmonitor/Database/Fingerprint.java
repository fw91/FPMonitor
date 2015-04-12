package de.lmu.mvs.fpmonitor.Database;

import java.util.ArrayList;

/**
 * Fingerprint Object.
 * Containing:
 * X/Y-Coordinates for the map.
 * Direction the Device was/is/should be facing.
 * An ArrayList of Scans.
 */
public class Fingerprint
{
    public int x_coordinate;
    public int y_coordinate;
    public int direction;
    public ArrayList<WifiScan> scans;


    /**
     * Main Constructor.
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @param d Direction
     * @param scans ArrayList of WifiScans
     */
    public Fingerprint(int x, int y, int d, ArrayList<WifiScan> scans)
    {
        this.x_coordinate = x;
        this.y_coordinate = y;
        this.direction    = d;
        this.scans        = scans;
    }


    /**
     * ToString method.
     * @return The Fingerprint in readable version.
     */
    public String toString()
    {
        String Scans = "";

        for(int i=0;i<scans.size();i++)
        {
            Scans += scans.get(i).toString()+"\n";
        }

        return "Fingerprint-Data: \nX/Y = " + x_coordinate + "/" + y_coordinate + ", \nDir = " + direction + "\n" + Scans;
    }
}
