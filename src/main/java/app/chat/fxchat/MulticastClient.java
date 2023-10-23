package app.chat.fxchat;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.charset.StandardCharsets;

//TODO: Use the class in the javafx implementation
//I'm not sure if the "receiveMessage()" should be called in a new thread
//To avoid blocking, while the method itself is non-blocking, it should be called in a while-loop....
public class MulticastClient {
    private static final int MESSAGE_LIMIT = 50;
    private static final int USERNAME_LIMIT = 20;
    private static final String GROUP_IP = "225.4.5.6";
    private static final int PORT = 6969;
    private final DatagramChannel channel;
    private final MembershipKey membership;

    public MulticastClient(String interfaceName) throws IOException, IllegalArgumentException, IllegalStateException {
        NetworkInterface netI = NetworkInterface.getByName(interfaceName);

        this.channel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(PORT))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, netI);

        this.channel.configureBlocking(false);

        InetAddress group = InetAddress.getByName(GROUP_IP);

        this.membership = this.channel.join(group, netI);
    }

    public void closeChannel() {
        try {
            if (this.membership != null) {
                this.membership.drop();
            }

            if (this.channel != null) {
                this.channel.disconnect();
                this.channel.close();
            }
        } catch (IOException e) {
            this.logError("Exception occurred while closing the client", e);
        }
    }

    public void listenForMessages() {
        if (this.isLive()) {
            Thread worker = new Thread(() -> {
                while (this.isLive()) {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(USERNAME_LIMIT + MESSAGE_LIMIT);

                        SocketAddress address = this.channel.receive(buffer);

                        if (address != null) {
                            //TODO: need reference to TextArea
                            decodeMessage(buffer.flip());
                        }

                    } catch (IOException e) {
                        //TODO: shiiiiiiiiiiiit :D, handle
                    }
                }
            });

            worker.start();
        }
    }

    public void sendMessage(String username, String message) throws IOException {
        if (this.isLive()) {

            if (this.validateMessage(message)) {
                this.channel.send(wrapMessage(username, message), new InetSocketAddress(GROUP_IP, PORT));
            }
        }
    }

    public boolean isLive() {
        return (this.channel != null && this.channel.isOpen()) && (this.membership != null && this.membership.isValid());
    }

    public void logError(String message, Throwable err) {
        System.err.printf("%s - %s%n", message, err.getMessage());
    }

    public boolean validateUsername(String name) {
        return name.length() <= USERNAME_LIMIT;
    }

    public boolean validateMessage(String message) {
        return message.length() <= MESSAGE_LIMIT;
    }

    private String decodeMessage(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private ByteBuffer wrapMessage(String username, String message) {
        return ByteBuffer.wrap(String.format("%s: %s", username, message).getBytes(StandardCharsets.UTF_8));
    }
}