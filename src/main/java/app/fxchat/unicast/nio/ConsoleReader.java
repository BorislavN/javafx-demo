package app.fxchat.unicast.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Queue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ConsoleReader implements Runnable {
    private final BufferedReader bufferedReader;
    private final Queue<String> pendingMessages;

    public ConsoleReader(BufferedReader bufferedReader, Queue<String> pendingMessages) {
        this.bufferedReader = bufferedReader;
        this.pendingMessages = pendingMessages;
    }

    @Override
    public void run() {
        String input;

        try {
            do {
                input = this.bufferedReader.readLine();

                //Break - because the end of the stream was reached
                if(input==null){
                    break;
                }

                if (input.isBlank()) {
                    System.out.println("Input can not be blank!");
                    continue;
                }

                if (input.getBytes(UTF_8).length > ChatUtility.MESSAGE_LIMIT) {
                    System.out.println("Input too long!");
                    continue;
                }

                this.pendingMessages.offer(input);

            } while (!"/quit".equals(input));

        } catch (IOException e) {
            System.err.println("ConsoleReader encountered an exception - " + e.getMessage());
        }
    }
}