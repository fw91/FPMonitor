package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.lmu.mvs.fpmonitor.Compass;
import de.lmu.mvs.fpmonitor.R;

/**
 * Placeholder for now.
 */
public class Placeholder_Activity extends Activity
{
    private Compass compass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        compass = new Compass(this);

        Button btn = (Button)findViewById(R.id.btn);
        final TextView tv  = (TextView)findViewById(R.id.tv);

        tv.setText("Press the Button");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("Dir: " + compass.getDir());
            }
        });
    }

    protected void onResume()
    {
        compass.start();
        super.onResume();
    }

    protected void onPause()
    {
        compass.stop();
        super.onPause();
    }
}
