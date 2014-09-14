package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Currency;
import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.SmokeQuitProgramDifficult;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.uc.common.TransactionUserCase;

public class GetGeneralDetails extends TransactionUserCase<Void,GetGeneralDetails.GeneralDetailsResponse>{

    public GetGeneralDetails(Registry registry) {
        super(registry);
    }

    @Override
    protected GeneralDetailsResponse transactionalExecute(Void request, DAO dao) {
        Preferences preferences = using(Preferences.class);
        Preferences.DB dbPreferences = using(Preferences.class).db(dao);
        int smokePerDay = preferences.getSmokePerDay();
        int desireSmokePerDay = preferences.getDesireSmokePerDay();
        SmokeQuitProgramDifficult difficultLevel = preferences.getQuitProgram();
        return new GeneralDetailsResponse(smokePerDay, desireSmokePerDay, difficultLevel,
                dbPreferences.getCostPerSmoke(),
                preferences.getCurrency(),
                dbPreferences.hasFinancialHistory());
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
