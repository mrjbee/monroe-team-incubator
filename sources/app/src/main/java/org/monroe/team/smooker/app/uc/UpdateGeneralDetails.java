package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Currency;
import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.SmokeQuitProgramDifficult;
import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.dp.TransactionManager;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class UpdateGeneralDetails extends TransactionUserCase<UpdateGeneralDetails.DetailsUpdateRequest, Void> {

    public UpdateGeneralDetails(Registry registry) {
        super(registry);
    }

    @Override
    protected Void transactionalExecute(DetailsUpdateRequest request, DAO dao) {
        Preferences preferences = using(Preferences.class);
        Preferences.DB dbPreferences = preferences.db(dao);
        if (exists(request.smokePerDay)){
            preferences.setSmokePerDay(request.smokePerDay.intValue());
        }

        if (exists(request.desireSmokePerDay)){
            preferences.setDesireSmokePerDay(request.desireSmokePerDay);
        }

        if (exists(request.difficultLevel)){
            preferences.setQuiteProgram(request.difficultLevel);
        }

        if (exists(request.financialHistoryRecalculateRequest)){
            //todo remove old costs
        }

        if (exists(request.costPerSmoke)){
            dbPreferences.setCostPerSmoke(request.costPerSmoke.floatValue());
        }

        if (exists(request.currency)){
            preferences.setCurrency(request.currency);
        }
        return null;
    }



    private boolean exists(Object value) {
        return value != null;
    }

    public static final class DetailsUpdateRequest {

        private Integer smokePerDay;
        private Integer desireSmokePerDay;
        private SmokeQuitProgramDifficult difficultLevel;
        private Float costPerSmoke;
        private Currency currency;
        private Boolean financialHistoryRecalculateRequest;

        public DetailsUpdateRequest withSmokePerDay(int value){
            smokePerDay =value;
            return this;
        }

        public DetailsUpdateRequest deleteSmokePerDay(){
            smokePerDay = null;
            return this;
        }


        public DetailsUpdateRequest withDesireSmokePerDay(int desireSmokePerDayCount) {
            desireSmokePerDay =desireSmokePerDayCount;
            return this;
        }

        public DetailsUpdateRequest withQuitDifficultLevel(SmokeQuitProgramDifficult difficult) {
            difficultLevel = difficult;
            return this;
        }

        public DetailsUpdateRequest withCostPerSmoke(float smokeCost) {
            costPerSmoke = smokeCost;
            return this;
        }


        public DetailsUpdateRequest withCurrency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public DetailsUpdateRequest withFinancialHistoryRecalculateRequest(boolean checked) {
            if (checked) {
                this.financialHistoryRecalculateRequest = checked;
            } else {
                financialHistoryRecalculateRequest = null;
            }
            return this;
        }
    }
}
