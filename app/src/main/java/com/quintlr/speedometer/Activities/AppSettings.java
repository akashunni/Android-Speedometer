package com.quintlr.speedometer.Activities;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;

import com.quintlr.speedometer.R;

public class AppSettings extends PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AppSettingsFragment())
                .commit();
    }

    public static class AppSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
        String TAG = "TEST";
        private static MapStyleChangeListener mapStyleChangeListener;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onStart() {
            super.onStart();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("mapStyle")){
                mapStyleChangeListener.onMapStyleChanged();
            }
        }

        public static void setOnMapStyleChangeListener(MapStyleChangeListener mapStyleChangeListener){
            AppSettingsFragment.mapStyleChangeListener = mapStyleChangeListener;
        }

        public interface MapStyleChangeListener{
            void onMapStyleChanged();
        }
    }
}