package app.fxchat.multicast.client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MulticastClient {
    public static final int MESSAGE_LIMIT = 175;
    public static final int USERNAME_LIMIT = 25;
    private final NetworkInterface netI;
    private DatagramChannel channel;
    private MembershipKey membership;
    private Selector selector;
    private String groupIP;
    private int port;

    public MulticastClient(String interfaceName) throws IOException, IllegalArgumentException, IllegalStateException {
        this.netI = NetworkInterface.getByName(interfaceName);
        this.groupIP = "239.4.5.6";
        this.port = 6969;

        this.initializeChannel();
    }

    private void initializeChannel() throws IOException, IllegalArgumentException, IllegalStateException {
        this.channel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(this.port))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, this.netI);

        this.channel.configureBlocking(false);

        this.selector = Selector.open();
        this.channel.register(this.selector, SelectionKey.OP_READ);

        InetAddress group = InetAddress.getByName(this.groupIP);

        this.membership = this.channel.join(group, this.netI);
    }

    public void closeChannel() {
        try {
            if (this.membership != null) {
                this.membership.drop();
            }

            if (this.selector != null) {
                this.selector.close();
            }

            if (this.channel != null) {
                this.channel.close();
            }
        } catch (IOException e) {
            this.logError("Exception occurred while closing the client", e);
        }
    }

    //The method returns "true" if the change was successful
    public boolean changeGroup(String address) throws IllegalArgumentException {
        if (!this.validateIpAddress(address)) {
            throw new IllegalArgumentException("Invalid group IP!");
        }

        if (this.groupIP.equals(address)) {
            throw new IllegalArgumentException("Target IP matches current IP!");
        }

        try {
            this.membership.drop();

            InetAddress newAddress = InetAddress.getByName(address);
            this.membership = this.channel.join(newAddress, this.netI);
            this.groupIP = address;

            return true;

        } catch (IOException e) {
            this.logError("Failed to change the group", e);

            return false;
        }
    }

    public boolean changePort(int port) throws IllegalArgumentException {
        if (!this.validatePort(String.valueOf(port))) {
            throw new IllegalArgumentException("Invalid port!");
        }

        if (this.port == port) {
            throw new IllegalArgumentException("Target port matches current port!");
        }

        try {
            this.closeChannel();

            this.port = port;
            this.initializeChannel();

            return true;

        } catch (IOException e) {
            this.logError("Failed to change the port", e);

            return false;
        }
    }

    public String receiveMessage() {
        if (this.isLive()) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(USERNAME_LIMIT + MESSAGE_LIMIT);
                SocketAddress address = this.channel.receive(buffer);

                if (address != null) {
                    return String.format("%s%n", decodeMessage(buffer.flip()));
                }
            } catch (IOException e) {
                this.logError("Client encountered error, while receiving messages", e);
            }
        }

        return null;
    }

    public void sendMessage(String message) {
        if (this.isLive()) {
            try {
                if (message.length() <= (MESSAGE_LIMIT + USERNAME_LIMIT)) {
                    this.channel.send(wrapMessage(message), new InetSocketAddress(this.groupIP, this.port));
                }
            } catch (IOException e) {
                this.logError("Client failed to send message", e);
            }
        }
    }

    public Selector getSelector() {
        return this.selector;
    }

    public String getGroupIP() {
        return this.groupIP;
    }

    public int getPort() {
        return this.port;
    }

    public boolean isLive() {
        return (this.channel != null && this.channel.isOpen()) && (this.membership != null && this.membership.isValid());
    }

    public void logError(String message, Throwable err) {
        System.err.printf("%s - %s%n", message, err.getMessage());
    }

    public boolean validatePort(String portValue) {
        try {
            int port = Integer.parseInt(portValue);

            if (port >= 1024 && port <= 65353) {
                return true;
            }
        } catch (NumberFormatException e) {
            //Ignored - will return false
        }

        return false;
    }

    public boolean validateIpAddress(String address) {
        boolean matches = Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$", address);

        if (matches) {
            //"239.0.0.0 to 239.255.255.255: These are meant for local use and are not to be routed outside the local network"
            //We can use the public multicast range too, since the default TTL is 1 - the packets won't leave the LAN
            //Receiving messages depends on firewall settings, router settings, ISP configuration... ,
            //so in most cases we won't be getting any "foreign" messages
            //Tried out "224.0.19.19" with devices on different networks, no messages were received
            if (!address.startsWith("239.")) {
                return false;
            }

            return this.isMulticast(address);
        }

        return false;
    }

    private boolean isMulticast(String address) {
        try {
            return InetAddress.getByName(address).isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private String decodeMessage(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private ByteBuffer wrapMessage(String message) {
        return ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
    }
}