package ru.test;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Created by user on 08.10.16.
 */
public class TimeLineTest extends Application{

    public static void main(String[] args) {

        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600, Color.BLACK);
        primaryStage.setScene(scene);
        TextArea textArea = new TextArea();
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(2);
        timeline.setAutoReverse(true);


        timeline.play();

        primaryStage.show();
    }

}
