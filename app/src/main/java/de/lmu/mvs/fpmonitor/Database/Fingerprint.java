package de.lmu.mvs.fpmonitor.Database;

import java.util.ArrayList;

/**
 * Fingerprint Object.
 * Containing:
 * X/Y-Coordinates for the map.
 * Direction the Device was/is/should be facing.
 * An ArrayList of APInformation.
 */
public class Fingerprint
{
    public int x_coordinate;
    public int y_coordinate;
    public String direction;
    public ArrayList<APInfo> apList;


    /**
     * Main Constructor.
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @param d Direction
     * @param apList ArrayList of APInformation
     */
    public Fingerprint(int x, int y, String d, ArrayList<APInfo> apList)
    {
        this.x_coordinate = x;
        this.y_coordinate = y;
        this.direction    = d;
        this.apList       = apList;
    }


    /**
     * ToString method.
     * @return The Fingerprint in readable version.
     */
    public String toString()
    {
        String apInformation = "";
        for(int i=0;i<apList.size();i++)
        {
            apInformation += "AP."+(i+1)+": "+apList.get(i).toString()+"\n";
        }
        return "Fingerprint: X/Y="+x_coordinate+"/"+y_coordinate+", Dir="+direction+"\n"+apInformation;
    }
}
