package app.fxchat.unicast.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static app.fxchat.unicast.nio.Constants.HOST;
import static app.fxchat.unicast.nio.Constants.PORT;

//TODO: rework client for javafx application
// can use Selector like in the multicast client or leave the channel in blocking mode
public class ChatClient {
    private final SocketChannel channel;
    private String username;

    public ChatClient() throws IOException {
        this.channel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        this.channel.configureBlocking(false);
        this.username = null;
    }

    public void shutdown() {
        try {
            this.channel.shutdownInput();
            this.channel.shutdownOutput();
            this.channel.close();

        } catch (IOException e) {
            this.printException("Channel failed to close", e);
        }
    }

    public void sendMessage(String message) {
        try {
            ChatUtility.writeMessage(this.channel, message);
        } catch (IOException e) {
            printException("Message WRITE failed", e);
        }
    }

    public String receiveMessage() {
        try {
          return  ChatUtility.readMessage(this.channel);
        } catch (IOException e) {
            printException("Message READ failed", e);
        }

        return null;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        ChatUtility.validateField("Username", username);

        this.username = username;
    }

    public static void printException(String message, Throwable err) {
        System.err.printf("%s - %s%n", message, err.getMessage());
    }

    public String wrapMessage(String message) {
        if (this.username != null) {
            return String.format("%s: %s", this.username, message);
        }

        return message;
    }
}