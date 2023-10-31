package app.fxchat.multicast;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class SenderService extends Service<Void> {
    private final MulticastClient client;
    private String currentMessage;

    public SenderService(MulticastClient client) {
        this.client = client;
        this.currentMessage = null;
    }

    public void sendMessage(String username, String message) {
        this.currentMessage = String.format("%s: %s", username, message);

        this.executeTask(this.createTask());
    }

    public void sendMessage(String message) {
        this.currentMessage = message;

        this.executeTask(this.createTask());
    }

    @Override
    protected Task<Void> createTask() {
        return new SenderTask(this.client, this.currentMessage);
    }
}