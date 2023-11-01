package app.fxchat.multicast;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class ReceiverTask extends Task<Void> {
    private final MulticastClient client;
    private final ObservableList<String> messageList;

    public ReceiverTask(MulticastClient client, ObservableList<String> messageList) {
        this.client = client;
        this.messageList = messageList;
    }

    @Override
    protected Void call() throws Exception {
        String tempGroup = this.client.getGroupIP();
        int tempPort = this.client.getPort();

        this.logMessage(tempGroup, tempPort, "ReceiverTask starting...");

        while (this.client.isLive()) {
            if (this.isCancelled()) {
                break;
            }

            String message = this.client.receiveMessage();

            if (message != null) {
                //This is the example from the "javafx.concurrent.Task" documentation
                //If we don't use the "runLater" method, the ListChangeListener in ChatController
                //ends-up executing outside the FX Thread
                Platform.runLater(() -> this.messageList.add(message));
            }
        }

        this.logMessage(tempGroup, tempPort, "ReceiverTask finishing...");

        return null;
    }

    private void logMessage(String group, int port, String message) {
        System.out.println("----------------------------------------------");
        System.out.println(Thread.currentThread().getName() + ":");
        System.out.printf("%s:%d - %s%n", group, port, message);
        System.out.println("----------------------------------------------");
    }
}