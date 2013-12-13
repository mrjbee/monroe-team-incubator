package org.monroe.team.notification.bridge.android;

import android.content.SharedPreferences;
import android.preference.PreferenceScreen;
import org.monroe.team.libdroid.commons.Should;

public final class SettingAccessor<Type> {

    public static final SettingAccessor<Boolean> SERVICE_ACTIVE = new SettingAccessor<Boolean>("pref_service_active", false);
    public static final SettingAccessor<Boolean> SHARE_NOTIFICATION = new SettingAccessor<Boolean>("pref_out_notif_enable", false);
    public static final SettingAccessor<Boolean> SHARE_OVER_BLUETOOTH = new SettingAccessor<Boolean>("pref_out_notif_bluetooth", true);
    public static final SettingAccessor<?>[] ALL_SETTINGS = new SettingAccessor[]{SERVICE_ACTIVE,SHARE_NOTIFICATION,SHARE_OVER_BLUETOOTH};


    private final String mKey;
    private Type mDefaultValue;


    public static SettingAccessor<?> getByKey(String key) {
        for (SettingAccessor<?> allSetting : ALL_SETTINGS) {
            if (key.equals(allSetting.mKey)){
                return allSetting;
            }
        }
        return null;
    }

    private SettingAccessor(String key, Type defaultValue) {
        mKey = key;
        mDefaultValue = defaultValue;
        Should.beNotNull(key,defaultValue);
    }


    public Type getValue(SharedPreferences preferences){
        if(mDefaultValue instanceof Boolean){
           return (Type)((Boolean)preferences.getBoolean(mKey, (Boolean)mDefaultValue));
        }
        throw Should.failsHere("Unsupported yet type = "+mDefaultValue.getClass());
    }


    @Override
    public String toString() {
        return "SettingAccessor{" +
                "mKey='" + mKey + '\'' +
                ", mDefaultValue=" + mDefaultValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingAccessor that = (SettingAccessor) o;

        if (mKey != null ? !mKey.equals(that.mKey) : that.mKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mKey != null ? mKey.hashCode() : 0;
    }

    public boolean isEnabled(PreferenceScreen preferenceScreen) {
        return preferenceScreen.findPreference(mKey).isEnabled();
    }

    public void setEnable(boolean value, PreferenceScreen preferenceScreen) {
        preferenceScreen.findPreference(mKey).setEnabled(value);
    }


    public boolean isType(Class<?> clazz) {
        return clazz == mDefaultValue.getClass();
    }
}
