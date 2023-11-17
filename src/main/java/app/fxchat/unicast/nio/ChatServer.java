package app.fxchat.unicast.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import static app.fxchat.unicast.nio.Constants.HOST;
import static app.fxchat.unicast.nio.Constants.PORT;
import static java.nio.channels.SelectionKey.*;

public class ChatServer implements Runnable {
    private final ServerSocketChannel server;
    private final Selector mainSelector;
    private final Selector writeSelector;
    private boolean receivedAConnection;

    public ChatServer() throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.bind(new InetSocketAddress(HOST, PORT));
        this.mainSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.receivedAConnection = false;

        this.server.configureBlocking(false);
        this.server.register(mainSelector, OP_ACCEPT);
    }

    @Override
    public void run() {
        while (this.hasConnections()) {
            try {
                //Check mainSelector for events
                this.checkSelectorForEvents(this.mainSelector, "read");
                //Check writeSelector for events
                this.checkSelectorForEvents(this.writeSelector, "write");

            } catch (IOException e) {
                this.logError("Server encountered an Exception", e);
            }
        }

        this.shutdown();
    }

    private void checkSelectorForEvents(Selector selector, String type) throws IOException {
        selector.select();

        for (SelectionKey key : selector.selectedKeys()) {
            try {
                if ("read".equals(type)) {
                    this.handleConnection(key);
                    this.handleIncomingData(key);
                }

                if ("write".equals(type)) {
                    this.handlePendingMessages(key);
                }

            } catch (IllegalArgumentException e) {
                Attachment.enqueuePriorityMessage(key, e.getMessage());
                this.registerInWriteSelector(key);

                this.logError("IllegalArgumentException caught", e);

            } catch (SocketException | IllegalStateException e) {
                this.removeConnection(key);

                this.logError("Removing connection", e);
            }
        }
    }

    private void handleConnection(SelectionKey key) throws IOException {
        if (key.isValid() && key.isAcceptable()) {
            SocketChannel connection = this.server.accept();

            if (connection != null) {
                connection.configureBlocking(false);

                connection.register(this.mainSelector, OP_READ, new Attachment());

                this.receivedAConnection = true;
            }
        }
    }

    private void handleIncomingData(SelectionKey key) throws IOException {
        if (key.isValid() && key.isReadable()) {
            String message = ChatUtility.readMessage(key);

            String[] data = message.split("\\|");

            this.log(message);

            switch (data[0]) {
                case "#join" -> this.handleJoinCommand(key, data);

                case "#members" -> this.handleMembersCommand(key);

                case "#to" -> this.handleToCommand(key, data);

                case "#public" -> this.handlePublicCommand(key, data);

                case "#quit", default -> this.handleQuitCommand(key);
            }
        }
    }

    private void handleJoinCommand(SelectionKey key, String[] data) throws IOException {
        String proposedName = data[1];
        String currentName = Attachment.getUsername(key);

        Set<String> usernames = this.getTakenUsernames();

        if (usernames.contains(proposedName) && !currentName.equals(proposedName)) {
            throw new IllegalArgumentException(ChatUtility.newUsernameExceptionResponse("Username is already taken!"));
        }

        if (!usernames.contains(proposedName)) {
            Attachment.setUsername(key, proposedName);

            String message = currentName == null
                    ? ChatUtility.newJoinResponse(proposedName)
                    : ChatUtility.newChangedNameResponse(currentName, proposedName);

            this.enqueueInAll(message, null);
        }
    }

    private void handleMembersCommand(SelectionKey key) throws IOException {
        Attachment.enqueuePriorityMessage(key, ChatUtility.newMembersResponse(this.getTakenUsernames()));
        this.registerInWriteSelector(key);
    }

    private void handleToCommand(SelectionKey key, String[] data) {
        String origin = Attachment.getUsername(key);
        String destination = data[1];

        this.getAllConnections().stream()
                .filter(k -> destination.equals(Attachment.getUsername(k)))
                .forEach(k -> Attachment.enqueueMessage(k, ChatUtility.newDirectMessageResponse(origin, data[2])));
    }

    private void handlePublicCommand(SelectionKey origin, String[] data) throws IOException {
        this.enqueueInAll(data[2], origin);
    }

    private void handleQuitCommand(SelectionKey key) throws IOException {
        this.removeConnection(key);
        this.enqueueInAll(ChatUtility.newLeftResponse(Attachment.getUsername(key)), key);
    }

    private void removeConnection(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
    }

    //If origin is null, the message will be enqueued in all channels
    //If origin is specified  - it will be excluded
    private void enqueueInAll(String message, SelectionKey origin) throws IOException {
        for (SelectionKey connection : this.getAllConnections()) {
            if (connection != origin) {
                Attachment.enqueueMessage(connection, message);
                this.registerInWriteSelector(connection);
            }
        }
    }

    //Register the channel for writing (if already registered - the interest set is reset)
    private void registerInWriteSelector(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.register(this.writeSelector, OP_WRITE, key.attachment());
    }

    private void handlePendingMessages(SelectionKey key) throws IOException {
        if (key.isValid() && key.isWritable()) {
            String message = Attachment.peekMessage(key);

            //Unregister if queue is empty
            if (message == null) {
                key.cancel();
                return;
            }

            int bytesWritten = ChatUtility.writeMessage(key, message);

            //If write succeeded poll the message
            if (bytesWritten != 0) {
                Attachment.dequeMessage(key);
            }
        }
    }

    public void shutdown() {
        this.receivedAConnection = true;

        try {
            this.mainSelector.close();
            this.writeSelector.close();
            this.server.close();

        } catch (IOException e) {
            this.logError("Exception happened while server shutdown", e);
        }
    }

    private boolean hasConnections() {
        return this.getConnectionCount() > 0 || !this.receivedAConnection;
    }

    private long getConnectionCount() {
        return this.mainSelector.keys().stream()
                .filter(key -> key.isValid() && key.attachment() != null)
                .count();
    }

    private Set<SelectionKey> getAllConnections() {
        return this.mainSelector.keys().stream()
                .filter(key -> key.isValid() && Attachment.getUsername(key) != null)
                .collect(Collectors.toSet());
    }

    private Set<String> getTakenUsernames() {
        return this.getAllConnections().stream()
                .map(Attachment::getUsername)
                .collect(Collectors.toSet());
    }

    private void log(String message) {
        System.out.printf("[%1$tH:%1$tM] Server log - \"%2$s\"%n", LocalTime.now(), message);
    }

    private void logError(String message, Throwable error) {
        System.err.printf("[%1$tH:%1$tM] Server log - \"%2$s - %3$s\"%n", LocalTime.now(), message, error.getMessage());
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.run();
    }
}