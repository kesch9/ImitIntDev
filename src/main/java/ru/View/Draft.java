package ru.View;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.DAO.UserDAOImpl;
import ru.DAO.UserDAOb;
import ru.ServerThread;
import ru.ServiceConcurrent;
import ru.algoritms.WorkCKC;
import ru.algoritms.WorkGVI;
import ru.connect.ConnectEth;
import ru.excel.WorkExcel;
import ru.model.CKCBase;
import ru.model.GVIBase;
import ru.model.Model;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Draft extends Application {

    TreeView <String> Model;
    MenuBar menuBar;
    private TextArea textArea;
    ConnectEth connectEth;
    private ServiceConcurrent serviceConcurrent;
    ArrayList<ServerThread> listSocket = new ArrayList<>();
    private Stage primaryStage;
    private File model;
    private BorderPane borderPane;
    WorkExcel workExcel = new WorkExcel();
    ArrayList<TabPane> arrayTabpane = new ArrayList<>();
    //VBox vBox;
    UserDAOb userDAOImpl = new UserDAOb();
    private Session session;
    private static final Logger log = Logger.getLogger(Draft.class);
    protected SimpleProcessImage simpleProcessImage;
    protected ModbusTCPListener listener;
    private TreeView treeView;


    public final int DEFAULT_UNIT_ID = 1;

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

        //********************************
        //Заполним Modbus Holding Register
        //********************************
        simpleProcessImage = new SimpleProcessImage();
        for (int i = 0; i<4095; i++){
            simpleProcessImage.addRegister(new SimpleRegister(0));
        }
        ModbusCoupler.getReference().setProcessImage(simpleProcessImage);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(DEFAULT_UNIT_ID);

        //********************************
        //*********Рабочее окно***********
        //********************************

        borderPane = new BorderPane();
        borderPane.setTop(createMenu(menuBar));
        borderPane.setBottom(textArea);
        treeView = createTreateFromDB();
        borderPane.setLeft(treeView);
        primaryStage.setScene(new Scene(borderPane, 1024, 768));

        //********************************
        //************Титул***************
        //********************************

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
//        UserDAOImpl.init();
//        session = UserDAOImpl.sessionFactory.getCurrentSession();

    }

    @Override
    public void stop() throws Exception {
        UserDAOImpl.destroy();
        if (listener != null && listener.isListening()){
            listener.stop();
            log.debug("Modbus поток остановлен\n");
        }
        System.out.println("Конец Работы Программы\n");
        Platform.exit();
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
            if (model != null) {
                if (model.getName().contains("ПБЭ")){
                    borderPane.setCenter(workExcel.parseToApplicationGVI(model,treeView,arrayTabpane,simpleProcessImage));
                    log.debug("Добавлена Задвижка с №ID " + workExcel.getIdModel());
                    borderPane.setRight(new WorkGVI().create(workExcel.getIdModel(),null,textArea,simpleProcessImage));
                }
                if (model.getName().contains("СКС")){
                    System.out.println("СКС");
                    borderPane.setCenter(workExcel.parseToApplicationCKC(model,treeView,arrayTabpane));
                    log.debug("Добавлена СКС с №ID " + workExcel.getIdModel());
                    borderPane.setRight(new WorkCKC().create(workExcel.getIdModel(),null));
                }
            }
            //Доработать вызов
            treeView = createTreateFromDB();
            borderPane.setLeft(treeView);

        });
        MenuItem save = new MenuItem("Save");
        save.setOnAction(event -> {
            textArea.appendText(userDAOImpl.createTable(userDAOImpl.connection()));
        });

        MenuItem reload =  new MenuItem("Reload");
        reload.setOnAction(event -> {
            readDBtestCKC(Long.valueOf(24));
        });

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> {
            UserDAOImpl.destroy();
            if (listener != null && listener.isListening()){
                listener.stop();
                log.debug("Modbus поток остановлен\n");
            }
            System.out.println("Конец Работы Программы\n");
            Platform.exit();
            });
        fileMenu.getItems().addAll(open,save,reload,exit);

        Menu imitator = new Menu("Imitator");
        MenuItem connect = new MenuItem("Connect ModbusTCP");

        connect.setOnAction(event -> {
            //ConnectEth connectEth = new ConnectEth(textArea,listSocket);
            //connectEth.start();
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Ввод адреса устройства");
            dialog.setContentText("Введите адрес имитируемого устройства (1-255)");

            // Traditional way to get the response value.
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                try {
                    ModbusCoupler.getReference().setUnitID(Integer.valueOf(result.get()));
                    log.debug("ID устройства изменен на " + ModbusCoupler.getReference().getUnitID());
                } catch (NumberFormatException e){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Ошибка ввода");
                    alert.setHeaderText(null);
                    alert.setContentText("Вы ввели не число, id устройство останется по умолчанию ID=1");
                    alert.showAndWait();
                }
            }
            log.debug("ID устройства " + ModbusCoupler.getReference().getUnitID());
            try {
                listener = new ModbusTCPListener(3, InetAddress.getByName("192.168.1.2"));
            } catch (UnknownHostException e) {
                log.debug("Не удалось запустить поток ModbusTCP\n");
                textArea.appendText("Не удалось запустить поток ModbusTCP. Проверьте кабель\n");
            }
            listener.setPort(1234);
            listener.start();
            if (listener.isListening()) {
                log.debug("ModbusTCPListener запущен\n");
                textArea.appendText("ModbusTCPListener запущен\n");
            }

        });
        MenuItem disconnect = new MenuItem("Disconnect ModbusTCP");
        disconnect.setOnAction(event -> {
            if (connectEth != null){
                connectEth.cancel();
            }
            if (listener != null &&listener.isListening()){

                textArea.appendText("Modbus поток остановлен\n");
                log.debug("Modbus поток остановлен\n");
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
                    textArea.appendText("Нет ни одного соединения" + listSocket.size()+"\n");
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


    //********************
    //***Чтение из БД*****
    //********************

    public void readDBtestGVI(Long id){

        UserDAOImpl.init();
        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria userCriteria = session.createCriteria(GVIBase.class);
        userCriteria.add(Restrictions.eq("model.modelId", id));
        //List<GVIBase> gviBaseList = session.createQuery("from GVIBase").list();
        List<GVIBase> gviBaseList = userCriteria.list();
        gviBaseList.sort(Comparator.comparing(GVIBase::getGviId));
        borderPane.setCenter(reloadTabpaneCreateGVI(gviBaseList));
        borderPane.setRight(new WorkGVI().create(id,null,textArea,simpleProcessImage));
        session.getTransaction().commit();
    }

    public void readDBtestCKC(Long id){

        UserDAOImpl.init();
        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria userCriteria = session.createCriteria(CKCBase.class);
        userCriteria.add(Restrictions.eq("model.modelId", id));
        List<CKCBase> ckcBaseList = userCriteria.list();
        ckcBaseList.sort(Comparator.comparing(CKCBase::getCkcId));
        borderPane.setCenter(reloadTabpaneCreateCKC(ckcBaseList));
        borderPane.setRight(new WorkCKC().create(workExcel.getIdModel(),null));
        session.getTransaction().commit();
    }

    //*****************************
    //***Чтение из БД Задвижки*****
    //*****************************
    public TabPane reloadTabpaneCreateGVI(List<GVIBase> gviBaseList){

        TabPane tabPane = new TabPane();
        tabPane.setId("ПБЭ");
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
        kod.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("kod"));
        name.setCellValueFactory(new PropertyValueFactory<GVIBase, String>("name"));
        value.setCellValueFactory(new PropertyValueFactory<GVIBase, Integer>("value"));
        value.setCellFactory(TextFieldTableCell.<GVIBase,Integer>forTableColumn(new IntegerStringConverter()));

        value.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<GVIBase,Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<GVIBase,Integer> event) {
                        Integer integer = event.getNewValue();
                        int row = event.getTablePosition().getRow();
                        GVIBase gviBase = event.getTableView().getItems().get(row);
                        gviBase.setValue(integer);
                        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
                        session.beginTransaction();
                        session.update(gviBase);
                        session.getTransaction().commit();
                        System.out.println("Новое значение" + integer);
                    }
                });
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

        int i = 0;
