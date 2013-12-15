package org.monroe.team.notification.bridge.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;
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

    private final static int REQUEST_ENABLE_BT = Integer.MAX_VALUE;

    private Map<SettingAccessor<Boolean>, SettingAccessor<?>[]> mSettingAvailabilityMap = new HashMap<SettingAccessor<Boolean>, SettingAccessor<?>[]>();
    private Map<SettingAccessor<?>, VoidClosure<SharedPreferences>> mSettingAccessorActionMap = new HashMap<SettingAccessor<?>, VoidClosure<SharedPreferences>>();
    private ModelProvider<NotificationBridgeManager> mModelProvider;
    private NotificationBridgeManager mBridgeManager;
    private Set<SettingAccessor<Boolean>> mUnsupportedSettingSet = new HashSet<SettingAccessor<Boolean>>(3);

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
        bindSettingAction(SettingAccessor.SHARE_OVER_BLUETOOTH, new VoidClosure<SharedPreferences>() {
            @Override
            public void call(SharedPreferences in) {
                boolean enable = SettingAccessor.SHARE_OVER_BLUETOOTH.getValue(in);
                if (enable){
                    if (!mBridgeManager.isBluetoothGatewayEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        mBridgeManager.activateBluetooth();
                    }
                } else {
                    mBridgeManager.deactivateBluetooth();
                }
            }
        });
        SettingAccessor.SERVICE_ACTIVE.setEnable(false, getPreferenceScreen());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                mBridgeManager.activateBluetooth();
            } else if (resultCode == RESULT_CANCELED){
                SettingAccessor.SHARE_OVER_BLUETOOTH.setValue(false, getPreferenceScreen());
            }
        }
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
             settingAccessor.setEnable(mUnsupportedSettingSet.contains(settingAccessor)? false: enable, getPreferenceScreen());
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
            } else {
                mBridgeManager.onSettingChange(accessor);
            }
        } else {
           Should.fails("Unknown setting = "+key);
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
        if (!mBridgeManager.isBluetoothGatewaySupported()){
            mUnsupportedSettingSet.add(SettingAccessor.SHARE_OVER_BLUETOOTH);
        }
        SettingAccessor.SERVICE_ACTIVE.setEnable(true, getPreferenceScreen());
        checkAllAvailabilityBindings();
    }

    @Override
    public void onRelease(NotificationBridgeManager notificationBridgeManager) {
        mBridgeManager = null;
    }
}
