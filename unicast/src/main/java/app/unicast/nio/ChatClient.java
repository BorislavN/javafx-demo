package app.unicast.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ChatClient {
    private final SocketChannel channel;
    private final Selector selector;
    private final String address;
    private final int port;


    public ChatClient(String address, int port) throws IOException {
        this.address = address;
        this.port = port;

        this.channel = SocketChannel.open(new InetSocketAddress(this.address, this.port));
        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(this.selector, SelectionKey.OP_READ);
    }

    public void shutdown() {
        try {
            this.selector.close();
            this.channel.close();

        } catch (IOException e) {
            ChatUtility.logException("Channel failed to close", e);
        }
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public void sendMessage(String message) throws IOException {
        ChatUtility.writeMessage(this.channel, message);
    }

    public String receiveMessage() throws IOException {
        return ChatUtility.readMessage(this.channel);
    }

    public boolean isLive() {
        return this.channel.isOpen() && this.channel.isConnected() && this.selector.isOpen();
    }
}