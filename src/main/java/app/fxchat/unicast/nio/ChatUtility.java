package app.fxchat.unicast.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.regex.Pattern;

import static app.fxchat.unicast.nio.Constants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatUtility {

    public static String readMessage(SelectionKey key) throws IOException {
        checkKey(key);

        return readMessage((SocketChannel) key.channel());
    }

    public static int writeMessage(SelectionKey key, String message) throws IOException {
        checkKey(key);

        return writeMessage((SocketChannel) key.channel(), message);
    }

    public static int writeMessage(SocketChannel channel, String message) throws IOException {
        verifyConnection(channel);

        return write(channel, message);
    }

    public static String readMessage(SocketChannel channel) throws IOException {
        verifyConnection(channel);

        return read(channel);
    }

    //Provide the field capitalized, it "looks" better when the error is displayed ;D
    public static void validateField(String field, String value) {
        int limit = "username".equalsIgnoreCase(field) ? USERNAME_LIMIT : MESSAGE_LIMIT;

        if (value.getBytes().length > limit) {
            throw new IllegalArgumentException(field + " too long!");
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " can not be blank!");
        }

        if (Pattern.matches(".*[|\"'`;]+.*", value)) {
            throw new IllegalArgumentException(field + " can't contain: |\"'`;");
        }
    }

    public static String newJoinResponse(String username) {
        return generateMessage(JOINED_FLAG, java.lang.String.format("\"%s\" joined the chat!", username), username);
    }

    public static String newChangedNameResponse(String oldName, String newName) {
        return generateMessage(CHANGED_FLAG, java.lang.String.format("\"%s\" changed their username to \"%s\"", oldName, newName), oldName, newName);
    }

    public static String newLeftResponse(String username) {
        return generateMessage(LEFT_FLAG, java.lang.String.format("\"%s\" left the chat...", username), username);
    }

    public static String newDirectMessageResponse(String origin, String message) {
        return generateMessage(FROM_FLAG, message, origin);
    }

    public static String newUsernameExceptionResponse(String message) {
        return generateMessage(USERNAME_EXCEPTION_FLAG, message);
    }

    public static String newMembersResponse(Set<String> usernames) {
        return generateMessage(MEMBERS_COMMAND, java.lang.String.join(ARRAY_DELIMITER, usernames));
    }

    public static String newPublicMessage(String message) {
        return generateMessage(PUBLIC_MESSAGE_COMMAND, message);
    }

    public static String newJoinRequest(String username) {
        return generateMessage(JOIN_COMMAND, username);
    }

    public static String newDirectMessageRequest(String destination, String message) {
        return generateMessage(TO_COMMAND, message, destination);
    }

    public static String newQuitRequest() {
        return QUIT_COMMAND;
    }

    public static String newMembersRequest() {
        return MEMBERS_COMMAND;
    }


    private static String generateMessage(String command, String data, String... arguments) {
        String args = java.lang.String.join(ARRAY_DELIMITER, arguments);

        return java.lang.String.join(COMMAND_DELIMITER, command, args, data);
    }

    private static String read(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LIMIT + USERNAME_LIMIT);
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

    private static void verifyConnection(SocketChannel channel) {
        if (channel == null) {
            throw new IllegalStateException("SocketChannel is null!");
        }

        if (!channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is not connected!");
        }
    }

    private static void checkKey(SelectionKey key) {
        if (key == null) {
            throw new IllegalStateException("SelectionKey is null!");
        }

        if (!key.isValid()) {
            throw new IllegalStateException("SelectionKey invalid!");
        }
    }
}