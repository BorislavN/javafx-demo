package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.concurrent.Task;

import java.io.IOException;

public class SenderTask extends Task<Void> {
    private final ChatClient client;
    private final String message;

    public SenderTask(ChatClient client, String message) {
        this.client = client;
        this.message = message;
    }

    @Override
    protected Void call() {
        if (this.message == null) {
            throw new IllegalArgumentException("Message cannot be null!");
        }

        if (!this.isCancelled()) {
            try {
                this.client.sendMessage(this.message);
            } catch (IOException e) {
                System.err.println("Task failed to send message - " + this.message);
            }
        }

        return null;
    }
}