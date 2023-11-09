package app.fxchat.unicast.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

//TODO: rework client for javafx application
public class ChatClient implements Runnable {
    private final SocketChannel client;
    private final Queue<String> messageQueue;
    private final BufferedReader bufferedReader;

    public ChatClient() throws IOException {
        this.client = SocketChannel.open(new InetSocketAddress(ChatUtility.HOST, ChatUtility.PORT));
        this.messageQueue = new ArrayBlockingQueue<>(21);
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        this.client.configureBlocking(false);
    }

    @Override
    public void run() {
        ConsoleReader reader = new ConsoleReader(this.bufferedReader, this.messageQueue);
        Thread readerThread = new Thread(reader);
        readerThread.start();

        String message;

        try {
            do {
                message = this.messageQueue.poll();

                if (message != null) {
                    ChatUtility.writeMessage(this.client, message);
                }

                String response = ChatUtility.readMessage(this.client);

                if (!response.isBlank()) {
                    System.out.println(response);
                }

            } while (!"/quit".equals(message) && readerThread.isAlive());

        } catch (IOException | IllegalStateException e) {
            System.err.println("Client encountered exception - " + e.getMessage());
        } finally {
            this.shutdown();
        }
    }

    private void shutdown() {
        try {
            this.bufferedReader.close();
            this.client.shutdownInput();
            this.client.shutdownOutput();
            this.client.close();
        } catch (IOException e) {
            System.err.println("Encountered exception while trying to close client - " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient();
        client.run();
    }
}
