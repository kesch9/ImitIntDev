package ru;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

/**
 * Created by user on 20.07.16.
 */
public class TaskConcurent extends Task <String> {

    private TextArea textArea;

    @Override
    protected void updateValue(String value) {
        textArea.appendText("Iteration " + value+"\n");
    }

    public TaskConcurent(TextArea textArea) {
        this.textArea = textArea;
    }



    @Override
    protected String call() throws Exception {
        Integer iterations;
        textArea.appendText("Старт \n");
        for (iterations = 0; iterations < 10; iterations++) {
            if (isCancelled()) {
               break;
            }
            updateValue(iterations.toString());
            textArea.appendText(iterations.toString());
            System.out.println("Iteration " + iterations);
            Thread.sleep(1000);

        }
        return iterations.toString();
    }
}
