package ru.View;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.hibernate.Session;
import ru.DAO.UserDAOImpl;
import ru.DAO.UserDAOb;
import ru.ServerThread;
import ru.ServiceConcurrent;
import ru.connect.ConnectEth;
import ru.excel.WorkExcel;
import ru.model.GVIBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Draft extends Application {

    public TreeView <String> Model;
    public MenuBar menuBar;
    TextArea textArea;
    ConnectEth connectEth;
    ServiceConcurrent serviceConcurrent;
    ArrayList<ServerThread> listSocket = new ArrayList<>();
    private Stage primaryStage;
    private File model;
    public BorderPane borderPane;
    WorkExcel workExcel = new WorkExcel();
    ArrayList<TabPane> arrayTabpane = new ArrayList<>();
    boolean openingGV;
    boolean closingGV;
    Timer timer;
    TimerTask timerTask;
    VBox vBox;
    Slider slider;
    UserDAOb userDAOImpl = new UserDAOb();

    Timeline tlnOpen = new Timeline();
    Timeline tlnClose = new Timeline();

    KeyValue keyValue;
    KeyValue keyValueClose;

    boolean disMufta = false;
    boolean disAlarm = false;

    Integer speed = 5000;


    public void setWorkExcel(WorkExcel workExcel) {
        this.workExcel = workExcel;
    }

    GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //tlnOpen.setCycleCount(10);
        tlnOpen.setAutoReverse(true);

        System.out.println("start");
        this.primaryStage = primaryStage;
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        FlowPane root = new FlowPane();
        root.setAlignment(Pos.CENTER);
        primaryStage.setTitle("Имитатор интерфейсных устройств БЕТА");
        primaryStage.getIcons().add(new Image("/images/Symbol.png"));
       // primaryStage.setScene(new Scene(root, 500, 450));
        Canvas canvas = new Canvas(100,700);
        gc = canvas.getGraphicsContext2D();
        Button btnChangeColor = new Button("Change Color");
        btnChangeColor.setOnAction(event -> {
            System.out.println("Btn");
        });

        borderPane = new BorderPane();
        borderPane.setTop(createMenu(menuBar));

        borderPane.setBottom(textArea);
        borderPane.setLeft(Model = new TreeView<>(createTreate()));
        System.out.println(Model.getTreeItem(0));
        primaryStage.setScene(new Scene(borderPane, 1024, 768));
        painGV(gc,canvas);
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
//            if (timer != null) {
//                timer.cancel();
//            }
//            timer = new Timer();
//            timerTask = new TimerTask() {
//                @Override
//                public void run() {
//                    if (i != 101) {
//                        System.out.println("i= " + i);
//                        i++;
//
//                    } else {
//                        timer.cancel();
//                        timerTask.cancel();
//                    }
//                }};
//            timer.scheduleAtFixedRate(timerTask,0,250);
        });
        Button close = new Button("Закрыть");
        close.setOnAction(event -> {
            textArea.appendText("Подана команда закрыть \n");
            closingGV = true;
            openingGV = false;
            if (!(disAlarm || disMufta)) {
                tlnClose.play();
            }

//            if (timer != null) {
//                timer.cancel();
//
//            }
//            timer = new Timer();
//            timerTask = new TimerTask() {
//                @Override
//                public void run() {
//                    if (i != 0) {
//                        System.out.println("i= " + i);
//                        i--;
//                        //textField.setText(i+"");
//                    } else {
//                        timer.cancel();
//                        timerTask.cancel();
//                    }
//                }};
//            timer.scheduleAtFixedRate(timerTask,0,250);
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

        vBox = new VBox();
        vBox.getChildren().addAll(opened,closed
        ,space,mufta,alarm,opening,closing,slider
                ,textField,labelSet, open, close,stop
                ,speedLabel,speedField);
        vBox.getChildren().get(1).setVisible(false);


        // Титул
        ImageView title = new ImageView("/images/Title.png");
        title.autosize();
        title.setFitHeight(610);
        borderPane.setCenter(title);


       //Таблица
        TabPane tabPane = new TabPane();
        Tab tabA = new Tab();
        Tab tabB = new Tab();
        Tab tabC = new Tab();
        Tab tabD = new Tab();
        Tab tabE = new Tab();
        Tab tabAlarm = new Tab();
        tabA.setText("A Показания системы");
        tabB.setText("В Параметры настройки");
        tabC.setText("С Заводские настройки");
        tabD.setText("D Команды управления");
        tabE.setText("E Журнал аварий");
        tabAlarm.setText("Имитация аварий");
        tabPane.getTabs().addAll(tabA, tabB, tabC, tabD, tabE, tabAlarm);
        TableView table = new TableView();
        table.setEditable(true);
        TableColumn kod = new TableColumn("Код");
        TableColumn name = new TableColumn("Название");
        TableColumn value = new TableColumn("Значение");
        TableColumn unit = new TableColumn("Ед.изм.");;
        TableColumn type = new TableColumn("Тип");;
        TableColumn view = new TableColumn("Вид");;
        TableColumn adres = new TableColumn("Адрес");;
        TableColumn write = new TableColumn("Запись");;
        TableColumn min = new TableColumn("Мин.");;
        TableColumn max = new TableColumn("Макс.");;
        TableColumn def = new TableColumn("Завод.установ.");
        TableColumn koef = new TableColumn("Коэффициент.");;
        TableColumn size = new TableColumn("Размер");
        TableColumn descr = new TableColumn("Описание битов");

        kod.setMinWidth(100);
       // kod.setCellFactory(new PropertyValueFactory<Object, String>("string"));
        ObservableList<GVIBase> usersData = FXCollections.observableArrayList();
        usersData.add(new GVIBase("1.0","B0 ",9600,"Бод","STR","DEC",256,1,24,1152,192,0,1,"description"));

        kod.setCellValueFactory(new PropertyValueFactory<GVIBase, Double>("kod"));
        name.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("name"));
        value.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("value"));
        unit.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("unit"));
        type.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("type"));
        view.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("view"));
        adres.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("adres"));
        write.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("write"));
        min.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("min"));
        max.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("max"));
        def.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("def"));
        koef.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("koef"));
        size.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("size"));
        descr.setCellValueFactory(new PropertyValueFactory<GVIBase,String>("description"));


        table.getColumns().addAll(kod,name,value,unit,type,view,adres,write,min,max,def,koef,size,descr);

        table.setItems(usersData);

        tabA.setContent(table);

        //root.getChildren().addAll(canvas,btnChangeColor);
        primaryStage.show();

    }


    public static void create(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        System.out.println("Init");
        textArea = new TextArea();
        textArea.appendText("Init\n");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stop");
    }


