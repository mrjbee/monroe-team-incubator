package org.monroe.team.chekit.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.monroe.team.chekit.uc.presentations.Actions;
import org.monroe.team.chekit.uc.presentations.Screen;
import org.monroe.team.chekit.ui.Main;

import java.io.IOException;

public class GlobalController {

    RootWindowController rootWindowController;
    ScreenController screenController;
    Main app;
    Stage stage;

    public void lockUI() {
        rootWindowController.lockUI();
    }

    public void unlockUI() {
        rootWindowController.unlockUI();
    }

    public void routeAction(Actions.Action action) {
        System.out.println("Action: " +action);
        if (action == null || action instanceof Actions.Toast) {
            rootWindowController.alarmInvalidAction((Actions.Toast) action);
            if (screenController.isPromptAllaysVisible()){
                commandPromptFocus();
            }
            return;
        }

        if (action instanceof Actions.Application.ChangeScreen){
            if (screenController.onQuit()) {
                rootWindowController.changeScreenTo(((Actions.Application.ChangeScreen) action).screen);
            }
        } else if (action instanceof Actions.Application.QuitApplication){
            if (screenController.onQuit()){
                shutdown();
            }
        } else {
            screenController.onAction(action);
        }
    }

    public void shutdown() {
       System.exit(0);
    }

    public void registerApplication(Main main, Stage primaryStage) {
        app = main;
        stage = primaryStage;
    }

    public Parent inflateScreen(Screen screen) {
        try {
            return FXMLLoader.load(app.getClass().getResource(screen.layoutName));
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public void commandPromptFocus() {
        rootWindowController.promptFocus();
    }

    public void commandPromptHide() {
        rootWindowController.promptHide();
    }
    public boolean commandPromptAlwaysVisible() {
        return screenController.isPromptAllaysVisible();
    }

    public void commentDialogRequest(String title, String initialText, CommentDialogHandler commentDialogHandler) {
        rootWindowController.commentDialog(title, initialText, commentDialogHandler);
    }

    public void screenRootComponentFocus() {
        screenController.rootComponentFocus();
    }

    public double getWindowsHeight() {
        return stage.getScene().getHeight();
    }

    public static interface ScreenController {
        void onAction(Actions.Action action);
        boolean onQuit();
        boolean isPromptAllaysVisible();
        void rootComponentFocus();
    }

    public static interface RootWindowController{
        void lockUI();
        void unlockUI();
        void changeScreenTo(Screen screen);
        void alarmInvalidAction(Actions.Toast error);
        void promptFocus();
        void promptHide();
        void commentDialog(String title, String initialText, CommentDialogHandler commentDialogHandler);
    }

    public static interface CommentDialogHandler {
        public void onComment(String comment);
        public void onCommentDiscard();
    }
}
