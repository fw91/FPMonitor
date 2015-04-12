package de.lmu.mvs.fpmonitor.Activities;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.lmu.mvs.fpmonitor.Database.DatabaseHandler;
import de.lmu.mvs.fpmonitor.Database.Fingerprint;
import de.lmu.mvs.fpmonitor.R;


public class Testing_Activity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private ListView list;
    TextView compassDir;

    // Start with some variables
    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    Button updateDir;

    DatabaseHandler DH;

    ArrayList<Fingerprint> test;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        list = (ListView)findViewById(R.id.listView1);
        compassDir = (TextView)findViewById(R.id.compassDir);

        updateDir = (Button)findViewById(R.id.directionUpdate);

        DH = new DatabaseHandler(this);

        test = DH.getFingerprints(74);

        Log.i("FPDATA",test.toString());

        updateDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DH.exportDB(getApplicationContext());
            }
        });




        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

    }


    protected void onPause()
    {
        mSensorManager.unregisterListener(this);

        super.onPause();
    }


    protected void onResume()
    {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

        super.onResume();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
            Log.i("Z-AXIS","Z Value: " + event.values[2]);
            // Make this higher or lower according to how much
            // motion you want to detect

            if(mAccel > 0.5){
                // do something
                compassDir.setText("Moving");
            }
            else
            {
                compassDir.setText("Standing Still");
            }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // not in use
    }
}
