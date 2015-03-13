package de.lmu.mvs.fpmonitor.Database;

import java.util.ArrayList;

public class Fingerprint
{

    public int x;
    public int y;
    public int e;
    public int d; //direction, degree
    public ArrayList<Measurement> measures;

    public Fingerprint(int x, int y, int e, int d, ArrayList<Measurement> measures)
    {
        this.x = x;
        this.y = y;
        this.e = e;
        this.d = d;
        this.measures = measures;
    }

    @Override
    public String toString()
    {
        String s = "fingerprint: ";
        s+= x + "," + y + "," + e + "," + d + "\n";
        for(Measurement m : measures)
        {
            s+=m.toString() + "\n";
        }
        return s;
    }

}