//        final ObservableList<GVIBase> tableData = FXCollections.observableArrayList();
//        ArrayList<ObservableList> observableLists = new ArrayList<>();
//        observableLists.add(i, FXCollections.observableArrayList());
        ObservableList<GVIBase> observableList = FXCollections.observableArrayList();
        TableView table = new TableView<>(observableList);
        table.setEditable(true);
        table.getColumns().addAll(kod,name,value,unit,type,view,adres,write,min,max,def,koef,size,descr);
//        ArrayList<TableView> tableViews = new ArrayList<>();
        Tab tab = new Tab();
        tab.setText("ЗадвижкаПБЭ");
        tab.setContent(table);
        tabPane.getTabs().add(tab);
        for (GVIBase r : gviBaseList) {
//            Pattern pattern = Pattern.compile("\\d.0.0");
//            Matcher matcher = pattern.matcher(r.getKod());
//            if (matcher.matches()){
//                System.out.println("Вошли");
//                if (table != null) {
//                    System.out.println("Создаем Tab");
//                    Tab tab = new Tab();
//                    tab.setText("ЗадвижкаПБЭ");
//                    tab.setContent(tableViews.get(i-1));
//                    tabPane.getTabs().add(tab);
//                }
//                observableLists.add(i, FXCollections.observableArrayList());
//                table = new TableView<>(observableLists.get(i));
//                table.setEditable(true);
//                table.getColumns().addAll(kod,name,value,unit,type,view,adres,write,min,max,def,koef,size,descr);
//                tableViews.add(i,table);
//                i++;
//            }
            observableList.add(r);
        };
        //Tab tab = new Tab();
        //tab.setText("Номер вкладки" + i);
        //tab.setContent(table);
        //tabPane.getTabs().add(tab);
