package ru;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

public class ServiceConcurrent extends Service <String> {

    public TextArea textArea;

    public ServiceConcurrent(TextArea textArea) {
        this.textArea = textArea;
        textArea.appendText("create");

    }

    @Override
    protected Task<String> createTask() {

        return new Task<String>() {
            Integer iterations;

            @Override
            protected void updateProgress(long workDone, long max) {
                Long l = workDone;
                textArea.appendText(l.toString());
            }

            protected String call() throws Exception {
                while (true) {
                    for (iterations = 0; iterations < 10; iterations++) {
                        if (isCancelled()) {
                            break;
                        }
                        System.out.println("Iteration " + iterations);
                        Platform.runLater(() -> textArea.appendText(iterations.toString()));
                        Thread.sleep(1000);
                    }
                }
            }
        };
    }
}
