package org.monroe.team.notification.bridge.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.notification.bridge.R;

import java.util.*;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 8:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Map<SettingAccessor<Boolean>, SettingAccessor<?>[]> mSettingAvailabilityMap = new HashMap<SettingAccessor<Boolean>, SettingAccessor<?>[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        //Because of hierarchy involved it`s not recommend to make binding unions,
        //where one bind dst item involved depends on several src items
        bindSettingsAvailability(SettingAccessor.SERVICE_ACTIVE, SettingAccessor.SHARE_NOTIFICATION);
        bindSettingsAvailability(SettingAccessor.SHARE_NOTIFICATION, SettingAccessor.SHARE_OVER_BLUETOOTH);
    }

    private void checkAllAvailabilityBindings() {
        for (SettingAccessor<Boolean> booleanSettingAccessor : mSettingAvailabilityMap.keySet()) {
            checkAvailabilityBindingFor(booleanSettingAccessor);
        }

    }

    private void checkAvailabilityBindingFor(SettingAccessor<Boolean> booleanSettingAccessor) {

        Boolean enabled = booleanSettingAccessor.getValue(getPreferenceScreen().getSharedPreferences());
        List<SettingAccessor<Boolean>> checkAlsoList;

        if (enabled && booleanSettingAccessor.isEnabled(getPreferenceScreen())){
            checkAlsoList = makeAllOf(mSettingAvailabilityMap.get(booleanSettingAccessor), true);
        } else {
            checkAlsoList = makeAllOf(mSettingAvailabilityMap.get(booleanSettingAccessor), false);
        }

        //Because of hierarchy binding should check if just enabled/disabled settings
        //allows to enable/disable other settings....
        //Not too much clever implementation since could be error prone because of order
        for (SettingAccessor<Boolean> settingAccessor : checkAlsoList) {
            checkAvailabilityBindingFor(settingAccessor);
        }

    }

    private List<SettingAccessor<Boolean>> makeAllOf(SettingAccessor<?>[] settingAccessors, boolean enable) {
        if (settingAccessors == null) return Collections.EMPTY_LIST;
        List<SettingAccessor<Boolean>> checkAlsoList = new ArrayList<SettingAccessor<Boolean>>();
        for (SettingAccessor<?> settingAccessor : settingAccessors) {
            settingAccessor.setEnable(enable, getPreferenceScreen());
            if (settingAccessor.isType(Boolean.class)){
                checkAlsoList.add((SettingAccessor<Boolean>) settingAccessor);
            }
        }
        return checkAlsoList;
    }

    private void bindSettingsAvailability(SettingAccessor<Boolean> bindSource, SettingAccessor<Boolean> ... bindTargets) {
        SettingAccessor<?>[] wasValue = mSettingAvailabilityMap.put(bindSource, bindTargets);
        Should.beTrue("There was already binding for = "+bindSource, wasValue == null);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         SettingAccessor<?> accessor = SettingAccessor.getByKey(key);
         if (accessor != null && accessor.isType(Boolean.class)){
             checkAvailabilityBindingFor((SettingAccessor<Boolean>) accessor);
         }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        checkAllAvailabilityBindings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