//        for (ObservableList<GVIBase>observableLists1 : observableLists){
//            for (GVIBase gviBase : observableLists1){
//                System.out.println(gviBase.toString());
//            }
//        }
        return tabPane;
    }
    //*****************************
    //******Чтение из БД СКС*******
    //*****************************
    public TabPane reloadTabpaneCreateCKC(List<CKCBase> ckcBaseList){

        TabPane tabPane = new TabPane();
        tabPane.setId("CKC");
        TableColumn name = new TableColumn("Название");
        TableColumn value = new TableColumn("Значение");
        TableColumn adres = new TableColumn("Адрес");;
        TableColumn descr = new TableColumn("Описание битов");

        name.setCellValueFactory(new PropertyValueFactory<CKCBase, String>("name"));
        value.setCellValueFactory(new PropertyValueFactory<CKCBase, String>("value"));
        adres.setCellValueFactory(new PropertyValueFactory<CKCBase, Integer>("adres"));
        descr.setCellValueFactory(new PropertyValueFactory<CKCBase,String>("description"));
        value.setCellFactory(TextFieldTableCell.<GVIBase,Integer>forTableColumn(new IntegerStringConverter()));
        value.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CKCBase,Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CKCBase,Integer> event) {
                        Integer integer = event.getNewValue();
                        int row = event.getTablePosition().getRow();
                        CKCBase ckcBase = event.getTableView().getItems().get(row);
                        ckcBase.setValue(integer);
                        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
                        session.beginTransaction();
                        session.update(ckcBase);
                        session.getTransaction().commit();
                        System.out.println("Новое значение value =" + integer);
                    }
                });

        int i = 0;

        ObservableList<CKCBase> observableList = FXCollections.observableArrayList();
        TableView table = new TableView<>(observableList);
        table.setEditable(true);
        table.getColumns().addAll(name, adres, value, descr);
        Tab tab = new Tab();
        tab.setText("CKC");
        tab.setContent(table);
        tabPane.getTabs().add(tab);
        for (CKCBase r : ckcBaseList) {

            observableList.add(r);
        };
        return tabPane;
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

    //*********************************
    //Создание дерева (тест)
    //*********************************

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

    public TreeView createTreateFromDB(){

        TreeItem <String> model = new TreeItem<>("Модели");
        log.debug("Создаем Ветки");

        UserDAOImpl.init();
        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
        session.beginTransaction();


        //****Задвижки******
        Criteria userCriteria = session.createCriteria(Model.class);
        userCriteria.add(Restrictions.eq("modelName", "Задвижки"));
        List<Model> modelList = userCriteria.list();
        modelList.sort(Comparator.comparing(ru.model.Model::getModelId));
        TreeItem <String> gateValve = new TreeItem<>("Задвижки");
        for (Model m: modelList){
            gateValve.getChildren().add(new TreeItem<>("Mодель №" + m.getModelId() + " Тип " + m.getDescription()));
        }


        //******CKC*******
        userCriteria = session.createCriteria(Model.class);
        userCriteria.add(Restrictions.eq("modelName", "СКС"));
        modelList = userCriteria.list();
        modelList.sort(Comparator.comparing(ru.model.Model::getModelId));
        TreeItem <String> ckc = new TreeItem<>("СКС");
        for (Model m: modelList){
            ckc.getChildren().add(new TreeItem<>("№" + m.getModelId() + " Тип " + m.getDescription()));
        }


        model.getChildren().addAll(gateValve, ckc);
        session.getTransaction().commit();
        TreeView <String> treeView = new TreeView<>(model);


        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2){

                    log.debug("Выбрали "+ treeView.getSelectionModel().getSelectedItem().getValue());
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(treeView.getSelectionModel().getSelectedItem().getValue());
                    if (matcher.find()){
                        //log.debug(matcher.group());
                        int id = Integer.valueOf(matcher.group());
                        UserDAOImpl.init();
                        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
                        session.beginTransaction();
                        Criteria userCriteria = session.createCriteria(Model.class);
                        userCriteria.add(Restrictions.eq("modelId", Long.valueOf(id)));
                        Model model = (Model) userCriteria.uniqueResult();
                        log.debug("Считали модель " + model.getDescription() + " id " + model.getModelId() + model.getModelName());

                        if (model.getModelName().equals("Задвижки")) {
                            log.debug("Выбрали Задвижку");
                            readDBtestGVI(Long.valueOf(model.getModelId()));

                        }
                        if (model.getModelName().equals("СКС")){
                            log.debug("Выбрали CKC");
                            readDBtestCKC(Long.valueOf(model.getModelId()));

                        }
                        session.getTransaction().commit();
                    }

               }
            }
        });
        treeView.setCellFactory(new Callback<TreeView<String>,TreeCell<String>>(){
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                return new TextFieldTreeCellImpl();
            }
        });
        return treeView;

    }

    private final class TextFieldTreeCellImpl extends TreeCell<String> {

        private TextField textField;
        private ContextMenu addMenu = new ContextMenu();

        public TextFieldTreeCellImpl() {
            MenuItem addMenuItem = new MenuItem("Delete");
            addMenu.getItems().add(addMenuItem);
            addMenuItem.setOnAction(new EventHandler() {
                public void handle(Event t) {

                    log.debug("Выбрали для удаления модель "+ getTreeView().getSelectionModel().getSelectedItem().getValue());
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(getTreeView().getSelectionModel().getSelectedItem().getValue());
                    if (matcher.find()){
                        int id = Integer.valueOf(matcher.group());
                        UserDAOImpl.init();
                        Session session = UserDAOImpl.sessionFactory.getCurrentSession();
                        session.beginTransaction();
                        Criteria userCriteria = session.createCriteria(Model.class);
                        userCriteria.add(Restrictions.eq("modelId", Long.valueOf(id)));
                        Model model = (Model) userCriteria.uniqueResult();
                        log.debug("Считали модель " + model.getDescription() + " id " + model.getModelId() + model.getModelName());
                        if (model.getModelName().equals("Задвижки")) {
                            log.debug("Выбрали Задвижку");
                            Criteria userCriteria1 = session.createCriteria(GVIBase.class);
                            userCriteria1.add(Restrictions.eq("model.modelId", model.getModelId()));
                            List <GVIBase> gviBase = userCriteria1.list();
                            for (GVIBase g : gviBase){
                                session.delete(g);
                            }
                            Model delModel = (Model) session.load(Model.class, model.getModelId());
                            log.debug("Удаляем модель с ID " + delModel.getModelId());
                            session.delete(delModel);

//                            Model delModel = (Model) session.load(Model.class, model.getModelId());
//                            log.debug("Удаляем модель с ID " + delModel.getModelId());
//                            session.delete(delModel);
                        }
                        if (model.getModelName().equals("СКС")){
                            log.debug("Выбрали CKC");
                            Model delModel = (Model) session.load(Model.class, model.getModelId());
                            session.delete(delModel);
                        }
                        session.getTransaction().commit();
                    }
                    treeView = createTreateFromDB();
                    borderPane.setLeft(treeView);
                }
            });
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    if (
                            !getTreeItem().isLeaf()&&getTreeItem().getParent()!= null
                            ){
                        setContextMenu(addMenu);
                    }
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });

        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }


}




