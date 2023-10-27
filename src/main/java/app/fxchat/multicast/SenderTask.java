package app.fxchat.multicast;

import javafx.concurrent.Task;

public class SenderTask extends Task<Void> {
    private final MulticastClient client;
    private final String username;
    private final String message;

    public SenderTask(MulticastClient client, String username,String message) {
        this.client = client;
        this.username = username;
        this.message = message;
    }

    @Override
    protected Void call() throws Exception {
        if (this.username!=null){
            this.client.sendMessage(this.username,this.message);

            return null;
        }

        this.client.sendMessage(this.message);

        return null;
    }
}