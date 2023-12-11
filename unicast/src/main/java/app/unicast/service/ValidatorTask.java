package app.unicast.service;

import app.unicast.fx.ChatContext;
import javafx.concurrent.Task;

public class ValidatorTask extends Task<ChatContext> {
    private final String address;
    private final int port;

    public ValidatorTask(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    protected ChatContext call() throws Exception {
        return new ChatContext(this.address, this.port);
    }
}