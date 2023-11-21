package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class ReceiverTask extends Task<Void> {
    private final ChatClient client;
    private final StringProperty latestMessage;

    public ReceiverTask(ChatClient client, StringProperty latestMessage) {
        this.client = client;
        this.latestMessage = latestMessage;
    }

    @Override
    protected Void call() throws IOException {
        Selector selector = this.client.getSelector();

        while (this.client.isLive()) {
            if (this.isCancelled()) {
                break;
            }

            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isValid() && key.isReadable()) {
                    String message = this.client.receiveMessage();

                    if (message != null) {
                        Platform.runLater(() -> this.latestMessage.setValue(message));
                    }
                }
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