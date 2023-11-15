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

    public static void setUsername(SelectionKey key, String name) throws IllegalStateException, IllegalArgumentException {
        ChatUtility.validateField("Username", name);
        getAttachment(key).username = name;
    }

    public static void enqueueMessage(SelectionKey key, String message) throws IllegalStateException {
         getAttachment(key).pendingMessages.offer(message);
    }

    public static void enqueuePriorityMessage(SelectionKey key, String message) throws IllegalStateException {
         getAttachment(key).pendingMessages.offerFirst(message);
    }


    public static String dequeMessage(SelectionKey key) throws IllegalStateException {
        return getAttachment(key).pendingMessages.poll();
    }

    public static String peekMessage(SelectionKey key) throws IllegalStateException {
        return getAttachment(key).pendingMessages.peek();
    }

    public static String getUsername(SelectionKey key) throws IllegalStateException {
        return getAttachment(key).username;
    }

    public static Attachment getAttachment(SelectionKey key) throws IllegalStateException {
        if (key.attachment() == null) {
            throw new IllegalStateException("SelectionKey attachment is Null!");
        }

        return (Attachment) key.attachment();
    }
}