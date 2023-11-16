package app.fxchat.unicast.nio;

import java.nio.channels.SelectionKey;
import java.util.ArrayDeque;

public class Attachment {
    private String username;
    private final ArrayDeque<String> pendingMessages;

    public Attachment() {
        this.username = null;
        this.pendingMessages = new ArrayDeque<>();
    }

    public static void setUsername(SelectionKey key, String name) {
        ChatUtility.validateField("Username", name);
        getAttachment(key).username = name;
    }

    public static void enqueueMessage(SelectionKey key, String message) {
        getAttachment(key).pendingMessages.offer(message);
    }

    public static void enqueuePriorityMessage(SelectionKey key, String message) {
        getAttachment(key).pendingMessages.offerFirst(message);
    }

    public static String dequeMessage(SelectionKey key) {
        return getAttachment(key).pendingMessages.poll();
    }

    public static String peekMessage(SelectionKey key) {
        return getAttachment(key).pendingMessages.peek();
    }

    public static String getUsername(SelectionKey key) {
        return getAttachment(key).username;
    }

    public static Attachment getAttachment(SelectionKey key) {
        if (key.attachment() == null) {
            throw new IllegalStateException("SelectionKey attachment is Null!");
        }

        return (Attachment) key.attachment();
    }
}