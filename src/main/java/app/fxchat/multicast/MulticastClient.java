package app.fxchat.multicast;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

//When the channel is in non-blocking mode it returns null if there is no data
//creating more CPU load, because of the fast-spinning while, than in blocking mode
//But we cannot cancel the application :D and a bunch of bugs occur
//TODO: try to find a workaround to use the blocking mode and reduce cpu load
//TODO: maybe I can use a Selector, to indicate when there is data to read,
// the selector requires non-blocking mode - maybe we can dodge the bugs :D
public class MulticastClient {
    public static final int MESSAGE_LIMIT = 50;
    public static final int USERNAME_LIMIT = 20;
    private final NetworkInterface netI;
    private DatagramChannel channel;
    private MembershipKey membership;
    private String groupIP;
    private int port;

    public MulticastClient(String interfaceName) throws IOException, IllegalArgumentException, IllegalStateException {
        this.netI = NetworkInterface.getByName(interfaceName);
        this.groupIP = "225.4.5.6";
        this.port = 6969;

        this.initializeChannel();
    }

    private void initializeChannel() throws IOException, IllegalArgumentException, IllegalStateException {
        this.channel = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(this.port))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, this.netI);


        this.channel.configureBlocking(false);

        InetAddress group = InetAddress.getByName(this.groupIP);

        this.membership = this.channel.join(group, this.netI);
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

    //The method returns "true" if the change was successful
    public boolean changeGroup(String address) throws IllegalArgumentException {
        if (!this.validateIpAddress(address)) {
            throw new IllegalArgumentException("Invalid group IP!");
        }

        if (this.groupIP.equals(address)) {
            throw new IllegalArgumentException("Target IP matches current IP!");
        }

        try {
            InetAddress newAddress = InetAddress.getByName(address);
            this.membership.drop();

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
            //"The range of addresses between 224.0.0.0 and 224.0.0.255, inclusive,is reserved"
            if (address.startsWith("224.0.0.")) {
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