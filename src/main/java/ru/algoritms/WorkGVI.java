package ru.algoritms;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.DAO.UserDAOImpl;
import ru.DAO.UserDAOb;
import ru.model.GVIBase;

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

    Integer speed = 10000;
    Integer Status;
    Integer Position;
    Integer Alarm;

    public void setID(Long ID) {
        this.ID = ID;
    }

    private Long ID;

    public void writeDB(int value, Long model, String kod){
        UserDAOImpl.init();
        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria userCriteria = session.createCriteria(GVIBase.class);
        userCriteria.add(Restrictions.and(
                Restrictions.eq("model.modelId",model),
                Restrictions.eq("kod",kod)
        ));
        GVIBase gvi = (GVIBase) userCriteria.uniqueResult();
        gvi.setValue(value);
        session.saveOrUpdate(gvi);
        session.getTransaction().commit();
    }

    public VBox create (Long modelID, ArrayList<TabPane> viewModel, TextArea textArea, SimpleProcessImage simpleProcessImage) {

        this.ID = modelID;

        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
        //session.beginTransaction();
        Criteria userCriteria = session.createCriteria(GVIBase.class);
        userCriteria.add(Restrictions.and(
                Restrictions.eq("model.modelId",ID),
                Restrictions.eq("kod","0.0.7")
        ));
        GVIBase gviPol = (GVIBase) userCriteria.uniqueResult();
        Position = gviPol.getValue();
        userCriteria = session.createCriteria(GVIBase.class);
        userCriteria.add(Restrictions.and(
                Restrictions.eq("model.modelId",ID),
                Restrictions.eq("kod","0.0.1")
        ));
        gviPol = (GVIBase) userCriteria.uniqueResult();
        Status = gviPol.getValue();
        userCriteria = session.createCriteria(GVIBase.class);
        userCriteria.add(Restrictions.and(
                Restrictions.eq("model.modelId",ID),
                Restrictions.eq("kod","0.0.2")
        ));
        gviPol = (GVIBase) userCriteria.uniqueResult();
        Alarm = gviPol.getValue();
        //session.getTransaction().commit();

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
            writeDB(slider.valueProperty().getValue().intValue(), this.ID, "0.0.7");
            tlnOpen.stop();
            tlnClose.stop();
        });


        slider = new Slider(0, 100,Position);
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

            simpleProcessImage.setRegister(1,new SimpleRegister(newValue.intValue()));

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
                writeDB(slider.valueProperty().getValue().intValue(),this.ID, "3.0.0");
            } else if (newValue.intValue() == 0){
                opened.setVisible(false);
                closed.setVisible(true);
                space.setVisible(false);
                writeDB(slider.valueProperty().getValue().intValue(),this.ID, "3.0.0");
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
