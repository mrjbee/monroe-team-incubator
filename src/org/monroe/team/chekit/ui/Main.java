package org.monroe.team.chekit.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.monroe.team.chekit.common.Context;
import org.monroe.team.chekit.services.BackgroundTaskManager;
import org.monroe.team.chekit.ui.controller.GlobalController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Parent startSlide = FXMLLoader.load(getClass().getResource("slide_start.fxml"));
        BorderPane borderPane = (BorderPane) root.lookup("#rootContentPane");
        borderPane.setCenter(startSlide);
        primaryStage.setTitle("Check It Runner");
        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.onCloseRequestProperty().set(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Context.get().using(BackgroundTaskManager.class).destroy();
            }
        });
        primaryStage.show();
        Context.get().using(GlobalController.class).registerApplication(this,primaryStage);
    }

    public static void main(String[] args){
        launch(args);
    }
}
