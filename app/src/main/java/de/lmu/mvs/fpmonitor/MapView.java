package de.lmu.mvs.fpmonitor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;


public class MapView extends ImageView
{
    Paint myPaint;
    float x,y;
    final static float INITIAL_MAP_HEIGHT = 288;
    final static float INITIAL_MAP_WIDTH  = 492;


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


    private void init()
    {
        Resources res = getResources();
        setImageDrawable(res.getDrawable(R.drawable.wohnungsplan));

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(Color.BLUE);

        x = 0;
        y = 0;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (x != 0 && y != 0)
        {
            canvas.drawCircle(x,y,7,myPaint);
        }
    }


    public void setPosition(float x, float y, float scaledWidth, float scaledHeight)
    {
        this.x = Math.round((x*(scaledWidth/INITIAL_MAP_WIDTH)));
        this.y = Math.round((y*(scaledHeight/INITIAL_MAP_HEIGHT)));
    }
}
