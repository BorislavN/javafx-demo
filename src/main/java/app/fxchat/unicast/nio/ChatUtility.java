package app.fxchat.unicast.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatUtility {
    public static final String HOST = "localhost";
    public static final int PORT = 6009;
    public static final int MESSAGE_LIMIT = 300;
    public static final int USERNAME_LIMIT = 30;

    public static final String TO_COMMAND = "#to";
    public static final String JOIN_COMMAND = "#join";
    public static final String QUIT_COMMAND = "#quit";
    public static final String MEMBERS_COMMAND = "#members";
    public static final String PUBLIC_MESSAGE_COMMAND = "#public";

    public static final String FROM_FLAG = "#from";
    public static final String JOINED_FLAG = "#joined";
    public static final String CHANGED_FLAG = "#changed";
    public static final String LEFT_FLAG = "#left";
    public static final String USERNAME_EXCEPTION_FLAG = "#usernameException";

    public static String readMessage(SelectionKey key) throws IOException {
        checkKey(key);

        return read(verifyConnection((SocketChannel) key.channel()));
    }

    public static int writeMessage(SelectionKey key, String message) throws IOException {
        checkKey(key);

        return write(verifyConnection((SocketChannel) key.channel()), message);
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

    public static String joinedMessage(String username) {
        return String.format("\"%s\" joined the chat!", username);
    }

    public static String changedMessage(String oldName, String newName) {
        return String.format("\"%s\" changed their username to \"%s\"", oldName, newName);
    }

    public static String leftMessage(String username) {
        return String.format("\"%s\" left the chat...", username);
    }

    public static String newFromMessage(SelectionKey origin, String message) {
        return java.lang.String.format("%s|%s|%s", FROM_FLAG, Attachment.getUsername(origin), message);
    }

    public static String newUsernameExceptionMessage(String message) {
        return String.format("%s|%S", USERNAME_EXCEPTION_FLAG, message);
    }

    public static String generateMessage(String command, String data, String... arguments) {
        String args = String.join(":", arguments);

        return String.join("|", command, args, data);
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

    private static SocketChannel verifyConnection(SocketChannel channel) {
        if (channel == null) {
            throw new IllegalStateException("SocketChannel is null!");
        }

        if (!channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is not connected!");
        }

        return channel;
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