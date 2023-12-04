package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Executors;

public class SenderService extends Service<Void> {
    private final ChatClient client;
    private String currentMessage;

    public SenderService(ChatClient client) {
        this.client = client;
        this.currentMessage = null;

        //Creates new thread, only if current is occupied
        this.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);

            return thread;
        }));
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public String getCurrentMessage() {
        return this.currentMessage;
    }

    @Override
    protected Task<Void> createTask() {
        return new SenderTask(this.client, this.currentMessage);
    }
}