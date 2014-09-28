package org.monroe.team.smooker.app.common;

import android.content.SharedPreferences;

import java.util.Date;

public class Settings {

    public static final SettingItem<Long> LAST_SMOKE_SUGGESTED_DATE = new SettingItem<Long>("LAST_SMOKE_SUGGESTED_DATE",Long.class, -1L);

    public static final SettingItem<Integer> AVERAGE_SMOKE = new SettingItem<Integer>("AVERAGE_SMOKE",Integer.class, null);

    public static final SettingItem<Integer> QUITE_START_SMOKE = new SettingItem<Integer>("QUITE_START_SMOKE",Integer.class, null);
    public static final SettingItem<Integer> QUITE_END_SMOKE = new SettingItem<Integer>("DESIRE_SMOKE", Integer.class,0);

    public static final SettingItem<Float> SMOKE_PRICE = new SettingItem<Float>("SMOKE_PRICE",Float.class,0.6f);
    public static final SettingItem<Integer> CURRENCY_ID = new SettingItem<Integer>("CURRENCY_INDEX",Integer.class,Currency.RUB.id);
    public static final SettingItem<Integer> QUIT_PROGRAM_INDEX = new SettingItem<Integer>("QUITE_PROGRAM",Integer.class, 0);
    public static final SettingItem<Integer> CONTENT_VIEW_CONFIG = new SettingItem<Integer>("CONTENT_VIEW",Integer.class, 0);

    public static final SettingItem<Long> APP_FIRST_TIME_DATE = new SettingItem<Long>("APP_FIRST_TIME_DATE",Long.class, null);


    public static final Flag ENABLED_STICKY_NOTIFICATION = new Flag("STICKY_NOTIFICATION",false);
    public static final Flag ENABLED_STATISTIC_NOTIFICATION = new Flag("STATISTIC_NOTIFICATION",false);

    public static final Flag FIRST_TIME_CLOSE_STICKY_NOTIFICATION = new Flag("STICKY_NOTIFICATION_FIRST_CLOSE", true);
    public static final Flag FIRST_TIME_ENTER_APP = new Flag("FIRST_TIME_ENTER_APP", true);
    public static final Flag FIRST_TIME_AFTER_SETUP = new Flag("FIRST_TIME_AFTER_SETUP", true);
    public static final Flag FIRST_TIME_QUIT_SMOKE_PAGE = new Flag("FIRST_TIME_QUIT_SMOKE_PAGE", true);
    public static final Flag IS_SMOKE_QUIT_ACTIVE = new Flag("IS_SMOKE_QUIT_ACTIVE", false);

    public static final Closure<Integer,Currency> CONVERT_CURRENCY = new Closure<Integer, Currency>() {
        @Override
        public Currency execute(Integer arg) {
            return Currency.byId(arg);
        }
    };

    private final SharedPreferences preferences;

    public Settings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public <Type> void set(SettingItem<Type> item, Type value){
        item.set(preferences,value);
    }

    public <Type> Type get(SettingItem<Type> item){
       return item.getOrDefault(preferences);
    }

    public <Type> void unset(SettingItem<Type> item){
        item.set(preferences,null);
    }

    public <Type> boolean has(SettingItem<Type> item){
      return item.get(preferences) != null;
    }

    public <Type> Type getAndSet(SettingItem<Type> item, Type value){
        Type answer = item.getOrDefault(preferences);
        if (answer != value){
            item.set(preferences,value);
        }
        return answer;
    }

    public <ItemType, AnswerType> AnswerType getAs(SettingItem<ItemType> setting, Closure<ItemType,AnswerType> convert) {
        return convert.execute(setting.getOrDefault(preferences));
    }

    public static class SettingItem <Type>{

        private final String id;
        private final Type defaultValue;
        private final Class<Type> valueClass;

        private SettingItem(String id, Class<Type> typeClass, Type defaultValue) {
            this.id = id;
            this.defaultValue = defaultValue;
            this.valueClass = typeClass;
        }

        private void set(SharedPreferences preferences, Type value){
            SharedPreferences.Editor editor = preferences.edit();
            if (value == null) {
                editor.remove(id).commit();
                return;
            }
            if (valueClass == String.class){
                editor.putString(id, (String) value);
            }else if (valueClass == Integer.class){
                editor.putInt(id, (Integer) value);
            } else if (valueClass == Float.class){
                editor.putFloat(id, (Float) value);
            }else if (valueClass == Boolean.class){
                editor.putBoolean(id, (Boolean) value);
            } else if (valueClass == Long.class){
                editor.putLong(id, (Long) value);
            } else {
                throw new IllegalStateException();
            }
            editor.commit();
        }

        private Type get(SharedPreferences preferences){
            if (!preferences.contains(id)) return null;
            if (valueClass == Integer.class){
                return (Type)(Integer) preferences.getInt(id,0);
            } else if (valueClass == Float.class){
                return (Type)(Float) preferences.getFloat(id, 0);
            }else if (valueClass == Boolean.class){
                return (Type)(Boolean) preferences.getBoolean(id, false);
            } else if (valueClass == Long.class){
                return (Type) (Long)preferences.getLong(id, 0l);
            } else {
                throw new IllegalStateException();
            }
        }

        private Type getOrDefault(SharedPreferences preferences){
            Type answer = get(preferences);
            return answer == null ? defaultValue:answer;
        }

    }

    public static class Flag extends SettingItem<Boolean> {
        private Flag(String id, boolean defaultValue) {
            super(id, Boolean.class, defaultValue);
        }

    }

}
