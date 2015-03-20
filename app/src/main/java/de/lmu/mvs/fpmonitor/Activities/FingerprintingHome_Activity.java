package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import de.lmu.mvs.fpmonitor.MapView;
import de.lmu.mvs.fpmonitor.R;

/**
 * This Activity will show Your current Position on a floor Plan.
 * It is necessary to Record Your Database first, so there is data to evaluate.
 */
public class FingerprintingHome_Activity extends Activity
{
    MapView map;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fphome);

        map = (MapView)findViewById(R.id.wlan_view);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setPosition(positions[posCtr][0], positions[posCtr][1], map.getWidth(), map.getHeight());
                map.invalidate();
                if (posCtr == positions.length - 1) {
                    posCtr = 0;
                } else {
                    posCtr++;
                }
                //Log.i("Dimensions","WIDTH="+map.getWidth()+" HEIGHT="+map.getHeight());
            }
        });
    }
}
