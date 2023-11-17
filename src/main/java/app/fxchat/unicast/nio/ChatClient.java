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
        this.username=null;
    }

    private void shutdown() {
        try {
            this.channel.shutdownInput();
            this.channel.shutdownOutput();
            this.channel.close();
        } catch (IOException e) {
            System.err.println("Encountered exception while trying to close client - " + e.getMessage());
        }
    }

    public void sendMessage(String message) throws IOException {
        ChatUtility.writeMessage(this.channel,message);
    }

    public void receiveMessage(String message) {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
