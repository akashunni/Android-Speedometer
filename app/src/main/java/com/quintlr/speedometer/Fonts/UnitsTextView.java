package com.quintlr.speedometer.Fonts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Akash on 7/19/2016.
 */
public class UnitsTextView extends TextView {
    public UnitsTextView(Context context) {
        super(context);
        init();
    }

    public UnitsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UnitsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(!isInEditMode())
            init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UnitsTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if(!isInEditMode())
            init();
    }
    private void init(){
        if(!isInEditMode()){
            Typeface typeface_units = FontFactory.getInstance().getFontForUnits(getContext());
            setTypeface(typeface_units);
        }

    }
}
