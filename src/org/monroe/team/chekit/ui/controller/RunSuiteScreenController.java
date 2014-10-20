package org.monroe.team.chekit.ui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.monroe.team.chekit.services.BackgroundTaskManager;
import org.monroe.team.chekit.uc.*;
import org.monroe.team.chekit.uc.presentations.Actions;
import org.monroe.team.chekit.uc.presentations.Screen;
import org.monroe.team.chekit.uc.presentations.StepRunRepresentation;
import org.monroe.team.chekit.ui.Main;

import java.io.File;

public class RunSuiteScreenController extends ScreenControllerSupport{

    static Image passed_image = new Image(Main.class.getResourceAsStream("resources/passed.png"));
    static Image failed_image = new Image(Main.class.getResourceAsStream("resources/failed.png"));
    static Image skipped_image = new Image(Main.class.getResourceAsStream("resources/skipped.png"));

    @FXML TreeView<StepRunRepresentation> suiteRunTreeView;
    @FXML Label suiteNameLabel;
    @FXML Label suiteDetailsLabel;
    @FXML Label suiteDateLabel;

    @FXML Pane runDetailsPane;
    @FXML Label runDetailsLabel;


    TreeItem<StepRunRepresentation> rootItem;
    @Deprecated //because runDetails
    String testRunId;
    CreateCheckRun.CheckRunDetails runDetails;

    private boolean saved = true;

