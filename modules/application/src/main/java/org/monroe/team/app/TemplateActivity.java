package org.monroe.team.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 8:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class TemplateActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("pref_service_active".equals(key)){
           boolean serviceBecomeEnabled = (sharedPreferences.getBoolean("pref_service_active",false));
           getPreferenceScreen().findPreference("pref_out_notif_enable").setEnabled(serviceBecomeEnabled);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
