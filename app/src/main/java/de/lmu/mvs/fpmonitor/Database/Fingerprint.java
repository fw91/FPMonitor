package de.lmu.mvs.fpmonitor.Database;

import java.util.ArrayList;

public class Fingerprint
{

    public int x_coordinate;
    public int y_coordinate;
    public int direction;
    public ArrayList<APInfo> apList;

    public Fingerprint(int x, int y, int d, ArrayList<APInfo> apList)
    {
        this.x_coordinate = x;
        this.y_coordinate = y;
        this.direction    = d;
        this.apList       = apList;
    }
}
