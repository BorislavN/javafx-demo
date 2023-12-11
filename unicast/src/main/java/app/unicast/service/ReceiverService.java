package app.unicast.service;

import app.unicast.nio.ChatClient;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Executors;

public class ReceiverService extends Service<Void> {
    private final ChatClient client;
    private final ReadOnlyStringWrapper latestMessage;

    public ReceiverService(ChatClient client) {
        this.client = client;
        this.latestMessage = new ReadOnlyStringWrapper();

        //Creates new thread, only if current is occupied
        this.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);

            return thread;
        }));
    }

    public ReadOnlyStringProperty latestMessageProperty() {
        return this.latestMessage.getReadOnlyProperty();
    }

    @Override
    protected Task<Void> createTask() {
        return new ReceiverTask(this.client, this.latestMessage);
    }
}