package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatClient;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.service.ReceiverService;
import app.fxchat.unicast.service.SenderService;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.WorkerStateEvent;

import java.io.IOException;
import java.util.*;

public class ChatContext {
    private ChatClient client;
    private ReceiverService receiverService;
    private SenderService senderService;
    private String username;
    private Queue<String> typedMessages;
    private Map<String, List<String>> chatHistory;
    private ChangeListener<String> messageListener;

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

    public ChatClient getClient() {
        return this.client;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        ChatUtility.validateField("Username", username);

        this.username = username;
    }

    public void setMessageListener(ChangeListener<String> listener) {
        if (this.messageListener != null) {
            this.receiverService.latestMessageProperty().removeListener(this.messageListener);
        }

        this.messageListener = listener;
        this.receiverService.latestMessageProperty().addListener(this.messageListener);
    }

    public Map<String, List<String>> getChatHistory() {
        return Collections.unmodifiableMap(this.chatHistory);
    }

    //The messages are sent so fast, that in majority of cases we will not need the queue
    //The thread executing the task will be free by the time we call this method again
    public void enqueueMessage(String message) {
        if (!this.senderService.isRunning()) {
            this.senderService.setCurrentMessage(message);
            this.senderService.reset();
            this.senderService.start();

            return;
        }

        this.typedMessages.offer(message);
    }

    public void addToHistory(String key, String value) {
        this.chatHistory.putIfAbsent(key, new ArrayList<>());
        this.chatHistory.get(key).add(value);
    }

    public String wrapMessage(String message) {
        if (this.username == null) {
            throw new IllegalArgumentException("Username not set!");
        }

        return String.format("%s: %s", username, message);
    }

    public String[] extractMessageData(String message, String delimiter) {
        return message.split(delimiter);
    }

    public String extractUserMessage(String message) {
        String[] data = this.extractMessageData(message,"\\|");

        return data[data.length - 1];
    }

    public void shutdown() {
        if (this.client != null) {
            this.client.shutdown();

            this.senderService.cancel();
            this.receiverService.cancel();

            this.senderService.removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, this.senderService.getOnSucceeded());
            this.senderService.removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, this.senderService.getOnFailed());

            if (this.messageListener != null) {
                this.receiverService.latestMessageProperty().removeListener(this.messageListener);
            }
        }
    }
}