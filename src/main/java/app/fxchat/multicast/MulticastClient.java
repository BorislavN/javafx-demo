package app.fxchat.multicast;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MulticastClient {
    public static final int MESSAGE_LIMIT = 50;
    public static final int USERNAME_LIMIT = 20;
    private final DatagramChannel channel;
    private final NetworkInterface netI;
    private MembershipKey membership;
    private String groupIP;
    private int port;

    public MulticastClient(String interfaceName) throws IOException, IllegalArgumentException, IllegalStateException {
        this.netI = NetworkInterface.getByName(interfaceName);
        this.groupIP = "225.4.5.6";
        this.port = 6969;

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

    public void changeGroup(String address, Label errorField, TextArea area) {
        if (this.groupIP.equals(address)) {
            return;
        }

        if (this.validateIpAddress(address)) {
            try {
                InetAddress newAddress = InetAddress.getByName(address);

                this.membership.drop();

                this.membership = this.channel.join(newAddress, this.netI);
                this.groupIP = address;
                area.clear();

                return;

            } catch (IOException e) {
                errorField.setText("Failed to join group!");
                errorField.setVisible(true);

                return;
            }
        }

        errorField.setText("Invalid Group IP!");
        errorField.setVisible(true);
    }

    public String receiveMessage() {
        try {
            if (this.isLive()) {
                ByteBuffer buffer = ByteBuffer.allocate(USERNAME_LIMIT + MESSAGE_LIMIT);
                SocketAddress address = this.channel.receive(buffer);

                if (address != null) {
                    return String.format("%s%n", decodeMessage(buffer.flip()));
                }
            }
        } catch (IOException e) {
            System.err.println("Client encountered error, while receiving messages - " + e.getMessage());
        }

        return null;
    }

    public void sendMessage(String username, String message) {
        if (this.isLive()) {
            try {
                if (message.length() <= (MESSAGE_LIMIT + USERNAME_LIMIT)) {
                    this.channel.send(wrapMessage(username, message), new InetSocketAddress(this.groupIP, this.port));
                }
            } catch (IOException e) {
                System.err.println("Client failed to send message - " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        if (this.isLive()) {
            try {
                if (message.length() <= MESSAGE_LIMIT) {
                    this.channel.send(wrapMessage(message), new InetSocketAddress(this.groupIP, this.port));
                }
            } catch (IOException e) {
                System.err.println("Client failed to send message - " + e.getMessage());
            }
        }
    }

    public String getGroupIP() {
        return this.groupIP;
    }

    public void setGroupIP(String groupIP) {
        this.groupIP = groupIP;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isLive() {
        return (this.channel != null && this.channel.isOpen()) && (this.membership != null && this.membership.isValid());
    }

    public void logError(String message, Throwable err) {
        System.err.printf("%s - %s%n", message, err.getMessage());
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

    private ByteBuffer wrapMessage(String username, String message) {
        return ByteBuffer.wrap(String.format("%s: %s", username, message).getBytes(StandardCharsets.UTF_8));
    }

    private ByteBuffer wrapMessage(String message) {
        return ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
    }
}