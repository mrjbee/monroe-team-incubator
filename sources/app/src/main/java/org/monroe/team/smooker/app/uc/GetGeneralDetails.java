package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Currency;
import org.monroe.team.smooker.app.common.PreferenceManager;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.SmokeQuitProgramDifficult;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class GetGeneralDetails extends UserCaseSupport<Void,GetGeneralDetails.GeneralDetailsResponse>{

    public GetGeneralDetails(Registry registry) {
        super(registry);
    }

    @Override
    public GeneralDetailsResponse execute(Void request) {
        int smokePerDay = using(PreferenceManager.class).getSmokePerDay(GeneralDetailsResponse.SMOKE_PER_DAY_UNDEFINED);
        int desireSmokePerDay = using(PreferenceManager.class).getDesireSmokePerDay();
        SmokeQuitProgramDifficult difficultLevel = using(PreferenceManager.class).getQuitProgram();
        return new GeneralDetailsResponse(smokePerDay, desireSmokePerDay, difficultLevel,
                using(PreferenceManager.class).getCostPerSmoke(),
                using(PreferenceManager.class).getCurrency(),
                using(PreferenceManager.class).hasFinancialHistory());
    }

    public static class GeneralDetailsResponse {

        public static final int SMOKE_PER_DAY_UNDEFINED = -100;
        public static final int SMOKE_PER_DAY_UNDER_DETECT = -200;

        public final int smokePerDay;
        public final int desireSmokePerDay;
        public final SmokeQuitProgramDifficult difficultLevel;
        public final float costPerSmoke;
        public final Currency currency;
        public final boolean hasFinancialHistory;

        public GeneralDetailsResponse(int smokePerDay, int desireSmokePerDay, SmokeQuitProgramDifficult difficultLevel, float costPerSmoke, Currency currency, boolean hasFinancialHistory) {
            this.smokePerDay = smokePerDay;
            this.desireSmokePerDay = desireSmokePerDay;
            this.difficultLevel = difficultLevel;
            this.costPerSmoke = costPerSmoke;
            this.currency = currency;
            this.hasFinancialHistory = hasFinancialHistory;
        }

        public boolean isSmokingPerDay(int value) {
            return smokePerDay == value;
        }
    }

}
