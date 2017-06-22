package com.akashunni.speedometer.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.akashunni.speedometer.R;

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
        private static AppSettingsChangeListener appSettingsChangeListener;
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
                appSettingsChangeListener.onMapStyleChanged();
            }
            if (key.equals("theme")){
                appSettingsChangeListener.onAppThemeChanged();
            }
            if (key.equals("lcdBacklit")){
                appSettingsChangeListener.onBacklitChanged();
            }
            if (key.equals("DMS")){
                appSettingsChangeListener.onLocationUnitsChanged();
            }
            if (key.equals("precision")){
                appSettingsChangeListener.onPrecisionChanged();
            }
        }

        public static void setOnMapStyleChangeListener(AppSettingsChangeListener appSettingsChangeListener){
            AppSettingsFragment.appSettingsChangeListener = appSettingsChangeListener;
        }

        public interface AppSettingsChangeListener {
            void onMapStyleChanged();
            void onAppThemeChanged();
            void onBacklitChanged();
            void onLocationUnitsChanged();
            void onPrecisionChanged();
        }
    }
}