package app.fxchat.multicast;

import javafx.concurrent.Task;

public class ReceiverTask extends Task<String> {
    private final MulticastClient client;

    public ReceiverTask(MulticastClient client) {
        this.client = client;
    }

    @Override
    protected String call() throws Exception {
        String latestMessage="ReceiverTask starting...";

        while (this.client.isLive()) {
            if (this.isCancelled()){
                break;
            }

            latestMessage= this.client.receiveMessage();

            if (latestMessage!=null){
                //Update latestMessage value, we will listen for the event in the "UI" thread
                //"Updates are coalesced to prevent saturation of the FX event queue" - there may be better ways to update the value
                //There is a chance with heavy load that some messages are missed
                this.updateValue(latestMessage);
            }
        }

        return latestMessage;
    }
}