package app.fxchat.unicast.nio;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer("localhost", 8080);
        chatServer.run();
    }
}
