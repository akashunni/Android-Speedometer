package com.quintlr.speedometer.Utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.widget.TextView;

import com.quintlr.speedometer.R;

/**
 * Created by akash on 20/6/17.
 */

public class ChangeColor {
    public static void ofButtonDrawableToActive(Context context, AppCompatImageView imageView){
        DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(context, R.color.green));
    }

    public static void ofButtonDrawableToNormal(Context context, AppCompatImageView imageView){
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("theme", true)){
            DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(context, R.color.pureWhite));
        }else {
            DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(context, R.color.pitchBlack));
        }
    }

    public static void ofTextView(TextView textView, int color){
        textView.setTextColor(color);
    }

    public static void ofTextViewToNormal(Context context, TextView textView){
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("theme", true)){
            textView.setTextColor(context.getResources().getColor(R.color.pureWhite));
        }else {
            textView.setTextColor(context.getResources().getColor(R.color.pitchBlack));
        }
    }
}