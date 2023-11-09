package app.fxchat.unicast.nio;

import java.nio.channels.SelectionKey;
import java.util.ArrayDeque;
import java.util.Queue;

public class ConnectionAttachment {
    private String username;
    private final Queue<String> pendingMessages;

    public ConnectionAttachment() {
        this.username = null;
        this.pendingMessages = new ArrayDeque<>();
    }

    public boolean enqueueMessage(String message) {
        return this.pendingMessages.offer(message);
    }

    public String getName() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String pollMessage() {
        return this.pendingMessages.poll();
    }

    public String peekMessage() {
        return this.pendingMessages.peek();
    }

    public static void setUsername(SelectionKey key, String name) throws IllegalStateException,IllegalArgumentException {
        checkIfAttachmentIsValid(key);

        ((ConnectionAttachment) key.attachment()).setUsername(ChatUtility.validateUsername(name));
    }

    public static boolean enqueueMessage(SelectionKey key, String message) throws IllegalStateException {
        checkIfAttachmentIsValid(key);
        ConnectionAttachment attachment = (ConnectionAttachment) key.attachment();

        return attachment.enqueueMessage(message);
    }

    public static String pollMessage(SelectionKey key) throws IllegalStateException {
        checkIfAttachmentIsValid(key);
        ConnectionAttachment attachment = (ConnectionAttachment) key.attachment();

        return attachment.pollMessage();

    }

    public static String peekMessage(SelectionKey key) throws IllegalStateException {
        checkIfAttachmentIsValid(key);
        ConnectionAttachment attachment = (ConnectionAttachment) key.attachment();

        return attachment.peekMessage();
    }

    public static String getUsername(SelectionKey key) throws IllegalStateException {
        checkIfAttachmentIsValid(key);

        String name = ((ConnectionAttachment) key.attachment()).getName();

        return name == null ? "Anonymous" : name;
    }

    private static void checkIfAttachmentIsValid(SelectionKey key) {
        if (key.attachment() == null) {
            throw new IllegalStateException("SelectionKey attachment is Null!");
        }
    }
}