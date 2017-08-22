package ru;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ServerThread extends Thread  {

    private PrintStream os;
    private BufferedReader is;
    private TextArea textArea;
    String str;
    public ServerThread(Socket socket, TextArea textArea) throws IOException{
        this.textArea = textArea;
        os = new PrintStream(socket.getOutputStream());
        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    public void run(){

        try {
            while ((str = is.readLine())!= null){
                if ("PING".equals(str)) {
                    os.println("PONG ");
                }
                Platform.runLater(() -> textArea.appendText(str+"\n"));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Сбой соединение");
        }finally {
            disconnect();
        }
    }

    private void disconnect() {

        if (os != null){
            os.close();
        }
        if (is != null){
            try {
                is.close();
                System.out.println("Соединение разорвано");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.interrupt();
            }
        }

    }
    public void sendMessage ( String message){
            os.println(message);
    }
}
