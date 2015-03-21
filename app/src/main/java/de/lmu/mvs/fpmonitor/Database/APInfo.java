package de.lmu.mvs.fpmonitor.Database;

/**
 * APInfo Object.
 * Containing:
 * Unique MAC-Address.
 * Received Signal Strength Information.
 */
public class APInfo
{
    public String mac;
    public int rssi;


    /**
     * Main Constructor.
     * @param mac MAC-Address
     * @param rssi Received Signal Strength Information
     */
    public APInfo(String mac, int rssi)
    {
        this.mac = mac;
        this.rssi = rssi;
    }


    /**
     * ToString method.
     * @return The APInfo in readable version.
     */
    public String toString()
    {
        return "mac="+mac+", rssi="+rssi;
    }
}
