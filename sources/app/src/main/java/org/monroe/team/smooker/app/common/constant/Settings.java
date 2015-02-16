package org.monroe.team.smooker.app.common.constant;


import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.utils.Closure;

import java.util.Date;

public interface Settings {
    SettingManager.SettingItem<Long> LAST_OVERNIGHT_UPDATE_DATE = new SettingManager.SettingItem<Long>("LAST_OVERNIGHT_UPDATE_DATE",Long.class, -1L);
    SettingManager.SettingItem<Integer> QUITE_START_SMOKE = new SettingManager.SettingItem<Integer>("QUITE_START_SMOKE",Integer.class, null);
    SettingManager.SettingItem<Integer> QUITE_END_SMOKE = new SettingManager.SettingItem<Integer>("DESIRE_SMOKE", Integer.class,0);
    SettingManager.SettingItem<Float> SMOKE_PRICE = new SettingManager.SettingItem<Float>("SMOKE_PRICE",Float.class,0.6f);
    SettingManager.SettingItem<Integer> CURRENCY_ID = new SettingManager.SettingItem<Integer>("CURRENCY_INDEX",Integer.class,Currency.RUB.id);
    SettingManager.SettingItem<Integer> QUIT_PROGRAM_INDEX = new SettingManager.SettingItem<Integer>("QUITE_PROGRAM",Integer.class, 0);
    SettingManager.SettingItem<Integer> CONTENT_VIEW_CONFIG = new SettingManager.SettingItem<Integer>("CONTENT_VIEW",Integer.class, 2);
    SettingManager.SettingItem<Long> APP_FIRST_TIME_DATE = new SettingManager.SettingItem<Long>("APP_FIRST_TIME_DATE",Long.class, null);
    SettingManager.SettingItem<Long> QUIT_SMOKE_VALIDATION_DATE = new SettingManager.SettingItem<Long>("QUIT_SMOKE_VALIDATION_DATE",Long.class, null);
    SettingManager.Flag ENABLED_STICKY_NOTIFICATION = new SettingManager.Flag("STICKY_NOTIFICATION",false);
    SettingManager.Flag ENABLED_STATISTIC_NOTIFICATION = new SettingManager.Flag("STATISTIC_NOTIFICATION",false);
    SettingManager.Flag ENABLED_ASSISTANCE_NOTIFICATION = new SettingManager.Flag("ASSISTANCE_NOTIFICATION",true);
    SettingManager.Flag FIRST_TIME_CLOSE_STICKY_NOTIFICATION = new SettingManager.Flag("STICKY_NOTIFICATION_FIRST_CLOSE", true);
    SettingManager.Flag FIRST_TIME_ENTER_APP = new SettingManager.Flag("FIRST_TIME_ENTER_APP", true);
    SettingManager.Flag FIRST_TIME_AFTER_SETUP = new SettingManager.Flag("FIRST_TIME_AFTER_SETUP", true);
    SettingManager.Flag FIRST_TIME_QUIT_SMOKE_PAGE = new SettingManager.Flag("FIRST_TIME_QUIT_SMOKE_PAGE", true);
    SettingManager.Flag IS_SMOKE_QUIT_ACTIVE = new SettingManager.Flag("IS_SMOKE_QUIT_ACTIVE", false);

    Closure<Integer,Currency> CONVERT_CURRENCY = new Closure<Integer, Currency>() {
        @Override
        public Currency execute(Integer arg) {
            return Currency.byId(arg);
        }
    };

    Closure<Long,Date> CONVERT_DATE = new Closure<Long,Date>() {
        @Override
        public Date execute(Long dateTime) {
            if (dateTime == null) return null;
            return new Date(dateTime);
        }
    };
}
