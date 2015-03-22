package de.lmu.mvs.fpmonitor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * A Very Simple Compass using Magnetometer and Accelerometer.
 */
public class Compass implements SensorEventListener
{
    final static String COMPASS_UPDATE = "compass_updated";
    Intent compassUpdate = new Intent(COMPASS_UPDATE);

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] mGravity     = new float[3];
    private float[] mGeomagnetic = new float[3];

    private String direction     = "";

    private Context ctx;


    /**
     * Main Constructor
     * @param context Context from Activity calling
     */
    public Compass(Context context)
    {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer   = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.ctx = context;
    }


    /**
     * Register Listeners.
     */
    public void start()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * Unregister Listeners.
     */
    public void stop()
    {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event)
    {
        final float alpha = 0.97f;
        float azimuth;
        String newDirection;

        synchronized (this)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                if (azimuth >= 315 || azimuth < 45)
                {
                    newDirection = "N";

                    if (!direction.equals(newDirection))
                    {
                        compassUpdate.putExtra("direction","North");
                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(compassUpdate);
                        direction = "N";
                    }
                }
                else if (azimuth >= 45 && azimuth < 135 )
                {
                    newDirection = "E";

                    if (!direction.equals(newDirection))
                    {
                        compassUpdate.putExtra("direction","East");
                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(compassUpdate);
                        direction = "E";
                    }
                }
                else if  (azimuth >= 135 && azimuth < 225)
                {
                    newDirection = "S";

                    if (!direction.equals(newDirection))
                    {
                        compassUpdate.putExtra("direction","South");
                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(compassUpdate);
                        direction = "S";
                    }
                }
                else if (azimuth >= 225 && azimuth < 315)
                {
                    newDirection = "W";

                    if (!direction.equals(newDirection))
                    {
                        compassUpdate.putExtra("direction","West");
                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(compassUpdate);
                        direction = "W";
                    }
                }
            }
        }
    }


    /**
     * TODO Delete, only used in Testing_Activity
     * Retrieve direction the Phone is currently facing.
     * (North/East/South/West)
     * @return direction
     */
    public String getDir()
    {
        return this.direction;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //not in use
    }
}
