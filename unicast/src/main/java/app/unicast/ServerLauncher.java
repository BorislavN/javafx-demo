package app.unicast;

import app.unicast.nio.ChatServer;
import app.unicast.nio.Constants;

import java.io.IOException;

public class ServerLauncher {
    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer(Constants.HOST, Constants.PORT);
        chatServer.run();
    }
}
