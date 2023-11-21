package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ReceiverService extends Service<Void> {
    private final ChatClient client;
    private final ReadOnlyStringWrapper latestMessage;

    public ReceiverService(ChatClient client) {
        this.client = client;
        this.latestMessage = new ReadOnlyStringWrapper();
    }

    public ReadOnlyStringProperty latestMessageProperty() {
        return this.latestMessage.getReadOnlyProperty();
    }

    @Override
    protected Task<Void> createTask() {
        return new ReceiverTask(this.client, this.latestMessage);
    }
}