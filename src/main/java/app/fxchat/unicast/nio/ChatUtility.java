package app.fxchat.unicast.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatUtility {
    public static final String HOST = "localhost";
    public static final int PORT = 8080;
    public static final int MESSAGE_LIMIT = 300;
    public static final int USERNAME_LIMIT = 30;

    public static String readMessage(SelectionKey key) throws IOException, IllegalStateException {
        checkIfKeyIsValid(key);

        return readMessage((SocketChannel) key.channel());
    }

    public static int writeMessage(SelectionKey key, String message) throws IOException, IllegalStateException {
        checkIfKeyIsValid(key);

        return writeMessage((SocketChannel) key.channel(), message);
    }

    public static int writeMessage(SocketChannel channel, String message) throws IOException, IllegalStateException {
        checkIfSocketIsConnected(channel);

        return write(channel, message);
    }

    public static String readMessage(SocketChannel channel) throws IOException, IllegalStateException {
        checkIfSocketIsConnected(channel);

        return read(channel);
    }

    private static String read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LIMIT);
        StringBuilder output = new StringBuilder();

        int bytesRead = channel.read(buffer);

        while (bytesRead > 0) {
            output.append(decodeBuffer(buffer.flip()));
            bytesRead = channel.read(buffer.clear());
        }

        //Check if connection was closed
        if (bytesRead == -1) {
            channel.close();
        }

        return output.toString();
    }

    private static int write(SocketChannel channel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(UTF_8));

        int bytesWritten = channel.write(buffer);
        int totalBytes = bytesWritten;

        while (bytesWritten > 0 && buffer.hasRemaining()) {
            bytesWritten = channel.write(buffer);
            totalBytes += bytesWritten;
        }

        //Check if connection was closed
        if (totalBytes == -1) {
            channel.close();
        }

        return totalBytes;
    }

    private static String decodeBuffer(ByteBuffer buffer) {
        return UTF_8.decode(buffer).toString();
    }

    private static void checkIfSocketIsConnected(SocketChannel channel) throws IllegalStateException {
        if (!channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is not connected!");
        }
    }

    private static void checkIfKeyIsValid(SelectionKey key) throws IllegalStateException {
        if (!key.isValid()) {
            throw new IllegalStateException("SelectionKey invalid!");
        }
    }

    public static String validateUsername(String name) throws IllegalArgumentException {
        if (name.getBytes().length > USERNAME_LIMIT) {
            throw new IllegalArgumentException("Username too long!");
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException("Username can not be blank!");
        }

        return name;
    }

    public static String joinMessage(String message) {
        if (message.startsWith("/user")) {
            return substringMessage(message, 6) + " joined the chat!";
        }

        return message + " joined the chat!";
    }

    public static String prependUsername(SelectionKey key, String message) {
        return String.format("%s: %s", ConnectionAttachment.getUsername(key), message);
    }

    public static String leftMessage(SelectionKey key, Set<String> takenNames) {
        String name = ConnectionAttachment.getUsername(key);
        //Remove username
        takenNames.remove(name);

        return name + " left the chat...";
    }

    public static String substringMessage(String message, int start) throws IllegalArgumentException {
        try {
            return message.substring(start);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid username!");
        }
    }
}