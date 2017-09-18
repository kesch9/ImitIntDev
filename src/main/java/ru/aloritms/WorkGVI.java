package ru.aloritms;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.hibernate.Session;
import ru.DAO.UserDAOb;
import ru.model.Model;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by sergej on 18.09.17.
 */
public class WorkGVI {

    boolean openingGV;
    boolean closingGV;
    Timer timer;
    //TimerTask timerTask;
    Slider slider;
    UserDAOb userDAOImpl = new UserDAOb();

    Timeline tlnOpen = new Timeline();
    Timeline tlnClose = new Timeline();

    KeyValue keyValue;
    KeyValue keyValueClose;

    boolean disMufta = false;
    boolean disAlarm = false;

    Integer speed = 5000;


    public VBox create (Model model, ArrayList<TabPane> viewModel, Session session, TextArea textArea) {

        tlnOpen.setAutoReverse(true);
        Button opened = new Button("Открыта");
        Button closed = new Button("Закрыта");
        Button space = new Button("Промежуток");
        CheckBox mufta = new CheckBox("Муфта");
        mufta.setOnAction(event -> {
            textArea.appendText("Сработала муфта \n");
            if (mufta.isSelected()){
                disMufta = true;
                tlnClose.stop();
                tlnOpen.stop();
            } else {
                disMufta = false;
            }
        });


        CheckBox alarm = new CheckBox("Авария");
        alarm.setOnAction(event -> {
            textArea.appendText("Авария на задвижке \n");
            if (alarm.isSelected()){
                disAlarm = true;
                tlnClose.stop();
                tlnOpen.stop();
            } else {
                disAlarm = false;
            }
        });
        Button opening = new Button("Открывается");
        Button closing = new Button("Закрывается");
        TextField textField = new TextField("Значение");
        Label labelSet = new Label("  Управление  ");
        Button open = new Button("Открыть");
        open.setOnAction(event ->{
            textArea.appendText("Подана команда открыть \n");
            openingGV = true;
            closingGV = false;
            if (!(disAlarm || disMufta)) {
                tlnOpen.play();
            }

        });
        Button close = new Button("Закрыть");
        close.setOnAction(event -> {
            textArea.appendText("Подана команда закрыть \n");
            closingGV = true;
            openingGV = false;
            if (!(disAlarm || disMufta)) {
                tlnClose.play();
            }

        });

        Button stop = new Button("Стоп");
        stop.setOnAction( event -> {
            textArea.appendText("Подана команда стоп \n");
            if (timer != null) {
                timer.cancel();
            }
            tlnOpen.stop();
            tlnClose.stop();
        });
        slider = new Slider(0, 100,40);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        keyValue = new KeyValue(slider.valueProperty(),100);
        keyValueClose = new KeyValue(slider.valueProperty(),0);
        tlnOpen.getKeyFrames().addAll(new KeyFrame(Duration.millis(speed),keyValue));
        tlnClose.getKeyFrames().addAll(new KeyFrame(Duration.millis(speed),keyValueClose));
        slider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue+"I");
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {

            if (openingGV && newValue.intValue() == 100){
                tlnOpen.stop();
            } else if(closingGV && newValue.intValue() == 0){
                tlnClose.stop();
            }
            textField.setText(newValue.intValue()+"");
            if (newValue.intValue() == 100) {
                opened.setVisible(true);
                closed.setVisible(false);
                space.setVisible(false);
            } else if (newValue.intValue() == 0){
                opened.setVisible(false);
                closed.setVisible(true);
                space.setVisible(false);
            } else {
                opened.setVisible(false);
                closed.setVisible(false);
                space.setVisible(true);
            }
        });

        Label speedLabel = new Label("время хода,мс");
        TextField speedField = new TextField(speed.toString());
        speedField.setOnAction(event -> {
            System.out.println("принт");
            speed = new Integer(speedField.getText().trim());
            textArea.appendText("Время ходя изменено на " + speed + "\n");
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(opened,closed
                ,space,mufta,alarm,opening,closing,slider
                ,textField,labelSet, open, close,stop
                ,speedLabel,speedField);
        vBox.getChildren().get(1).setVisible(false);

        return vBox;
    }

}