//    Нарисуем задвижку
    public void painGV (GraphicsContext gc, Canvas canvas){

        double width = canvas.getWidth();
        double heigt = canvas.getHeight();
        double [] xTriangle1 = {10, 60, 10};
        double [] yTriangle1 = {10, 25, 40};
        gc.setFill(Color.GREEN);
        gc.fillPolygon(xTriangle1,yTriangle1,3);
        gc.strokePolyline(xTriangle1,yTriangle1,3);
        double [] xTriangle2 = {60, 110, 110};
        double [] yTriangle2 = {25, 10, 40};
        gc.fillPolygon(xTriangle2,yTriangle2,3);
        gc.strokePolygon(xTriangle2,yTriangle2,3);
        gc.setFill(Color.YELLOW);
        gc.fillRect(5,8,5,34);
        gc.strokeRect(5,8,5,34);
        gc.fillRect(110,8,5,34);
        gc.strokeRect(110,8,5,34);
        gc.fillRect(56,8,8,17);
        gc.strokeRect(56,8,8,17);
        gc.fillOval(53,0,15,15);
        gc.strokeOval(53,0,15,15);
    }

    public MenuBar createMenu (MenuBar menuBar){

        Menu fileMenu = new Menu("File");
        MenuItem open = new MenuItem("Open");
        open.setOnAction(event -> {
            FileChooser fileChooser =  new FileChooser();
            fileChooser.setTitle("Откройте Excel файл описания");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));

            model = fileChooser.showOpenDialog(primaryStage);
            System.out.println(model.getAbsolutePath());
            if (model != null) {
                arrayTabpane.add(0,workExcel.parseToApplicationGVI(model,Model,arrayTabpane));
                borderPane.setCenter(arrayTabpane.get(0));
                borderPane.setRight(vBox);
            }
        });
        MenuItem save = new MenuItem("Save");
        save.setOnAction(event -> {
            textArea.appendText(userDAOImpl.createTable(userDAOImpl.connection()));
        });

        MenuItem reload =  new MenuItem("Reload");
        reload.setOnAction(event -> {
            readDBtest();
//            textArea.appendText("Reload \n");
//            File file = new File("C:\\javaee\\ImitIntDev\\src\\main\\resources\\ПБЭ_2.5.xlsx");
//
//            if (file != null) {
//                arrayTabpane.add(0,workExcel.parseToApplicationGVI(file,Model,arrayTabpane));
//                borderPane.setCenter(arrayTabpane.get(0));
//                borderPane.setRight(vBox);
//            }
        });
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> Platform.exit());
        fileMenu.getItems().addAll(open,save,reload,exit);

        Menu imitator = new Menu("Imitator");
        MenuItem connect = new MenuItem("Connect");

        connect.setOnAction(event -> {
            ConnectEth connectEth = new ConnectEth(textArea,listSocket);
            connectEth.start();
        });
        MenuItem disconnect = new MenuItem("Disconnect");
        disconnect.setOnAction(event -> {
            if (connectEth != null){
                connectEth.cancel();
            }
        });
        MenuItem test = new MenuItem("test");
        test.setOnAction(event -> {
            serviceConcurrent = new ServiceConcurrent(textArea);
            serviceConcurrent.textArea = textArea;
            serviceConcurrent.start();
        });
        MenuItem stop1 = new MenuItem("stoptest");
        stop1.setOnAction(event -> {
            if (serviceConcurrent!=null){
                serviceConcurrent.cancel();
            }
        });
        MenuItem sendMessage = new MenuItem("SendMessage");
        sendMessage.setOnAction(event -> {
            TextField textField = new TextField();
            Button send = new Button("Send");
            send.setOnAction(event1 -> {
                if (listSocket.size()>0 ){
                    listSocket.get(0).sendMessage(textField.getText());
                    //connectEth.sendMessage(listSocket.get(0),textField.getText());
                    textField.clear();
                } else {
                    textArea.appendText("Нет ни одного соединения" + listSocket.size());
                }
            });
            FlowPane flowPane = new FlowPane();
            flowPane.getChildren().addAll(textField,send);
            borderPane.setCenter(flowPane);
        });
        MenuItem run = new MenuItem("Run");
        MenuItem stop = new MenuItem("Stop");
        run.setDisable(true);
        stop.setDisable(true);
        imitator.getItems().addAll(connect,disconnect,test,stop1,sendMessage,run,stop);
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu,imitator);
        return menuBar;
    }

    public TreeItem createTreate (){

        TreeItem <String> model = new TreeItem<>("Модели");
        TreeItem <String> gateValve = new TreeItem<>("Задвижки");
        gateValve.getChildren().add(new TreeItem<>("ПБЗ - 2.5"));
        gateValve.getChildren().add(new TreeItem<>("ПБЗ - 7.5"));

        TreeItem <String> sensorGas = new TreeItem<>("Датчики загазованности");
        sensorGas.getChildren().add(new TreeItem<>("Drager"));

        TreeItem <String> sensorLevel = new TreeItem<>("Уровномеры");
        sensorLevel.getChildren().add(new TreeItem<>("TankRadar"));

        TreeItem <String> sensorPress = new TreeItem<>("Датчики Давления");
        sensorPress.getChildren().add(new TreeItem<>("Yokogawa"));


       // model.getChildren().addAll(gateValve,sensorGas,sensorLevel,sensorPress);

        TreeView <String> treeView = new TreeView<>(model);
        System.out.println(treeView.getTreeItem(0));
        MultipleSelectionModel <TreeItem<String>> treeSelModel = treeView.getSelectionModel();
        treeSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                borderPane.setCenter(new TextField(newValue.getValue()));
                textArea.appendText("Select " + newValue.getValue() + "\n");
            }
        });
       // treeView.setShowRoot(true);
        return model;
    }


    public void readDBtest(){
        UserDAOImpl.init(); // Открытие соед. с БД
        Session s = UserDAOImpl.sessionFactory.getCurrentSession();
        s.beginTransaction();
//        List<ru.model.Model> modelList = s.createQuery("from Model").list();
//        for (ru.model.Model r : modelList) {
//            System.out.println(r.toString());
//        }
        List<GVIBase> gviBaseList = s.createQuery("from GVIBase").list();
        for (GVIBase r : gviBaseList) {
            System.out.println(r.toString());
        }
//        s.getTransaction().commit();
        s.close();
        UserDAOImpl.destroy();
    }

//        log.info("==============GET=================");
//         Session session = sessionFactory.getCurrentSession();
//        session.beginTransaction();
//        Region region = (Region) session.get(Region.class, id);
//        log.info("region = {}", region);
//        session.getTransaction().commit();
}




