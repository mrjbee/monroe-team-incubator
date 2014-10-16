package org.monroe.team.chekit.ui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.monroe.team.chekit.services.BackgroundTaskManager;
import org.monroe.team.chekit.uc.ContinueSuiteRun;
import org.monroe.team.chekit.uc.LoadCheckSuite;
import org.monroe.team.chekit.uc.presentations.Actions;
import org.monroe.team.chekit.uc.presentations.Screen;

import java.util.*;
import java.util.List;

public class MainScreenController extends ScreenControllerSupport implements GlobalController.RootWindowController{

    @FXML BorderPane rootContentPane;

    @FXML Pane errorPane;
    @FXML Label errorLabel;

    @FXML Pane commentPane;
    @FXML Label commentTitleLabel;
    @FXML TextArea commentTextArea;

    @FXML Pane commandPane;
    @FXML TextField commandEditField;
    @FXML ListView suggestionsListView;

    private Screen currentScreen = Screen.STARTUP;
    private GlobalController.CommentDialogHandler currentCommentHandler;

    @Override
    protected void onInit() {
        using(GlobalController.class).rootWindowController = this;
        commandEditField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean old, Boolean newValue) {
                if (!commandEditField.isFocused()) {
                    hidePrompt();
                }
            }
        });
        commandEditField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old, String newValue) {
                onCommandStringUpdated(newValue);
            }
        });
        commandEditField.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case UP:
                        if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED)
                            onUpKey();
                        keyEvent.consume();
                        break;
                    case DOWN:
                        if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED)
                            onDownKey();
                        keyEvent.consume();
                        break;
                    case RIGHT:
                        if (commandEditField.getCaretPosition() == commandEditField.getText().length()){
                            if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED)
                                onTabKey();
                            keyEvent.consume();
                        }
                        break;
                    case ENTER:
                        if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED)
                            onEnterKey();
                        keyEvent.consume();
                        break;
                    case ESCAPE:
                        keyEvent.consume();
                        if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED){
                            using(GlobalController.class).screenRootComponentFocus();
                        }
                        break;
                }
            }
        });
        commentTextArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode()) {
                    case ENTER:
                        if (keyEvent.isShiftDown()) {
                            keyEvent.consume();
                            commentDialogContinue();
                        }
                        break;
                    case ESCAPE:
                        commentDialogCancel();
                        keyEvent.consume();
                        break;
                }
            }
        });
        promptFocus();
    }

    private void onUpKey() {
        suggestionSelection(-1);
    }
    private void onDownKey() {
        suggestionSelection(1);
    }

    private void suggestionSelection(int delta) {
        if (!suggestionsListView.isVisible() || suggestionsListView.getItems().isEmpty()) return;
        int curIndex = suggestionsListView.getSelectionModel().getSelectedIndex();
        if (curIndex < 0) {
            suggestionsListView.getSelectionModel().selectFirst();
            suggestionsListView.scrollTo(0);
        } else {
           curIndex += delta;
           if (curIndex > -1 && curIndex < suggestionsListView.getItems().size()){
                suggestionsListView.getSelectionModel().select(curIndex);
               suggestionsListView.scrollTo(curIndex);
           }
        }
    }

    private boolean onTabKey() {
        SuggestionListDataItem suggestionItem = (SuggestionListDataItem) suggestionsListView.getSelectionModel().getSelectedItem();
        if (suggestionItem != null){
            commandEditField.setText(suggestionItem.origin);
            commandEditField.positionCaret(commandEditField.getText().length());
            return true;
        }
        return false;
    }

    private void onEnterKey() {
        if (onTabKey())return;
        final String text = commandEditField.getText();
        final Screen curScreen = currentScreen;
        using(GlobalController.class).lockUI();
        commandEditField.setText("");
        using(BackgroundTaskManager.class).execute(11, new BackgroundTaskManager.BackgroundTask<Actions.Action>() {
            @Override
            protected Actions.Action doInBackground() throws InterruptedException {
                Actions.Action answer = using(TextRecognitionManager.class).parse(currentScreen,
                        text,
                        Collections.EMPTY_MAP
                );
                return answer;
            }

            @Override
            protected void onResult(Actions.Action action) {
                using(GlobalController.class).routeAction(action);
            }

            @Override
            protected void onFinish() {
                using(GlobalController.class).unlockUI();
            }
        });
        hidePrompt();
    }

    private void hidePrompt() {
        if (!using(GlobalController.class).commandPromptAlwaysVisible()){
            suggestionsListView.setVisible(false);
            commandPane.setVisible(false);
        }
    }


    void onCommandStringUpdated(final String text){
        using(BackgroundTaskManager.class).execute(10, new BackgroundTaskManager.BackgroundTask<String[]>() {
            @Override
            protected String[] doInBackground() throws InterruptedException {
                Thread.sleep(100);
                TextRecognitionManager.Prediction prediction = using(TextRecognitionManager.class).predict(currentScreen, text);
                return prediction.completions.toArray(new String[]{});
            }

            @Override
            protected void onResult(String[] strings) {
                if (strings.length > 0){
                    List<SuggestionListDataItem> suggestionListDataItemList = new ArrayList<SuggestionListDataItem>();
                    for (String string : strings) {
                        suggestionListDataItemList.add(new SuggestionListDataItem(string));
                    }
                    ObservableList<SuggestionListDataItem> observableList = FXCollections.observableList(suggestionListDataItemList);
                    suggestionsListView.setItems(observableList);
                    double prefSize = 27 * strings.length;
                    if (prefSize > using(GlobalController.class).getWindowsHeight()/2 ){
                        prefSize = 400;
                    }
                    suggestionsListView.setPrefHeight(prefSize);
                    suggestionsListView.getSelectionModel().selectFirst();
                    suggestionsListView.setVisible(true);
                } else {
                    suggestionsListView.setItems(FXCollections.emptyObservableList());
                    suggestionsListView.setVisible(false);
                }
            }
        });
    }


    @Override
    public void changeScreenTo(Screen screen) {
        Parent slide = using(GlobalController.class).inflateScreen(screen);
        rootContentPane.setCenter(slide);
        currentScreen = screen;
        if (screen == Screen.STARTUP){
            using(GlobalController.class).screenController = this;
            promptFocus();
        }
    }

    @Override
    public void alarmInvalidAction(Actions.Toast toast) {
        String message = "Invalid operation, come again ...";
        if (toast != null){
            message = toast.msg;
        }

        final String finalMessage = message;
        errorLabel.setText(finalMessage);
        errorPane.setVisible(true);

        using(BackgroundTaskManager.class).execute(12, new BackgroundTaskManager.BackgroundTask<Void>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                Thread.sleep(1000);
                return null;
            }

            @Override
            protected void onResult(Void aVoid) {
                errorPane.setVisible(false);
            }
        });
    }

    @Override
    public void promptFocus() {
        commandPane.setVisible(true);
        commandEditField.requestFocus();
    }

    @Override
    public void promptHide() {
        hidePrompt();
    }

    @Override
    public void lockUI() {
        //TODO implement lock UI
    }

    @Override
    public void unlockUI() {
        //TODO implement unlock UI
    }

    @Override
    public void onAction(final Actions.Action action) {
        if (action instanceof Actions.Suite.OpenCheckSuite){
            using(GlobalController.class).lockUI();
            using(BackgroundTaskManager.class).execute(20, new BackgroundTaskManager.BackgroundTask<LoadCheckSuite.LoadStatus>() {
                @Override
                protected LoadCheckSuite.LoadStatus doInBackground() throws InterruptedException {
                    LoadCheckSuite.LoadStatus status = uc().execute(LoadCheckSuite.class, ((Actions.Suite.OpenCheckSuite) action).file);
                    return status;
                }

                @Override
                protected void onResult(LoadCheckSuite.LoadStatus loadStatus) {
                    using(GlobalController.class).unlockUI();
                    if (loadStatus.toast != null){
                        using(GlobalController.class).routeAction(loadStatus.toast);
                    } else {
                        using(GlobalController.class).routeAction(new Actions.Application.ChangeScreen(Screen.RUNNER));
                        using(GlobalController.class).routeAction(new Actions.Suite.RunCheckSuite(loadStatus.checkSuiteId));
                    }
                }

            });
        }
        if (action instanceof Actions.Suite.OpenRun){
            using(GlobalController.class).lockUI();
            using(BackgroundTaskManager.class).execute(21, new BackgroundTaskManager.BackgroundTask<ContinueSuiteRun.LoadStatus>() {
                @Override
                protected ContinueSuiteRun.LoadStatus doInBackground() throws InterruptedException {
                    ContinueSuiteRun.LoadStatus status = uc().execute(ContinueSuiteRun.class, ((Actions.Suite.OpenRun) action).file);
                    return status;
                }

                @Override
                protected void onResult(ContinueSuiteRun.LoadStatus loadStatus) {
                    using(GlobalController.class).unlockUI();
                    if (loadStatus.toast != null){
                        using(GlobalController.class).routeAction(loadStatus.toast);
                    } else {
                        using(GlobalController.class).routeAction(new Actions.Application.ChangeScreen(Screen.RUNNER));


                        using(GlobalController.class).routeAction(new Actions.Suite.ContinueCheckSuiteRun(loadStatus.details));
                    }
                }

            });
        }
    }

    @Override
    public boolean onQuit() {
        return true;
    }

    @Override
    public boolean isPromptAllaysVisible() {
        return true;
    }

    @Override
    public void rootComponentFocus() {/*Nothing to focus*/}


    public class SuggestionListDataItem {

        final String origin;
        final String show;

        public SuggestionListDataItem(String origin) {
            this.origin = origin;
            if (origin.length() > 30){
                this.show = "..."+origin.substring(origin.length() - 30);
            } else {
                this.show = origin;
            }
        }

        @Override
        public String toString() {
            return show;
        }
    }

    @Override
    public void commentDialog(String title, String initialText, GlobalController.CommentDialogHandler commentDialogHandler) {
        commentTitleLabel.setText(title);
        commentTextArea.setText(initialText);
        commentPane.setVisible(true);
        commentTextArea.requestFocus();
        currentCommentHandler = commentDialogHandler;
    }

    public void commentDialogCancel() {
        currentCommentHandler.onCommentDiscard();
        currentCommentHandler = null;
        commentPane.setVisible(false);
    }

    public void commentDialogContinue() {
       String text = commentTextArea.getText();
       currentCommentHandler.onComment(text);
       currentCommentHandler = null;
       commentPane.setVisible(false);
    }


}
