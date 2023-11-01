package app.fxchat.multicast;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

//"The Service by default uses a thread pool Executor with some unspecified default or maximum thread pool size.
// This is done so that naive code will not completely swamp the system by creating thousands of Threads." - javafx.concurrent.Service
public class SenderService extends Service<Void> {
    private final MulticastClient client;
    private String currentMessage;

    public SenderService(MulticastClient client) {
        this.client = client;
        this.currentMessage = null;
    }

    //The tasks are shortLived, we create many threads, but they complete fast
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