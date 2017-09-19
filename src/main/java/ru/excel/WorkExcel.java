package ru.excel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import ru.DAO.UserDAOImpl;
import ru.model.CKCBase;
import ru.model.GVIBase;
import ru.model.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkExcel {

    Matcher matcher;
    Pattern pattern = Pattern.compile("X.X.X");

    private static final Logger log = Logger.getLogger(WorkExcel.class);

    public String parse(String name) {
        String result = "";
        InputStream in = null;
        XSSFWorkbook wb = null;
        try {
            in = new FileInputStream(name);
            wb = new XSSFWorkbook(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Sheet sheet = wb.getSheetAt(0);
        Sheet sheet1 = wb.getSheetAt(0);
        System.out.println(sheet.isDisplayRowColHeadings());
        System.out.println(sheet1.getPaneInformation());
        System.out.println(sheet.getSheetName()+ wb.getNumberOfSheets());
        Iterator<Row> it = sheet.iterator();
        while (it.hasNext()) {
            Row row = it.next();
            Iterator<Cell> cells = row.iterator();
            while (cells.hasNext()) {
                Cell cell = cells.next();
                int cellType = cell.getCellType();
                switch (cellType) {
                    case Cell.CELL_TYPE_STRING:
                        result += cell.getStringCellValue() + "S";
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        result += "[" + cell.getNumericCellValue() + "N";
                        break;

                    case Cell.CELL_TYPE_FORMULA:
                        result += "[" + cell.getNumericCellValue() + "F";
                        break;
                    default:
                        result += "|";
                        break;
                }
            }
            result += "\n";
        }

        return result;
    }

    public TabPane parseToApplicationGVI(File file, TreeView<String>treeView, ArrayList<TabPane>arrayList){

        //**********************
        //***Create new Table***
        //**********************

        TabPane tabPane = new TabPane();
        tabPane.setId(file.getName());
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

        //***********************
        //*****Add New Item******
        //***********************

        treeView.getTreeItem(0).getChildren().add(new TreeItem<>(file.getName()));
        MultipleSelectionModel <TreeItem<String>> treeSelModel = treeView.getSelectionModel();
        treeSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                System.out.println("hello");
//                borderPane.setCenter(new TextField(newValue.getValue()));
//                textArea.appendText("Select " + newValue.getValue() + "\n");
            }
        });


        Model modelGVI = new Model("Задвижки", "Модель задвижки");
        modelGVI.setModelId((long)1);
        log.debug(modelGVI.toString());
        UserDAOImpl.init();
        Session s = UserDAOImpl.sessionFactory.getCurrentSession();
        s.beginTransaction();
        s.save(modelGVI);
        //s.getTransaction().commit();

        //***********************
        //****Hibernate init*****
        //***********************

//        UserDAOImpl.init(); // Открытие соед. с БД
//
//
//        Session s = UserDAOImpl.sessionFactory.getCurrentSession();
//        s.beginTransaction();

        //***********************
        //***Prepare read file***
        //***********************

        InputStream in = null;
        XSSFWorkbook wb = null;
        try {
            in = new FileInputStream(file);
            wb = new XSSFWorkbook(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        log.debug("Количество страниц в файле: " + wb.getNumberOfSheets());

        //**********************************
        //Начинаем считывать данные из файла
        //**********************************


        ArrayList<ObservableList> arrTab = new ArrayList<>();
        int i=0;
        Row row = it.next();
        boolean tabIN = false;
        boolean first = true;
        while (it.hasNext()) {
            if (!tabIN && !first){
                row = it.next();
                tabIN=false;
            }
            first = false;
            Iterator<Cell> cells = row.iterator();
            while (cells.hasNext()) {
                Cell cell = cells.next();
                if (cell.getCellType() == Cell.CELL_TYPE_STRING){
                    pattern = Pattern.compile("\\d.0.0.\\d+");
                    matcher = pattern.matcher(cell.getStringCellValue());
                    if (matcher.matches()) {
                        ObservableList<GVIBase> usersData = FXCollections.observableArrayList();
                        TableView table = new TableView<>();
                        arrTab.add(i,usersData);
                        table.setEditable(true);
                        table.getColumns().addAll(kod,name,value,unit,type,view,adres,write,min,max,def,koef,size,descr);
                        Cell cellId = cells.next();
                        Tab tab = new Tab(cellId.getStringCellValue());
                        it.next();
                        Iterator<Cell> tabCells = it.next().iterator();
                        Cell tabCell = tabCells.next();
                        pattern = Pattern.compile("\\d+.0.\\d+");
                        matcher = pattern.matcher(tabCell.getStringCellValue());
                        while (matcher.matches()) {
                            log.debug("вошел");

                            GVIBase gviBase = new GVIBase(
                                    tabCell.getStringCellValue(),
                                    tabCells.next().getStringCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    tabCells.next().getStringCellValue(),
                                    tabCells.next().getStringCellValue(),
                                    tabCells.next().getStringCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    (int) tabCells.next().getNumericCellValue(),
                                    tabCells.next().getStringCellValue()
                                    );
                            // сохранение в БД
                            arrTab.get(i).add(gviBase);
                            gviBase.setModel(modelGVI);
                            s.save(gviBase);
                            if (it.hasNext()) {
                                row = it.next();
                                tabIN = true;
                                tabCells = row.iterator();
                                tabCell = tabCells.next();
                                matcher = pattern.matcher(tabCell.getStringCellValue());
                            } else {
                                matcher = pattern.matcher(tabCell.getStringCellValue());
                                break;
                            }
                        }
                        arrTab.add(i,usersData);
                        table.setItems(arrTab.get(i));

                        tab.setContent(table);
                        tabPane.getTabs().add(tab);
                        i++;
                    }
                }
            }
        }

        try {
            in.close();
            s.getTransaction().commit();

        } catch (IOException e) {
            log.debug("Файл не был открыт");
        }
        return tabPane;
    }

    public TabPane parseToApplicationCKC(File file, TreeView<String>treeView, ArrayList<TabPane>arrayList){

        //**********************
        //***Create new Table***
        //**********************

        TabPane tabPane = new TabPane();
        tabPane.setId(file.getName());
        TableColumn name = new TableColumn("Название");
        TableColumn value = new TableColumn("Значение");
        TableColumn adres = new TableColumn("Адрес");;
        TableColumn descr = new TableColumn("Описание битов");

        name.setCellValueFactory(new PropertyValueFactory<CKCBase, String>("name"));
        value.setCellValueFactory(new PropertyValueFactory<CKCBase, String>("value"));
        adres.setCellValueFactory(new PropertyValueFactory<CKCBase, Integer>("adres"));
        descr.setCellValueFactory(new PropertyValueFactory<CKCBase,String>("description"));

        //***********************
        //*****Add New Item******
        //***********************

        treeView.getTreeItem(0).getChildren().add(new TreeItem<>(file.getName()));
        MultipleSelectionModel <TreeItem<String>> treeSelModel = treeView.getSelectionModel();
        Model modelCKC = new Model("СКС", "Модель СКС-07");
       // modelCKC.setModelId((long)1);
        log.debug(modelCKC.toString());
        UserDAOImpl.init();
        Session s = UserDAOImpl.sessionFactory.getCurrentSession();
        s.beginTransaction();
        s.save(modelCKC);

        //***********************
        //***Prepare read file***
        //***********************

        InputStream in = null;
        XSSFWorkbook wb = null;
        try {
            in = new FileInputStream(file);
            wb = new XSSFWorkbook(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        log.debug("Количество страниц в файле: " + wb.getNumberOfSheets());

        //**********************************
        //Начинаем считывать данные из файла
        //**********************************

        ArrayList<ObservableList> arrTab = new ArrayList<>();
        ObservableList<GVIBase> usersData = FXCollections.observableArrayList();
        TableView table = new TableView<>();
        int i = 0;
        Tab tab = new Tab(sheet.getSheetName());
        arrTab.add(i, usersData);
        table.setEditable(true);
        table.getColumns().addAll(name, adres, value, descr);
        it.next();
        while (it.hasNext()) {
            Row row = it.next();
            Iterator<Cell> cells = row.iterator();
//            log.debug(cells.next().getStringCellValue());
//            log.debug(cells.next().getNumericCellValue());
//            log.debug(cells.next().getNumericCellValue());
//            log.debug(cells.next().getStringCellValue());
            CKCBase ckcBase = new CKCBase(
                    cells.next().getStringCellValue(),
                    cells.next().getNumericCellValue(),
                    cells.next().getNumericCellValue(),
                    cells.next().getStringCellValue()
            );
            // сохранение в БД
            arrTab.get(i).add(ckcBase);
            ckcBase.setModel(modelCKC);
            s.save(ckcBase);
            arrTab.add(i, usersData);
            table.setItems(arrTab.get(i));
            tab.setContent(table);
            tabPane.getTabs().add(tab);
            i++;
        }
        try {
            in.close();
            s.getTransaction().commit();// Закрытие соед. с БД
        } catch (IOException e) {
            log.debug("Файл не был открыт");
        }
        return tabPane;
    }

}
