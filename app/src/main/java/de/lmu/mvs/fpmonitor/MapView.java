package de.lmu.mvs.fpmonitor;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.ImageView;


public class MapView extends ImageView
{

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context) {
        super(context);
        init();
    }

    private void init()
    {
        Resources res = getResources();
        setImageDrawable(res.getDrawable(R.drawable.wohnungsplan));
    }
}
