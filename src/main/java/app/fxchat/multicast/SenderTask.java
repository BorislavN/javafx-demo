package app.fxchat.multicast;

import javafx.concurrent.Task;

public class SenderTask extends Task<Void> {
    private final MulticastClient client;
    private final String message;

    public SenderTask(MulticastClient client, String message) {
        this.client = client;
        this.message = message;
    }

    @Override
    protected Void call() throws IllegalArgumentException {
        if (this.message == null) {
            throw new IllegalArgumentException("Message cannot be null!");
        }

        if (!this.isCancelled()) {
            this.client.sendMessage(this.message);
        }

        return null;
    }
}