package app.fxchat.unicast.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.channels.SelectionKey.*;

public class ChatServer implements Runnable {
    private final ServerSocketChannel server;
    private final Selector mainSelector;
    private boolean receivedAConnection;

    public ChatServer(String host, int port) throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.bind(new InetSocketAddress(host, port));
        this.mainSelector = Selector.open();
        this.receivedAConnection = false;

        this.server.configureBlocking(false);
        this.server.register(mainSelector, OP_ACCEPT);

        this.log(String.format("Started on: %s", this.server.getLocalAddress()));
    }

    public ChatServer() throws IOException {
        this(Constants.HOST, Constants.PORT);
    }

    @Override
    public void run() {
        while (this.hasConnections()) {
            try {
                Iterator<SelectionKey> iterator = this.getReadySet(this.mainSelector);

                while (iterator != null && iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    try {
                        this.handleConnection(key);

                        this.handleIncomingData(key);

                        this.handlePendingMessages(key);

                    } catch (IllegalArgumentException e) {
                        Attachment.enqueuePriorityMessage(key, e.getMessage());
                        this.registerInWriteSelector(key);

                        this.logError("IllegalArgumentException caught", e);

                    } catch (SocketException | IllegalStateException e) {
                        this.handleQuitCommand(key);

                        this.logError("Removing connection", e);
                    }

                    iterator.remove();
                }

            } catch (IOException e) {
                this.logError("Server encountered an Exception", e);
            }
        }

        this.shutdown();
    }

    private Iterator<SelectionKey> getReadySet(Selector selector) throws IOException {
        int readyCount = selector.select();

        if (readyCount > 0) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            return selectedKeys.iterator();
        }

        return null;
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

                default -> this.handleQuitCommand(key);
            }
        }
    }

    private void handleJoinCommand(SelectionKey key, String[] data) throws IOException {
        String proposedName = data[1];
        String currentName = Attachment.getUsername(key);

        Set<String> usernames = this.getTakenUsernames();

        if (usernames.contains(proposedName) && !proposedName.equals(currentName)) {
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

    private void handleToCommand(SelectionKey key, String[] data) throws IOException {
        String origin = Attachment.getUsername(key);
        String destination = data[1];

        for (SelectionKey connection : this.getAllConnections()) {
            if (destination.equals(Attachment.getUsername(connection))) {
                Attachment.enqueueMessage(connection, ChatUtility.newDirectMessageResponse(origin, data[2]));
                this.registerInWriteSelector(connection);

                break;
            }
        }
    }

    private void handlePublicCommand(SelectionKey origin, String[] data) throws IOException {
        this.enqueueInAll(ChatUtility.newPublicMessage(data[1]), origin);
    }

    private void handleQuitCommand(SelectionKey key) throws IOException {
        this.removeConnection(key);

        String username = Attachment.getUsername(key);

        if (username != null) {
            this.enqueueInAll(ChatUtility.newLeftResponse(Attachment.getUsername(key)), key);
        }
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
        channel.register(this.mainSelector, OP_READ | OP_WRITE, key.attachment());
    }

    private void handlePendingMessages(SelectionKey key) throws IOException {
        if (key.isValid() && key.isWritable()) {
            String message = Attachment.peekMessage(key);

            //Unregister if queue is empty
            if (message == null) {
                key.channel().register(this.mainSelector, OP_READ, key.attachment());
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
                .filter(key -> key.isValid() && key.attachment() != null)
                .collect(Collectors.toSet());
    }

    private Set<String> getTakenUsernames() {
        return this.getAllConnections().stream()
                .map(Attachment::getUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void log(String message) {
        if (message.isBlank()) {
            this.logError("Empty String", new Throwable("Connection terminated unexpectedly!"));

            return;
        }

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