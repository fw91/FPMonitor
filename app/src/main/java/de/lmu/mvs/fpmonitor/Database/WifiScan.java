package de.lmu.mvs.fpmonitor.Database;

import java.util.ArrayList;

/**
 * WifiScan Object.
 * Containing:
 * Unique identifier.
 * An ArrayList of APInformation.
 */
public class WifiScan
{
    public int id;
    public ArrayList<APInfo> apInfoList;


    /**
     * Main Constructor
     * @param id Identifier
     * @param apInfoList ArrayList of APInformation
     */
    public WifiScan(int id, ArrayList<APInfo> apInfoList)
    {
        this.id = id;
        this.apInfoList = apInfoList;
    }


    /**
     * ToString Method.
     * @return The Scan in readable version.
     */
    public String toString()
    {
        String Scans = "Scan " + id + ".: ";

        for(int i=0;i<apInfoList.size();i++)
        {
            Scans += apInfoList.get(i).toString()+"; ";
        }

        return Scans;
    }
}
