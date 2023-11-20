package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

public class ReceiverTask extends Task<Void> {
    private final ChatClient client;
    private final StringProperty latestMessage;

    public ReceiverTask(ChatClient client, StringProperty latestMessage) {
        this.client = client;
        this.latestMessage = latestMessage;
    }

    @Override
    protected Void call() {
        while (this.client.isLive()) {
            if (this.isCancelled()) {
                break;
            }

            String message = this.client.receiveMessage();

            if (message != null) {
                System.out.println("Task received message - "+message);
                Platform.runLater(() -> this.latestMessage.setValue(message));
            }
        }

        return null;
    }

//    private void logMessage(String group, int port, String message) {
//        System.out.println("----------------------------------------------");
//        System.out.println(Thread.currentThread().getName() + ":");
//        System.out.printf("%s:%d - %s%n", group, port, message);
//        System.out.println("----------------------------------------------");
//    }
}