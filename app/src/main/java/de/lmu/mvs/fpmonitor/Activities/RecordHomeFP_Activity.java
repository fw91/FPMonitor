package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.R;

/**
 * This Activity is used for creating a Radio Map.
 * It shows a floor plan with key Positions on it where the recording has to take place.
 * The Record-Button collects all necessary data and stores it into the Database.
 * The Next-Button moves to the next Position.
 */
public class RecordHomeFP_Activity extends Activity
{
    MapView map;
    TextView tv;
    Button btn1, btn2;

    float positions[][] =
            {{57,85},{123,85},{57,139},{123,139},                           // Room1
             {41,203},{107,203},{41,255},{107,255},                         // Room2
             {254,33},                                                      // Room3
             {220,83},{220,136},{220,184},                                  // Room4 etc.
             {177,249},
             {236,249},
             {308,50},{381,50},{308,125},{381,125},
             {456,58},{456,120},
             {302,203},{376,203},{445,203},{302,252},{376,252},{445,252}};
    int posCtr;

    boolean recorded;
    boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_home_fp);

        map      = (MapView)findViewById(R.id.wlan_view);
        tv       = (TextView)findViewById(R.id.recordTV);
        btn1     = (Button)findViewById(R.id.recordBtn);
        btn2     = (Button)findViewById(R.id.nextBtn);

        posCtr   = 0;
        recorded = true;
        started = false;

        btn1.setVisibility(View.GONE);

        tv.setText("Press the Button to start recording.");

        btn1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO implement Fingerprint-Recording-Function
                Toast.makeText(getApplicationContext(),"Fingerprint Recorded",Toast.LENGTH_SHORT).show();
                tv.setText("Press Next.");
                recorded = true;
            }
        });


        btn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!started)
                {
                    btn1.setVisibility(View.VISIBLE);
                    btn2.setText("Next");
                    tv.setText("Walk to the Position and press Record.");
                    started = true;

                    map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                    map.invalidate();

                    posCtr++;
                    recorded = false;
                }
                else
                {
                    if (!recorded)
                    {
                        Toast.makeText(getApplicationContext(),"Please record the FP",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        map.setPosition(positions[posCtr][0],positions[posCtr][1],map.getWidth(),map.getHeight());
                        map.invalidate();

                        if (posCtr==positions.length-1)
                        {
                            //Toast.makeText(getApplicationContext(),"Recording finished",Toast.LENGTH_SHORT).show();
                            btn1.setVisibility(View.GONE);
                            btn2.setVisibility(View.GONE);

                            map.setPosition(0,0,map.getWidth(),map.getHeight());
                            map.invalidate();

                            tv.setText("Recording finished.");
                        }
                        else
                        {
                            posCtr++;
                            recorded = false;
                            tv.setText("Walk to the Position and press Record.");
                        }
                    }
                }
            }
        });


        /*
         * A way of getting all the coordinates needed for the Database
         *
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("Coords","X="+event.getX()+" Y="+event.getY());
                map.setPosition(event.getX(),event.getY(),map.getWidth(),map.getHeight());
                map.invalidate();
                return false;
            }
        });
        */
    }
}
