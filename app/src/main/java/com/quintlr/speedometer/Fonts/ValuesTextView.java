package com.quintlr.speedometer.Fonts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.quintlr.speedometer.Fonts.FontFactory;

/**
 * Created by Akash on 7/19/2016.
 */
public class ValuesTextView extends TextView {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ValuesTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if(!isInEditMode())
            init();
    }

    public ValuesTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(!isInEditMode())
            init();
    }

    public ValuesTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ValuesTextView(Context context) {
        super(context);
        init();
    }

    private void init(){
        if(!isInEditMode()){
            Typeface typeface_units = FontFactory.getInstance().getFontForValues(getContext());
            setTypeface(typeface_units);
        }
    }
}
