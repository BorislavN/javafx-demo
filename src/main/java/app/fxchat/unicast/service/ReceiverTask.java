package app.fxchat.unicast.service;

import app.fxchat.unicast.nio.ChatClient;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

//TODO: finish implementation
public class ReceiverTask extends Task<Void> {
    private final ChatClient client;
    private final StringProperty latestMessage;

    public ReceiverTask(ChatClient client, StringProperty latestMessage) {
        this.client = client;
        this.latestMessage = latestMessage;
    }

    @Override
    protected Void call() throws Exception {
//        Selector selector = this.client.getSelector();
//
//
//        while (this.client.isLive()) {
//            //The "select" is blocking to save CPU resources, if the thread is interrupted, the selector unblocks
//            selector.select();
//
//            if (this.isCancelled() || !selector.isOpen()) {
//                break;
//            }
//
//            for (SelectionKey key : selector.selectedKeys()) {
//                if (key.isValid() && key.isReadable()) {
//                    String message = this.client.receiveMessage();
//
//                    if (message != null) {
//                        Platform.runLater(() -> this.messageList.add(message));
//                    }
//                }
//            }
//        }

        return null;
    }

//    private void logMessage(String group, int port, String message) {
//        System.out.println("----------------------------------------------");
//        System.out.println(Thread.currentThread().getName() + ":");
//        System.out.printf("%s:%d - %s%n", group, port, message);
//        System.out.println("----------------------------------------------");
//    }
}