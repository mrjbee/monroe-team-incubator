package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Currency;
import org.monroe.team.smooker.app.common.PreferenceManager;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.SmokeQuitProgramDifficult;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class UpdateGeneralDetails extends UserCaseSupport<UpdateGeneralDetails.DetailsUpdateRequest, Void> {

    public UpdateGeneralDetails(Registry registry) {
        super(registry);
    }

    @Override
    public Void execute(DetailsUpdateRequest request) {
        if (exists(request.smokePerDay)){
            using(PreferenceManager.class).setSmokePerDay(request.smokePerDay.intValue());
        }

        if (exists(request.desireSmokePerDay)){
            using(PreferenceManager.class).setDesireSmokePerDay(request.desireSmokePerDay);
        }

        if (exists(request.difficultLevel)){
            using(PreferenceManager.class).setQuiteProgram(request.difficultLevel);
        }

        if (exists(request.financialHistoryRecalculateRequest)){
            //todo remove old costs
        }

        if (exists(request.costPerSmoke)){
            using(PreferenceManager.class).setCostPerSmoke(request.costPerSmoke.floatValue());
        }

        if (exists(request.currency)){
            using(PreferenceManager.class).setCurrency(request.currency);
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
