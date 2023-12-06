package app.fxchat.unicast.service;

import app.fxchat.unicast.fx.ChatContext;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Executors;

//Validates that the context is initialized successfully
public class ValidationService extends Service<ChatContext> {
    private String address;
    private int  port;

    public void setParameters(String address,int port) {
        this.address = address;
        this.port = port;

        //Creates new thread, only if current is occupied
        this.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);

            return thread;
        }));
    }

    @Override
    protected Task<ChatContext> createTask() {
        return new  ValidatorTask(this.address,this.port);
    }
}