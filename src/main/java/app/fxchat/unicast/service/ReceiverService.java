package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

//TODO: Finish implementation,
// In the controller I will add a ChangeListener to this property, and when it's updated
// the message will be saved to a List<String> playing the role of history, and appended in the TextArea
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