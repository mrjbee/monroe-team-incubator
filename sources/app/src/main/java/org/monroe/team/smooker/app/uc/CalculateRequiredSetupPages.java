package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

import java.util.ArrayList;
import java.util.List;

public class CalculateRequiredSetupPages extends UserCaseSupport<Void,List<SetupPage>> {

    public CalculateRequiredSetupPages(Registry registry) {
        super(registry);
    }

    @Override
    public List<SetupPage> execute(Void request) {

        List<SetupPage> answer = new ArrayList<SetupPage>(4);

        if(using(Preferences.class).isFirstStart()){
            answer.add(SetupPage.WELCOME_PAGE);
        }

        //TODO: add logic here
        answer.add(SetupPage.SMOKE_PER_DAYS);
        answer.add(SetupPage.QUIT_SMOKING);

        return answer;
    }


}