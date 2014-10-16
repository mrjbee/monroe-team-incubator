package org.monroe.team.chekit.ui.controller;

import javafx.fxml.FXML;
import org.monroe.team.chekit.common.Context;
import org.monroe.team.chekit.services.UseCaseManager;

public abstract class ScreenControllerSupport implements GlobalController.ScreenController {
    @FXML
    final public void initialize() {
        using(GlobalController.class).screenController = this;
        onInit();
        if (!isPromptAllaysVisible()){
            using(GlobalController.class).commandPromptHide();
        }
    }

    protected void onInit(){}

    final public UseCaseManager uc(){
        return Context.get().using(UseCaseManager.class);
    }

    final public <Type> Type using(Class<Type> serviceClass){
        return Context.get().using(serviceClass);
    }


}
