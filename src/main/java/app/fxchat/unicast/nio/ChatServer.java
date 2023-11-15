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
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.channels.SelectionKey.*;

public class ChatServer implements Runnable {
    private final ServerSocketChannel server;
    private final Selector mainSelector;
    private final Selector writeSelector;
    private boolean receivedAConnection;

    public ChatServer() throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.bind(new InetSocketAddress(ChatUtility.HOST, ChatUtility.PORT));
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
                //Enqueue the exception
                Attachment.enqueuePriorityMessage(key, e.getMessage());

                //Register the channel for writing (if already registered - the interest set is reset)
                registerInWriteSelector(key);

            } catch (SocketException | IllegalStateException e) {
                this.removeConnection(key);
            }

            iterator.remove();
        }
    }

    private Iterator<SelectionKey> getReadySet(Selector selector) throws IOException {
        int readyCount = selector.select();

        if (readyCount > 0) {
            return selector.selectedKeys().iterator();
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

    private void handleIncomingData(SelectionKey key) throws IOException, IllegalArgumentException, IllegalStateException {
        if (key.isValid() && key.isReadable()) {
            String message = ChatUtility.readMessage(key);

            String[] data = message.split("\\|");

            this.log(message);

            this.handleQuitCommand(key, data);

            this.handleJoinCommand(key, data);

            this.handleMessage(key, data);
        }
    }

    private void handleMessage(SelectionKey origin, String[] data) throws IOException {
        if (ChatUtility.TO_COMMAND.equals(data[0])) {
            this.enqueueMessage(origin, data[1], data[2]);
        }

        //If there is no command
        if (data.length == 1) {
            this.enqueueMessage(origin, null, data[0]);
        }
    }

    private void enqueueMessage(SelectionKey origin, String destination, String message) throws IllegalStateException, IOException {
        for (SelectionKey connection : this.getAllConnections()) {
            if (origin != connection) {
                if (destination != null) {
                    if (Attachment.getUsername(connection).equals(destination)) {
                        Attachment.enqueueMessage(connection, ChatUtility.newFromMessage(origin, message));
                        this.registerInWriteSelector(connection);

                        break;
                    }

                    continue;
                }

                //We reach this code only if the message is public
                Attachment.enqueueMessage(connection, message);
                this.registerInWriteSelector(connection);
            }
        }
    }

    private void registerInWriteSelector(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        channel.register(this.writeSelector, OP_WRITE, key.attachment());
    }


    private void handlePendingMessages(SelectionKey key) throws IOException, IllegalStateException {
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

    private void handleQuitCommand(SelectionKey key, String[] data) throws IOException {
        if (data.length == 0 || ChatUtility.QUIT_COMMAND.equals(data[0])) {
            this.removeConnection(key);

            this.enqueueMessage(key, null, ChatUtility.newLeftMessage(key));
        }
    }

    private void removeConnection(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
    }

    private void handleJoinCommand(SelectionKey key, String[] data) throws IOException, IllegalStateException, IllegalArgumentException {
//        if (message.startsWith("#join")) {
//            String[] data = message.split("\\|");
//
//            if (data.length != 2) {
//                throw new IllegalArgumentException("#usernameException|Malformed join request!");
//            }
//
//            String proposedName = data[1];
//            String setName = Attachment.getUsername(key);
//
//            //If current name and new name match
//            if (setName.equals(proposedName)) {
//                ChatUtility.writeMessage(key, message + " already is your username!");
//                throw new IllegalArgumentException(String.format("#usernameException"));
//                return null;
//            }
//
//            //If the username is taken
//            if (this.takenUsernames.contains(message)) {
//                ChatUtility.writeMessage(key, message + " is already taken!");
//
//                return null;
//            }
//
//            //If the user has a username and the new one is not taken
//            if (!"Anonymous".equals(setName) && !this.takenUsernames.contains(message)) {
//                this.takenUsernames.remove(setName);
//                this.takenUsernames.add(message);
//
//                Attachment.setUsername(key, message);
//
//                return String.format("%s changed their name to %s.", setName, message);
//            }
//
//            //If the user doesn't have a username and it is free
//            if ("Anonymous".equals(setName) && !this.takenUsernames.contains(message)) {
//                Attachment.setUsername(key, message);
//                this.takenUsernames.add(message);
//
//                return ChatUtility.joinMessage(message);
//            }
//        }
//
//        return message;
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

    private Set<SelectionKey> getAllConnections() throws IllegalStateException {
        return this.mainSelector.keys().stream()
                .filter(key -> key.isValid() && Attachment.getUsername(key) != null)
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