package org.monroe.team.notification.bridge.android;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import org.monroe.team.libdroid.commons.Should;

public final class SettingAccessor<Type> {

    public static final SettingAccessor<Boolean> SERVICE_ACTIVE = new SettingAccessor<Boolean>("pref_service_active", false);
    public static final SettingAccessor<Boolean> SHARE_NOTIFICATION = new SettingAccessor<Boolean>("pref_out_notif_enable", false);
    public static final SettingAccessor<Boolean> ACCEPT_NOTIFICATION = new SettingAccessor<Boolean>("pref_in_notif_enable", false);
    public static final SettingAccessor<Boolean> SHARE_OVER_BLUETOOTH = new SettingAccessor<Boolean>("pref_out_notif_bluetooth", false);
    public static final SettingAccessor<Boolean> ACCEPT_OVER_BLUETOOTH = new SettingAccessor<Boolean>("pref_in_notif_bluetooth", false);

    public static final SettingAccessor<?>[] ALL_SETTINGS = new SettingAccessor[]{
            SERVICE_ACTIVE,
            ACCEPT_NOTIFICATION,
            SHARE_NOTIFICATION,
            ACCEPT_OVER_BLUETOOTH,
            SHARE_OVER_BLUETOOTH};


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

    public void setValue(Type value, PreferenceScreen preferenceScreen) {
        Preference preference = preferenceScreen.findPreference(mKey);
        Should.beNotNull(value);
        if(mDefaultValue instanceof Boolean){
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            checkBoxPreference.setChecked((Boolean) value);
            return;
        }
        throw Should.failsHere("Unsupported yet type = "+mDefaultValue.getClass());
    }

    public void setValue(Type value, SharedPreferences in) {
        Should.beNotNull(value);
        if(mDefaultValue instanceof Boolean){
            in.edit().putBoolean(mKey, (Boolean)value);
            return;
        }
        throw Should.failsHere("Unsupported yet type = "+mDefaultValue.getClass());
    }
}
