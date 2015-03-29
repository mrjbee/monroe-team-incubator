package org.monroe.team.smooker.app.uc;

import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.db.Dao;

import java.io.Serializable;
import java.util.Date;

public class PrepareMoneyBoxProgress extends TransactionUserCase<Void,PrepareMoneyBoxProgress.MoneyBoxProgress,Dao>{


    public PrepareMoneyBoxProgress(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected MoneyBoxProgress transactionalExecute(Void request, Dao dao) {
        Float endPrice = using(SettingManager.class).get(Settings.MONEYBOX_SOMETHING_PRICE);
        Date startDate = using(SettingManager.class).getAs(Settings.MONEYBOX_START_DATE, Settings.CONVERT_DATE);
        if (endPrice == null || startDate == null){
            return new MoneyBoxProgress(0,0,0);
        }
        Integer averageSmoke = using(SettingManager.class).get(Settings.MONEYBOX_START_SMOKE);
        Float singleSmokePrice = using(SettingManager.class).get(Settings.SMOKE_PRICE);
        Date endDate = DateUtils.today();
        float savedMoney = 0;
        if (endDate.after(startDate)){
            int smokeSavedCount = 0;
            Date itDate = startDate;
            Date itEndDate = startDate;
            while (itDate.before(endDate)){
                itEndDate = DateUtils.mathDays(itDate,1);
                smokeSavedCount += (averageSmoke - dao.getSmokesForPeriod(itDate,itEndDate).size());
                itDate = itEndDate;
            }
            if (smokeSavedCount > 0){
                savedMoney = smokeSavedCount * singleSmokePrice;
            }
        }

        return new MoneyBoxProgress(endPrice, savedMoney,
                Math.min(100, Math.round(savedMoney / endPrice * 100)));
    }

    public static class MoneyBoxProgress implements Serializable {

        public final float totalPrice;
        public final float savedMoney;
        public final int targetProgress;

        public MoneyBoxProgress(float totalPrice, float savedMoney, int targetProgress) {
            this.totalPrice = totalPrice;
            this.savedMoney = savedMoney;
            this.targetProgress = targetProgress;
        }

        public boolean isDisabled() {
            return totalPrice == 0f;
        }
    }
}
