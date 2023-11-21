package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatClient;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.service.ReceiverService;
import app.fxchat.unicast.service.SenderService;
import javafx.concurrent.WorkerStateEvent;

import java.io.IOException;
import java.util.*;

//TODO: remove EventHandlers on address/port change???
public class ChatContext {
    private ChatClient client;
    private ReceiverService receiverService;
    private SenderService senderService;
    private String username;
    private Queue<String> typedMessages;
    private Map<String, List<String>> chatHistory;

    public ChatContext() {
        try {
            this.username = null;
            this.typedMessages = new ArrayDeque<>();
            this.chatHistory = new HashMap<>();

            this.client = new ChatClient();
            this.senderService = new SenderService(this.client);
            this.receiverService = new ReceiverService(this.client);

            this.senderService.setOnSucceeded((event -> {
                String newMessage = this.typedMessages.poll();

                if (newMessage != null) {
                    this.senderService.reset();
                    this.senderService.setCurrentMessage(newMessage);
                    this.senderService.restart();
                }
            }));

            this.senderService.setOnFailed((event -> {
                System.err.printf("SenderTask failed for message - \"%s\"%n", this.senderService.getCurrentMessage());
            }));

        } catch (IOException e) {
            ChatUtility.logException("ChatContext failed initialization", e);
        }
    }

    public ReceiverService getReceiverService() {
        return this.receiverService;
    }

    public SenderService getSenderService() {
        return this.senderService;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        ChatUtility.validateField("Username", username);

        this.username = username;
    }

    public Map<String, List<String>> getChatHistory() {
        return Collections.unmodifiableMap(this.chatHistory);
    }

    //The messages are sent so fast, that in majority of cases we will not need the queue
    //The thread executing the task will be free by the time we call this method again
    public void enqueueMessage(String message, boolean wrap) {
        if (wrap) {
            if (this.username == null) {
                throw new IllegalArgumentException("Username not set!");
            }

            message = this.wrapMessage(this.username, message);
        }

        if (!this.senderService.isRunning()) {
            this.senderService.setCurrentMessage(message);
            this.senderService.reset();
            this.senderService.start();

            return;
        }

        this.typedMessages.offer(message);
    }

    public void shutdown() {
        if (this.client != null) {
            this.client.shutdown();

            this.senderService.cancel();
            this.receiverService.cancel();

            this.senderService.removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, this.senderService.getOnSucceeded());
            this.senderService.removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, this.senderService.getOnFailed());
        }
    }

    private String wrapMessage(String username, String message) {
        return String.format("%s: %s", username, message);
    }
}