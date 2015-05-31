package de.lmu.mvs.fpmonitor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * The MapView-Object.
 * Can update and print the current Position.
 */
public class MapView extends ImageView
{
    Paint myPaint;
    float x,y;

    // Required to store values of initial Map-Dimensions for future scaling operations.
    final static float INITIAL_MAP_HEIGHT = 439;
    final static float INITIAL_MAP_WIDTH = 704;


    public MapView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }


    public MapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }


    public MapView(Context context)
    {
        super(context);
        init();
    }


    /**
     * Initialize
     */
    private void init()
    {
        Resources res = getResources();
        setImageDrawable(res.getDrawable(R.drawable.oettingenplan));

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(Color.RED);

        x = 0;
        y = 0;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (x != 0 && y != 0)
        {
            canvas.drawCircle(x,y,4,myPaint);
        }

        //drawAllFingerprints(canvas);

        /* Training-Data
        canvas.drawCircle(250,37,4,myPaint);
        canvas.drawCircle(328,55,4,myPaint);
        canvas.drawCircle(356,44,4,myPaint);
        canvas.drawCircle(369,77,4,myPaint);
        canvas.drawCircle(551,32,4,myPaint);
        canvas.drawCircle(621,43,4,myPaint);
        canvas.drawCircle(311,137,4,myPaint);
        canvas.drawCircle(385,156,4,myPaint);
        canvas.drawCircle(362,243,4,myPaint);
        canvas.drawCircle(319,186,4,myPaint);
        canvas.drawCircle(319,206,4,myPaint);
        canvas.drawCircle(304,286,4,myPaint);
        canvas.drawCircle(404,300,4,myPaint);
        canvas.drawCircle(360,369,4,myPaint);
        canvas.drawCircle(316,362,4,myPaint);
        canvas.drawCircle(297,412,4,myPaint);
        canvas.drawCircle(356,401,4,myPaint);
        canvas.drawCircle(406,354,4,myPaint);
        canvas.drawCircle(427,381,4,myPaint);
        canvas.drawCircle(389,397,4,myPaint);
        */
    }


    /**
     * Set the Coordinates for current Position.
     * Scales the values of x and y for different Map-Sizes, based on the dimensions present at
     * recording.
     * @param x x-Coordinate
     * @param y y-Coordinate
     * @param scaledWidth current Map-Width (needed for Scaling)
     * @param scaledHeight current Map-Height (needed for Scaling)
     */
    public void setPosition(float x, float y, float scaledWidth, float scaledHeight)
    {
        float xScaleFactor = scaledWidth / INITIAL_MAP_WIDTH;
        float yScaleFactor = scaledHeight / INITIAL_MAP_HEIGHT;

        this.x = x * xScaleFactor;
        this.y = y * yScaleFactor;
    }


    /**
     * Method to print every Fingerprint on the Map.
     * Can only be done locally inside this class.
     * @param canvas Canvas to paint on
     */
    private void drawAllFingerprints(Canvas canvas)
    {
        canvas.drawCircle(15,34,4,myPaint); // Gang oben
        canvas.drawCircle(45,34,4,myPaint);
        canvas.drawCircle(75,34,4,myPaint);
        canvas.drawCircle(105,34,4,myPaint);
        canvas.drawCircle(135,34,4,myPaint);
        canvas.drawCircle(165,34,4,myPaint);
        canvas.drawCircle(195,34,4,myPaint);
        canvas.drawCircle(225,34,4,myPaint);
        canvas.drawCircle(255,34,4,myPaint);
        canvas.drawCircle(285,34,4,myPaint);
        canvas.drawCircle(315,34,4,myPaint);
        canvas.drawCircle(345,34,4,myPaint);
        canvas.drawCircle(375,34,4,myPaint);
        canvas.drawCircle(405,34,4,myPaint);
        canvas.drawCircle(435,34,4,myPaint);
        canvas.drawCircle(465,34,4,myPaint);
        canvas.drawCircle(495,34,4,myPaint);
        canvas.drawCircle(525,34,4,myPaint);
        canvas.drawCircle(555,34,4,myPaint);
        canvas.drawCircle(585,34,4,myPaint);
        canvas.drawCircle(615,34,4,myPaint);
        canvas.drawCircle(645,34,4,myPaint);
        canvas.drawCircle(675,34,4,myPaint);

        canvas.drawCircle(295,62,4,myPaint); // Herrenklo
        canvas.drawCircle(295,75,4,myPaint);

        canvas.drawCircle(328,68,4,myPaint); // Damenklo

        canvas.drawCircle(360,63,4,myPaint); // Mittelstück
        canvas.drawCircle(360,94,4,myPaint);
        canvas.drawCircle(381,63,4,myPaint);
        canvas.drawCircle(381,94,4,myPaint);

        canvas.drawCircle(408,68,4,myPaint); // Treppen
        canvas.drawCircle(429,86,4,myPaint);
        canvas.drawCircle(408,103,4,myPaint);

        canvas.drawCircle(299,120,4,myPaint); // Room G 002
        canvas.drawCircle(325,120,4,myPaint);
        canvas.drawCircle(299,141,4,myPaint);
        canvas.drawCircle(325,141,4,myPaint);

        canvas.drawCircle(299,184,4,myPaint); // Room G 004
        canvas.drawCircle(325,184,4,myPaint);
        canvas.drawCircle(299,205,4,myPaint);
        canvas.drawCircle(325,205,4,myPaint);

        canvas.drawCircle(299,237,4,myPaint); // Room G 006
        canvas.drawCircle(325,237,4,myPaint);
        canvas.drawCircle(299,258,4,myPaint);
        canvas.drawCircle(325,258,4,myPaint);

        canvas.drawCircle(299,289,4,myPaint); // Room G 008
        canvas.drawCircle(325,289,4,myPaint);
        canvas.drawCircle(299,310,4,myPaint);
        canvas.drawCircle(325,310,4,myPaint);

        canvas.drawCircle(299,348,4,myPaint); // Room G 010
        canvas.drawCircle(325,348,4,myPaint);
        canvas.drawCircle(312,378,4,myPaint);
        canvas.drawCircle(299,409,4,myPaint);
        canvas.drawCircle(325,409,4,myPaint);

        canvas.drawCircle(393,144,4,myPaint); // Room G 001
        canvas.drawCircle(419,144,4,myPaint);

        canvas.drawCircle(393,184,4,myPaint); // Room G 003
        canvas.drawCircle(419,184,4,myPaint);
        canvas.drawCircle(393,205,4,myPaint);
        canvas.drawCircle(419,205,4,myPaint);

        canvas.drawCircle(393,237,4,myPaint); // Room G 005
        canvas.drawCircle(419,237,4,myPaint);
        canvas.drawCircle(393,258,4,myPaint);
        canvas.drawCircle(419,258,4,myPaint);

        canvas.drawCircle(393,289,4,myPaint); // Room G 007
        canvas.drawCircle(419,289,4,myPaint);
        canvas.drawCircle(393,310,4,myPaint);
        canvas.drawCircle(419,310,4,myPaint);

        canvas.drawCircle(393,348,4,myPaint); // Room G 009
        canvas.drawCircle(419,348,4,myPaint);
        canvas.drawCircle(406,378,4,myPaint);
        canvas.drawCircle(393,409,4,myPaint);
        canvas.drawCircle(419,409,4,myPaint);

        canvas.drawCircle(359,138,4,myPaint); // Room G 080 (Gang)
        canvas.drawCircle(359,168,4,myPaint);
        canvas.drawCircle(359,198,4,myPaint);
        canvas.drawCircle(359,228,4,myPaint);
        canvas.drawCircle(359,258,4,myPaint);
        canvas.drawCircle(359,288,4,myPaint);
        canvas.drawCircle(359,318,4,myPaint);
        canvas.drawCircle(359,348,4,myPaint);
        canvas.drawCircle(359,378,4,myPaint);
        canvas.drawCircle(359,408,4,myPaint);
    }
}
