package ru.connect;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import ru.ServerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectEth extends Service <String> {


    private static int PORT = 1001; //Номер порта сервера

    private BufferedReader is;
    private PrintStream os;
    private TextArea textArea;
    private ArrayList<ServerThread> arrayList;
    public ConnectEth(TextArea textArea, ArrayList<ServerThread> arrayList) {
        this.textArea = textArea;
        this.arrayList = arrayList;
    }

    public Integer getPort() {
        return PORT;
    }

    public void run(Socket socket){
        String str;
        try {
            os = new PrintStream(socket.getOutputStream());
            while ((str = is.readLine())!= null){
                System.out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage (Socket socket, String message){
        try {
            os = new PrintStream(socket.getOutputStream());
            os.println(message);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error connect";
        }
        return "Message send";
    }


    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                ServerSocket server = new ServerSocket(PORT);
                while (true){
                try {
                    System.out.println("Ждем нового соединения");
                    Socket socket = server.accept();

                    System.out.println("Размер массива" + arrayList.size());
                    System.out.println("Создано новое соединение");
                    ServerThread thread = new ServerThread(socket,textArea);
                    thread.start();

                    arrayList.add(0,thread);
                    Platform.runLater(() -> {
                        textArea.appendText(socket.getRemoteSocketAddress().toString() + "main/java/ru/connect\n");
                    });
                    Thread.sleep(1000);
                } catch (IOException e) {
                    System.out.println("Созданиие Thread закончено");
                    e.printStackTrace();
                }
                }
            }
        };
    }
}
