package app.unicast.service;

import app.unicast.nio.ChatClient;
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
    protected Void call() throws IOException {
        if (this.message == null) {
            throw new IllegalArgumentException("Message cannot be null!");
        }

        if (!this.isCancelled()) {
            this.client.sendMessage(this.message);
        }

        return null;
    }
}