package de.lmu.mvs.fpmonitor.Database;

public class APInfo
{
    public String mac;
    public int rssi;

    public APInfo(String mac, int rssi)
    {
        this.mac = mac;
        this.rssi = rssi;
    }
}