    @Override
    protected void onInit() {
        super.onInit();
        using(GlobalController.class).lockUI();
        runStepDetailsViewHide();
        suiteRunTreeView.setCellFactory(new Callback<TreeView<StepRunRepresentation>, TreeCell<StepRunRepresentation>>() {
            @Override
            public TreeCell<StepRunRepresentation> call(TreeView<StepRunRepresentation> treeView) {
                StepRunTreeCell stepRunTreeCell = new StepRunTreeCell();
                return stepRunTreeCell;
            }
        });

        suiteRunTreeView.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() != KeyCode.LEFT
                    && keyEvent.getCode() != KeyCode.RIGHT
                        && keyEvent.getCode() != KeyCode.PAGE_DOWN
                        && keyEvent.getCode() != KeyCode.PAGE_UP
                        && keyEvent.getCode() != KeyCode.UP
                        && keyEvent.getCode() != KeyCode.DOWN
                        && keyEvent.getCode() != KeyCode.ENTER
                        && keyEvent.getCode() != KeyCode.TAB
                        && keyEvent.getCode() != KeyCode.ESCAPE
                        && !keyEvent.isMetaDown()
                        && !keyEvent.isAltDown()
                        && !keyEvent.isShiftDown()
                        && !keyEvent.isShortcutDown()
                        && !keyEvent.isControlDown()
                        ){
                    using(GlobalController.class).commandPromptFocus();
                }
                if(keyEvent.getCode() == KeyCode.ENTER && keyEvent.isShiftDown()) {
                    keyEvent.consume();
                    if (runDetailsPane.isVisible()){
                        onAction(new Actions.Suite.UpdateStepComment());
                    } else {
                        runStepDetailsViewShow();
                    }
                }
                if(keyEvent.getCode() == KeyCode.ESCAPE){
                    keyEvent.consume();
                    runStepDetailsViewHide();
                }
                if(keyEvent.getCode() == KeyCode.E && keyEvent.isShiftDown()) {
                    keyEvent.consume();
                    onShiftE();
                }
            }


        });
        suiteRunTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<StepRunRepresentation>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<StepRunRepresentation>> observableValue, TreeItem<StepRunRepresentation> stepRunRepresentationTreeItem, TreeItem<StepRunRepresentation> stepRunRepresentationTreeItem2) {
                runStepDetailsViewUpdate();
            }
        });
    }

    private void onShiftE() {
        TreeItem<StepRunRepresentation> selectedItem = suiteRunTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null){
            using(GlobalController.class).routeAction(new Actions.Toast("Please select item first"));
        } else {
            changeExpanding(selectedItem, !selectedItem.isExpanded());
        }
    }

    private void changeExpanding(TreeItem<StepRunRepresentation> selectedItem, boolean expanded) {
        selectedItem.setExpanded(expanded);
        if (selectedItem.getChildren() == null) return;
        for (TreeItem<StepRunRepresentation> childItem : selectedItem.getChildren()) {
            changeExpanding(childItem,expanded);
        }
    }

    private void runStepDetailsViewUpdate() {
       TreeItem<StepRunRepresentation> node = suiteRunTreeView.getSelectionModel().getSelectedItem();
       String details = "";
       if (node != null){
            details = node.getValue().comment;
            if (details == null){
                details = "";
            }
       }
       runDetailsLabel.setText(details);
    }

    private void runStepDetailsViewHide() {
        runDetailsPane.setPrefHeight(0);
        runDetailsPane.setVisible(false);
    }

    private void runStepDetailsViewShow() {
        runDetailsPane.setPrefHeight(100);
        runDetailsPane.setVisible(true);
    }

    @Override
    public void onAction(final Actions.Action action) {
        if (action instanceof Actions.Suite.ReloadRun){
            if (!saved) {
                using(GlobalController.class).routeAction(new Actions.Toast("Save existing run first."));
            } else {
                File file = uc().execute(GetSuiteDetailsByCheckRun.class, runDetails.id).suiteFile;
                using(GlobalController.class).routeAction(new Actions.Application.ChangeScreen(Screen.STARTUP));
                using(GlobalController.class).routeAction(new Actions.Suite.OpenCheckSuite(file));
            }
        }

        if (action instanceof Actions.Suite.SaveRun){
            using(BackgroundTaskManager.class).execute(49, new BackgroundTaskManager.BackgroundTask<SaveCheckRun.SaveStatus>() {
                @Override
                protected SaveCheckRun.SaveStatus doInBackground() throws InterruptedException {
                    return uc().execute(SaveCheckRun.class,new SaveCheckRun.SaveRequest(runDetails.id, ((Actions.Suite.SaveRun) action).filePath));
                }

                @Override
                protected void onResult(SaveCheckRun.SaveStatus saveStatus) {
                    Actions.Toast toastToShow = null;
                    switch (saveStatus.responseCode){
                        case DONE: toastToShow = new Actions.Toast("Saved"); saved = true; break;
                        case ERROR: toastToShow = new Actions.Toast("Error during saving"); break;
                        case NO_FILE: toastToShow = new Actions.Toast("Please use, 'Save as' first..."); break;
                    }
                    using(GlobalController.class).routeAction(toastToShow);
                }
            });
        }

        if (action instanceof Actions.Suite.UpdateStepComment) {
            TreeItem<StepRunRepresentation> selected = suiteRunTreeView.getSelectionModel().getSelectedItem();

            if (selected == null) {
                using(GlobalController.class).routeAction(new Actions.Toast("Please select particular check"));
                return;
            }

            final StepRunRepresentation stepRunRepresentation = selected.getValue();
            //TODO: think of adding description of action
            if (stepRunRepresentation.type == StepRunRepresentation.Type.ACTION){
                using(GlobalController.class).routeAction(new Actions.Toast("No way to update action with comment yet"));
                return;
            }

            using(GlobalController.class).commentDialogRequest("Update comment",
                    stepRunRepresentation.comment, new GlobalController.CommentDialogHandler() {
                        @Override
                        public void onComment(String comment) {
                            updateRun(stepRunRepresentation, stepRunRepresentation.type, comment);
                        }

                        @Override
                        public void onCommentDiscard() {
                            using(GlobalController.class).routeAction(new Actions.Toast("Canceled"));
                        }
                    });

        }

        if (action instanceof Actions.Suite.RunCheckSuite) {
            using(BackgroundTaskManager.class).execute(31, new BackgroundTaskManager.BackgroundTask<StepRunRepresentation>() {
                @Override
                protected StepRunRepresentation doInBackground() throws InterruptedException {
                    CreateCheckRun.CheckRunDetails suiteId = uc().execute(CreateCheckRun.class, ((Actions.Suite.RunCheckSuite) action).suiteId);
                    testRunId = suiteId.id;
                    runDetails = suiteId;
                    autoSaveStart();
                    StepRunRepresentation root = uc().execute(PresentCheckRun.class, suiteId.id);
                    return root;
                }

                @Override
                protected void onResult(StepRunRepresentation stepRunRepresentation) {
                    rootItem = convertToTree(stepRunRepresentation);
                    suiteRunTreeView.setRoot(rootItem);
                    suiteDateLabel.setText(runDetails.date);
                    suiteNameLabel.setText(runDetails.name);
                    suiteDetailsLabel.setText(runDetails.details);
                    runStepDetailsViewUpdate();
                    using(GlobalController.class).unlockUI();
                }
            });
        }

        if (action instanceof Actions.Suite.ContinueCheckSuiteRun) {
            using(BackgroundTaskManager.class).execute(31, new BackgroundTaskManager.BackgroundTask<StepRunRepresentation>() {
                @Override
                protected StepRunRepresentation doInBackground() throws InterruptedException {
                    runDetails = ((Actions.Suite.ContinueCheckSuiteRun) action).details;
                    testRunId = runDetails.id;
                    autoSaveStart();
                    StepRunRepresentation root = uc().execute(PresentCheckRun.class, runDetails.id);
                    return root;
                }

                @Override
                protected void onResult(StepRunRepresentation stepRunRepresentation) {
                    rootItem = convertToTree(stepRunRepresentation);
                    suiteRunTreeView.setRoot(rootItem);
                    suiteDateLabel.setText(runDetails.date);
                    suiteNameLabel.setText(runDetails.name);
                    suiteDetailsLabel.setText(runDetails.details);
                    runStepDetailsViewUpdate();
                    using(GlobalController.class).unlockUI();
                }
            });
        }

        if (action instanceof Actions.Suite.UpdateRunStatusRequest) {
            TreeItem<StepRunRepresentation> selected = suiteRunTreeView.getSelectionModel().getSelectedItem();

            if (selected == null) {
                using(GlobalController.class).routeAction(new Actions.Toast("Please select particular check"));
                return;
            }
            final StepRunRepresentation stepRunRepresentation = selected.getValue();
            updateRun(stepRunRepresentation,
                    ((Actions.Suite.UpdateRunStatusRequest) action).status,
                    ((Actions.Suite.UpdateRunStatusRequest) action).comment);
        }

        if (action instanceof Actions.Suite.UpdateRunDetails) {
            String initialText = uc().execute(SetGetSuiteRunDetails.class, new SetGetSuiteRunDetails.UpdateDetailsRequest(runDetails.id,null));
            String title = "Run description";
            using(GlobalController.class).commentDialogRequest(title,initialText, new GlobalController.CommentDialogHandler() {
                @Override
                public void onComment(final String comment) {
                     using(BackgroundTaskManager.class).execute(44, new BackgroundTaskManager.BackgroundTask<String>() {
                         @Override
                         protected String doInBackground() throws InterruptedException {
                             saved = false;
                             return uc().execute(SetGetSuiteRunDetails.class, new SetGetSuiteRunDetails.UpdateDetailsRequest(runDetails.id,comment));
                         }

                         @Override
                         protected void onResult(String s) {
                             suiteDetailsLabel.setText(s);
                         }
                     });
                }
                @Override
                public void onCommentDiscard() {}
            });
        }
    }

    private void updateRun(final StepRunRepresentation stepRunRepresentation, final StepRunRepresentation.Type type, final String comment) {
        using(BackgroundTaskManager.class).execute(new BackgroundTaskManager.BackgroundTask<UpdateRunStep.UpdateResult>() {
            @Override
            protected UpdateRunStep.UpdateResult doInBackground() throws InterruptedException {
                return uc().execute(UpdateRunStep.class, new UpdateRunStep.UpdateRequest(
                        testRunId,
                        stepRunRepresentation.id,
                        type,
                        comment));
            }

            @Override
            protected void onResult(UpdateRunStep.UpdateResult result) {
                if (result.result == UpdateRunStep.Result.DONE){
                    saved = false;
                    stepRunRepresentationUpdate(result.representation);
                } else if (result.result == UpdateRunStep.Result.ACTION_COULD_BE_UPDATED){
                    using(GlobalController.class).routeAction(new Actions.Toast("Actions doesn`t have a status, select choice"));
                } else {
                    using(GlobalController.class).routeAction(new Actions.Toast("Comment required"));
                    using(GlobalController.class).commentDialogRequest("Comment required","",new GlobalController.CommentDialogHandler(){
                        @Override
                        public void onComment(String comment) {
                           updateRun(stepRunRepresentation, type, comment);
                        }
                        @Override
                        public void onCommentDiscard() {
                            using(GlobalController.class).routeAction(new Actions.Toast("Too bad. Update canceled"));
                        }
                    });
                }
            }
        });
    }

    private void stepRunRepresentationUpdate(StepRunRepresentation representation) {
        TreeItem<StepRunRepresentation> item = findItemByIdStartingWith(representation.id, rootItem);
        if (item == null){
            using(GlobalController.class).routeAction(new Actions.Toast("Something bad! Node disappears"));
            return;
        }
        StepRunRepresentation runRepresentation = item.getValue();
        runRepresentation.type = representation.type;
        runRepresentation.comment = representation.comment;
        //TODO: replace if required
        item.setValue(representation);
        item.setValue(runRepresentation);
        runStepDetailsViewUpdate();
        suiteRunTreeView.getSelectionModel().selectNext();
    }

    private TreeItem<StepRunRepresentation> findItemByIdStartingWith(String id, TreeItem<StepRunRepresentation> rootItem) {
        if (rootItem.valueProperty().get().id.equals(id)) return rootItem;
        ObservableList<TreeItem<StepRunRepresentation>> children =  rootItem.getChildren();
        if (children == null) return null;
        for (TreeItem<StepRunRepresentation> child : children) {
            TreeItem<StepRunRepresentation> answer = findItemByIdStartingWith(id, child);
            if (answer!=null) return answer;
        }
        return null;
    }

    private TreeItem<StepRunRepresentation> convertToTree(StepRunRepresentation stepRunRepresentation) {
        TreeItem<StepRunRepresentation> treeNode = new TreeItem<>(stepRunRepresentation);
        for (StepRunRepresentation runRepresentation : stepRunRepresentation.subStepList) {
            TreeItem<StepRunRepresentation> sunNode = convertToTree(runRepresentation);
            treeNode.getChildren().add(sunNode);
        }
        treeNode.setExpanded(true);
        return treeNode;
    }

    @Override
    public boolean onQuit() {
        if (!saved){
            //TODO: Something smarter
            using(GlobalController.class).routeAction(new Actions.Toast("Unsaved"));
            return false;
        }
        autoSaveStop();
        return true;
    }


    @Override
    public boolean isPromptAllaysVisible() {
        return false;
    }

    @Override
    public void rootComponentFocus() {
        suiteRunTreeView.requestFocus();
    }

    public static class StepRunTreeCell extends TreeCell<StepRunRepresentation> {
        @Override
        protected void updateItem(StepRunRepresentation stepRunRepresentation, boolean b) {
            super.updateItem(stepRunRepresentation, b);
            getStyleClass().remove("cell-action");
            getStyleClass().remove("cell-check");
            for (StepRunRepresentation.Type checkType : StepRunRepresentation.CHECK_TYPES) {
                getStyleClass().remove("cell-"+checkType.name());
            }
            if (stepRunRepresentation != null) {
                StringBuilder builder = new StringBuilder();
                if (stepRunRepresentation.type != StepRunRepresentation.Type.ACTION){
                    getStyleClass().add("cell-check");
                    getStyleClass().add("cell-" + stepRunRepresentation.type.name());
                    switch (stepRunRepresentation.type){
                        case CHECK_PASSED:
                            setGraphic(new ImageView(passed_image));
                        break;
                        case CHECK_FAILED:
                            setGraphic(new ImageView(failed_image));
                            break;
                        case CHECK_SKIPPED:
                            setGraphic(new ImageView(skipped_image));
                            break;
                        default:
                            setGraphic(null);
                        break;
                    }
                }else {
                    setGraphic(null);
                    getStyleClass().add("cell-action");
                }
                builder.append(stepRunRepresentation.title);
                setText(builder.toString());
            }
        }

        @Override
        public void updateSelected(boolean b) {
            super.updateSelected(b);
        }

    }

    private boolean autoSaveRequired = true;
    private void autoSaveStop() {
        autoSaveRequired = false;
        using(BackgroundTaskManager.class).cancelTask(49);
    }

    private void autoSaveStart(){
        using(BackgroundTaskManager.class).execute(49,new BackgroundTaskManager.BackgroundTask<SaveCheckRun.SaveStatus>() {
            @Override
            protected SaveCheckRun.SaveStatus doInBackground() throws InterruptedException {
                Thread.sleep(3000);
                if (saved) return null;
                return uc().execute(SaveCheckRun.class, new SaveCheckRun.SaveRequest(runDetails.id,null));
             }

            @Override
            protected void onResult(SaveCheckRun.SaveStatus saveStatus) {
                if (saveStatus != null && saveStatus.responseCode == SaveCheckRun.ResponseCode.DONE){
                    using(GlobalController.class).routeAction(new Actions.Toast("Saved"));
                    saved = true;
                }
            }

            @Override
            protected void onFinish() {
                if (autoSaveRequired) {
                    autoSaveStart();
                }
            }
        });
    }


}
