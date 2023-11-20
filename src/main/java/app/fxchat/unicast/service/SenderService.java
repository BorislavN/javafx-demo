package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.concurrent.Service;
import javafx.concurrent.Task;


// TODO: In the controller we will start the service,
//  then when we want to send a message we will check if the service is free,
//  if free - take the message and execute the task, if not - the message will be put in queue,
//  will crate an EventListener for the "Success" event
//  when invoked it will reset the service and poll a message from the queue, starting a task
public class SenderService extends Service<Void> {
    private final ChatClient client;
    private String currentMessage;

    public SenderService(ChatClient client) {
        this.client = client;
        this.currentMessage = null;
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