package ru.View;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.hibernate.Session;
import ru.DAO.UserDAOImpl;
import ru.DAO.UserDAOb;
import ru.ServerThread;
import ru.ServiceConcurrent;
import ru.aloritms.WorkGVI;
import ru.connect.ConnectEth;
import ru.excel.WorkExcel;
import ru.model.GVIBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    //VBox vBox;
    UserDAOb userDAOImpl = new UserDAOb();
    Session session;




    public void setWorkExcel(WorkExcel workExcel) {
        this.workExcel = workExcel;
    }

    GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) throws Exception{


        System.out.println("start");
        this.primaryStage = primaryStage;
        FlowPane root = new FlowPane();
        root.setAlignment(Pos.CENTER);
        primaryStage.setTitle("Имитатор интерфейсных устройств БЕТА");
        primaryStage.getIcons().add(new Image("/images/Symbol.png"));

        //Рабочее окно
        borderPane = new BorderPane();
        borderPane.setTop(createMenu(menuBar));
        borderPane.setBottom(textArea);
        borderPane.setLeft(Model = new TreeView<>(createTreate()));
        System.out.println(Model.getTreeItem(0));
        primaryStage.setScene(new Scene(borderPane, 1024, 768));

        // Титул
        ImageView title = new ImageView("/images/Title.png");
        title.autosize();
        title.setFitHeight(610);
        borderPane.setCenter(title);

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
        UserDAOImpl.init(); // Открытие соед. с БД
        session = UserDAOImpl.sessionFactory.getCurrentSession();

    }

    @Override
    public void stop() throws Exception {
        UserDAOImpl.destroy();
        System.out.println("Конец Работы Программы");
        Platform.exit();
    }

    //*********************************
    //Нарисуем задвижку (тест рисования)
    //*********************************

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


    //********************
    //Создание Меню Панели
    //********************



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
                arrayTabpane.add(0,workExcel.parseToApplicationGVI(model,Model,arrayTabpane,session));
                borderPane.setCenter(arrayTabpane.get(0));
                borderPane.setRight(new WorkGVI().create(null,null,session,textArea));
            }
        });
        MenuItem save = new MenuItem("Save");
        save.setOnAction(event -> {
            textArea.appendText(userDAOImpl.createTable(userDAOImpl.connection()));
        });

        MenuItem reload =  new MenuItem("Reload");
        reload.setOnAction(event -> {
            readDBtest(session);
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
        exit.setOnAction(event -> {
            UserDAOImpl.destroy();
            System.out.println("Конец Работы Программы");
            Platform.exit();
            });
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


    //********************
    //***Чтение из БД*****
    //********************

    public void readDBtest(Session session){

        session.beginTransaction();
//        List<ru.model.Model> modelList = s.createQuery("from Model").list();
//        for (ru.model.Model r : modelList) {
//            System.out.println(r.toString());
//        }
        List<GVIBase> gviBaseList = session.createQuery("from GVIBase").list();
        for (GVIBase r : gviBaseList) {
            System.out.println(r.toString());
        }
//        s.getTransaction().commit();
        session.close();
    }

//        log.info("==============GET=================");
//         Session session = sessionFactory.getCurrentSession();
//        session.beginTransaction();
//        Region region = (Region) session.get(Region.class, id);
//        log.info("region = {}", region);
//        session.getTransaction().commit();


}




