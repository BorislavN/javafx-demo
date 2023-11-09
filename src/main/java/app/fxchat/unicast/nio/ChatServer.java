package app.fxchat.unicast.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.channels.SelectionKey.*;

//TODO: rework server, add direct message functionality
public class ChatServer implements Runnable {
    private final ServerSocketChannel server;
    private final Selector mainSelector;
    private final Selector writeSelector;
    private final Set<String> takenUsernames;
    private boolean receivedAConnection;

    public ChatServer() throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.bind(new InetSocketAddress(ChatUtility.HOST, ChatUtility.PORT));
        this.mainSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.takenUsernames = new HashSet<>();
        this.receivedAConnection = false;

        this.server.configureBlocking(false);
        this.server.register(mainSelector, OP_ACCEPT);
    }

    @Override
    public void run() {
        while (this.hasConnections()) {
            try {
                //Check mainSelector for events
                checkSelectorForEvents(this.mainSelector, "read");
                //Check writeSelector for events
                checkSelectorForEvents(this.writeSelector, "write");

            } catch (IOException e) {
                this.logError("Server encountered an Exception", e);
            }
        }

        this.shutdown();
    }

    private void checkSelectorForEvents(Selector selector, String type) throws IOException {
        Iterator<SelectionKey> iterator = this.getReadySet(selector);

        while (iterator != null && iterator.hasNext()) {
            SelectionKey key = iterator.next();

            try {
                if ("read".equals(type)) {
                    this.handleConnection(key);
                    this.handleIncomingData(key);
                }

                if ("write".equals(type)) {
                    this.handlePendingMessages(key);
                }
            } catch (IllegalArgumentException e) {
                ChatUtility.writeMessage(key, e.getMessage());
            } catch (SocketException | IllegalStateException e) {
                this.removeConnection(key);
            }

            iterator.remove();
        }
    }

    private void handleConnection(SelectionKey key) throws IOException, IllegalStateException {
        if (key.isValid() && key.isAcceptable()) {
            SocketChannel connection = this.server.accept();

            if (connection != null) {
                connection.configureBlocking(false);
                this.receivedAConnection = true;

                connection.register(this.mainSelector, OP_READ, new ConnectionAttachment());

                ChatUtility.writeMessage(connection, "Welcome! Please choose a username.");
            }
        }
    }

    private void handleIncomingData(SelectionKey key) throws IOException, IllegalArgumentException, IllegalStateException {
        if (key.isValid() && key.isReadable()) {
            String message = ChatUtility.readMessage(key);

            if (this.checkForQuitCommand(key, message)) {
                return;
            }

            message = this.checkForSetUsernameCommand(key, message);

            if (message != null) {
                this.log(message);
                this.enqueueMessage(message);
            }
        }
    }

    //Add the message to the pending lists, and register the channels in the writeSelector
    private void enqueueMessage(String message) throws IOException {
        for (SelectionKey connection : this.getAllConnections()) {
            if (connection.isValid()) {
                SocketChannel channel = (SocketChannel) connection.channel();
                channel.register(this.writeSelector, OP_WRITE, connection.attachment());

                ConnectionAttachment.enqueueMessage(connection, message);
            }
        }
    }


    private void handlePendingMessages(SelectionKey key) throws IOException, IllegalStateException {
        if (key.isValid() && key.isWritable()) {
            String message = ConnectionAttachment.peekMessage(key);

            //Unregister if queue is empty
            if (message == null) {
                key.cancel();
                return;
            }

            int bytesWritten = ChatUtility.writeMessage(key, message);

            //If write succeeded poll the message
            if (bytesWritten != 0) {
                ConnectionAttachment.pollMessage(key);
            }
        }
    }

    private boolean checkForQuitCommand(SelectionKey key, String message) throws IOException {
        //Because the client does not permit sending empty messages, the only way we
        //received an empty string is - if the client was terminated abruptly
        //We want this termination to be logged, so we add the "isEmpty" clause
        if (message != null && (message.startsWith("/quit") || message.isEmpty())) {
            this.removeConnection(key);
            return true;
        }

        return false;
    }

    private void removeConnection(SelectionKey key) throws IOException {
        String message = ChatUtility.leftMessage(key, this.takenUsernames);

        key.cancel();
        key.channel().close();

        this.log(message);
        this.enqueueMessage(message);
    }

    private String checkForSetUsernameCommand(SelectionKey key, String message) throws IOException, IllegalStateException, IllegalArgumentException {
        if (message != null) {

            //If the message starts with "/user"
            if (message.startsWith("/user")) {
                message = ChatUtility.substringMessage(message, 6);
                String currentName = ConnectionAttachment.getUsername(key);

                //If current name and new name match
                if (message.equals(currentName)) {
                    ChatUtility.writeMessage(key, message + " already is your username!");

                    return null;
                }

                //If the username is taken
                if (this.takenUsernames.contains(message)) {
                    ChatUtility.writeMessage(key, message + " is already taken!");

                    return null;
                }

                //If the user has a username and the new one is not taken
                if (!"Anonymous".equals(currentName) && !this.takenUsernames.contains(message)) {
                    this.takenUsernames.remove(currentName);
                    this.takenUsernames.add(message);

                    ConnectionAttachment.setUsername(key, message);

                    return String.format("%s changed their name to %s.", currentName, message);
                }

                //If the user doesn't have a username and it is free
                if ("Anonymous".equals(currentName) && !this.takenUsernames.contains(message)) {
                    ConnectionAttachment.setUsername(key, message);
                    this.takenUsernames.add(message);

                    return ChatUtility.joinMessage(message);
                }
            }

            //If it does not contain "/user", prepend the username
            return ChatUtility.prependUsername(key, message);
        }

        //If the message was null, return null
        return null;
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

    private Iterator<SelectionKey> getReadySet(Selector selector) throws IOException {
        int readyCount = selector.selectNow();

        if (readyCount > 0) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            return selectedKeys.iterator();
        }

        return null;
    }

    private boolean hasConnections() {
        return !this.getAllConnections().isEmpty() || !this.receivedAConnection;
    }

    private Set<SelectionKey> getAllConnections() {
        return this.mainSelector.keys().stream().filter(k -> k.attachment() != null).collect(Collectors.toSet());
    }

    private void log(String message) {
        System.out.printf("[%1$tH:%1$tM] Server log - %2$s%n", LocalTime.now(), message);
    }

    private void logError(String message, Throwable error) {
        System.err.printf("[%1$tH:%1$tM] Server log - \"%2$s - %3$s\"%n", LocalTime.now(), message, error.getMessage());
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.run();
    }
}