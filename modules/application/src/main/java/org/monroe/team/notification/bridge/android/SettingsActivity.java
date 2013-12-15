package org.monroe.team.notification.bridge.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.libdroid.commons.VoidClosure;
import org.monroe.team.libdroid.mservice.ModelProvider;
import org.monroe.team.notification.bridge.R;

import java.util.*;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 8:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class SettingsActivity extends PreferenceActivity
                        implements SharedPreferences.OnSharedPreferenceChangeListener,
                        ModelProvider.ModelProviderOwner<NotificationBridgeManager>{

    private Map<SettingAccessor<Boolean>, SettingAccessor<?>[]> mSettingAvailabilityMap = new HashMap<SettingAccessor<Boolean>, SettingAccessor<?>[]>();
    private Map<SettingAccessor<?>, VoidClosure<SharedPreferences>> mSettingAccessorActionMap = new HashMap<SettingAccessor<?>, VoidClosure<SharedPreferences>>();
    private ModelProvider<NotificationBridgeManager> mModelProvider;
    private NotificationBridgeManager mBridgeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModelProvider = new ModelProvider<NotificationBridgeManager>(this, NotificationBridgeService.class);
        mModelProvider.obtain();
        addPreferencesFromResource(R.xml.preferences);
        //Because of hierarchy involved it`s not recommend to make binding unions,
        //where one bind dst item involved depends on several src items
        bindSettingAvailability(SettingAccessor.SERVICE_ACTIVE, SettingAccessor.SHARE_NOTIFICATION);
        bindSettingAvailability(SettingAccessor.SHARE_NOTIFICATION, SettingAccessor.SHARE_OVER_BLUETOOTH);

        bindSettingAction(SettingAccessor.SERVICE_ACTIVE, new VoidClosure<SharedPreferences>() {
            @Override
            public void call(SharedPreferences in) {
                boolean value = SettingAccessor.SERVICE_ACTIVE.getValue(in);
                if (value){
                    mBridgeManager.activate();
                }else {
                    mBridgeManager.disable();
                }
            }
        });
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

    private void bindSettingAvailability(SettingAccessor<Boolean> bindSource, SettingAccessor<Boolean>... bindTargets) {
        SettingAccessor<?>[] wasValue = mSettingAvailabilityMap.put(bindSource, bindTargets);
        Should.beTrue("There was already binding for = "+bindSource, wasValue == null);
    }

    private void bindSettingAction(SettingAccessor<?> setting, VoidClosure<SharedPreferences> action) {
        VoidClosure<SharedPreferences> oldValue = mSettingAccessorActionMap.put(setting, action);
        Should.beTrue("There was already action binding for = "+setting, oldValue == null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SettingAccessor<?> accessor = SettingAccessor.getByKey(key);

        if (accessor != null && accessor.isType(Boolean.class)){
         checkAvailabilityBindingFor((SettingAccessor<Boolean>) accessor);
        }

        if (accessor != null){
            VoidClosure<SharedPreferences> action = mSettingAccessorActionMap.get(accessor);
            if (action != null){
                action.call(sharedPreferences);
            }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mModelProvider.releaseAndDestroy();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onObtain(NotificationBridgeManager notificationBridgeManager) {
        mBridgeManager = notificationBridgeManager;
    }

    @Override
    public void onRelease(NotificationBridgeManager notificationBridgeManager) {
        mBridgeManager = null;
    }
}
