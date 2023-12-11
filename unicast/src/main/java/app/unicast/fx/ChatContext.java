package app.unicast.fx;

import app.unicast.nio.ChatClient;
import app.unicast.nio.ChatUtility;
import app.unicast.nio.Constants;
import app.unicast.service.ReceiverService;
import app.unicast.service.SenderService;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.IOException;
import java.util.*;

public class ChatContext {
    private final ChatClient client;
    private final ReceiverService receiverService;
    private final SenderService senderService;
    private String username;
    private final Set<String> unseenMessages;
    private final Queue<String> typedMessages;
    private final Map<String, List<String>> chatHistory;
    private ChangeListener<String> messageListener;

    public ChatContext() throws IOException {
        this(Constants.HOST, Constants.PORT);
    }

    public ChatContext(String address, int port) throws IOException {
        this.username = null;
        this.typedMessages = new ArrayDeque<>();
        this.chatHistory = new HashMap<>();
        this.unseenMessages = new HashSet<>();

        this.client = new ChatClient(address, port);
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

        this.receiverService.start();
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

    public boolean hasUnseenMessages() {
        System.out.println("Size:" +this.unseenMessages);
        return !this.unseenMessages.isEmpty();
    }

    public Set<String> getUnseenMessages() {
        return Collections.unmodifiableSet(this.unseenMessages);
    }

    public void copyUnseenMessages(Set<String> unseenMessages) {
        this.unseenMessages.retainAll(unseenMessages);
    }

    public void addToUnseenMessages(String username) {
        this.unseenMessages.add(username);
    }

    public void markAsSeen(String username) {
        this.unseenMessages.remove(username);
    }

    public void setMessageListener(ChangeListener<String> listener) {
        if (this.messageListener != null) {
            this.receiverService.latestMessageProperty().removeListener(this.messageListener);
        }

        this.messageListener = listener;
        this.receiverService.latestMessageProperty().addListener(this.messageListener);
    }

    //The event handler is overwritten on each call
    public void setReceiverServiceFailHandler(EventHandler<WorkerStateEvent> handler) {
        this.receiverService.setOnFailed(handler);
    }

    public Map<String, List<String>> getChatHistory() {
        return Collections.unmodifiableMap(this.chatHistory);
    }

    public void copyHistory(Map<String, List<String>> history) {
        for (String key : history.keySet()) {
            this.chatHistory.put(key, history.get(key));
        }
    }

    public void removePrivateMessages(String username) {
        this.chatHistory.remove(username);
    }

    //The messages are sent so fast, that in majority of cases we will not need the queue
    //The thread executing the task will be free by the time we call this method again
    public void enqueueMessage(String message) {
        if (!this.senderService.isRunning()) {
            this.senderService.setCurrentMessage(message);
            this.senderService.reset();
            this.senderService.restart();

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
        String[] data = this.extractMessageData(message, "\\|");

        return data[data.length - 1];
    }

    public boolean isInitialized() {
        return this.client != null && this.senderService != null && this.receiverService != null;
    }

    public boolean isMessageQueueEmpty() {
        return this.typedMessages.isEmpty();
    }

    public boolean isClientLive() {
        return this.client != null && this.client.isLive();
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

    public static boolean isNotNull(ChatContext context) {
        return context != null && context.isInitialized();
    }
}