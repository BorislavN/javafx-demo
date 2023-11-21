package app.fxchat.unicast.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static app.fxchat.unicast.nio.Constants.HOST;
import static app.fxchat.unicast.nio.Constants.PORT;

public class ChatClient {
    private final SocketChannel channel;
    private final Selector selector;

    public ChatClient() throws IOException {
        this.channel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(this.selector, SelectionKey.OP_READ);
    }

    public void shutdown() {
        try {
            this.selector.close();

            this.channel.shutdownInput();
            this.channel.shutdownOutput();
            this.channel.close();

        } catch (IOException e) {
            ChatUtility.logException("Channel failed to close", e);
        }
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