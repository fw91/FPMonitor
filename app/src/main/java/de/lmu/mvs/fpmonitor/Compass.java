package de.lmu.mvs.fpmonitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Compass implements SensorEventListener
{
    private static final String TAG = "Compass";

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] mGravity     = new float[3];
    private float[] mGeomagnetic = new float[3];

    private float azimuth        = 0f;
    private String direction     = "";


    public Compass(Context context)
    {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer   = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop()
    {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        final float alpha = 0.97f;

        synchronized (this)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];

                /*
                Log.e(TAG, Float.toString(mGravity[0]));
                Log.e(TAG, Float.toString(mGravity[1]));
                Log.e(TAG, Float.toString(mGravity[2]));
                */
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];

                /*
                Log.e(TAG, Float.toString(event.values[0]));
                Log.e(TAG, Float.toString(event.values[1]));
                Log.e(TAG, Float.toString(event.values[2]));
                */

            }

            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                //Log.d(TAG, "azimuth (rad): " + azimuth);

                azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                azimuth = (azimuth + 360) % 360;

                //Log.d(TAG, "azimuth (deg): " + azimuth);

                if (azimuth >= 315 || azimuth < 45)
                {
                    Log.d("COMPASSDIRECTION","NORTH");
                    direction = "North";
                }
                else if (azimuth >= 45 && azimuth < 135 )
                {
                    Log.d("COMPASSDIRECTION","EAST");
                    direction = "East";
                }
                else if  (azimuth >= 135 && azimuth < 225)
                {
                    Log.d("COMPASSDIRECTION","SOUTH");
                    direction = "South";
                }
                else if (azimuth >= 225 && azimuth < 315)
                {
                    Log.d("COMPASSDIRECTION","WEST");
                    direction = "West";
                }
            }
        }
    }

    public String getDir()
    {
        return this.direction;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
}
