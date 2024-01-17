package app.unicast.service;

import app.unicast.nio.ChatClient;
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
        try {
            this.logState(this.client, "ReceiverService starting...");

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
                            Platform.runLater(() -> {
                                if (message.equals(this.latestMessage.getValue())) {
                                    this.latestMessage.setValue(null);
                                }

                                this.latestMessage.setValue(message);
                            });
                        }
                    }
                }
            }

            return null;

        } finally {
            this.logState(this.client, "ReceiverService finishing...");
        }
    }

    private void logState(ChatClient client, String message) {
        System.out.println("----------------------------------------------");
        System.out.println(Thread.currentThread().getName() + ":");
        System.out.printf("%s:%d - %s%n", client.getAddress(), client.getPort(), message);
        System.out.println("----------------------------------------------");
    }
}