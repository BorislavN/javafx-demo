package app.fxchat.unicast.fx;

import app.fxchat.unicast.ChatApp;
import app.fxchat.unicast.nio.ChatClient;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.service.ReceiverService;
import app.fxchat.unicast.service.SenderService;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

//TODO: remove listeners on restart
//TODO: stop the receiver task after "connection reset"
public class ChatController {
    @FXML
    private Label joinPageError, announcementMessage;
    @FXML
    private TextField usernameInput, messageInput;
    @FXML
    private Button joinBtn, backBtn, sendBtn;
    @FXML
    private TextArea chatArea;
    private ChatClient client;
    private SenderService senderService;
    private ReceiverService receiverService;
    private Deque<String> userMessages;
    private List<String> chatHistory;

    public void onEnter(ActionEvent event) {
        event.consume();

        String targetId = ((Node) event.getTarget()).getId();

        if ("usernameInput".equals(targetId)) {
            this.joinBtn.fire();
        }

        if ("messageInput".equals(targetId)) {
            this.sendBtn.fire();
        }
    }

    public void onJoin(ActionEvent event) {
        event.consume();

        String username = this.usernameInput.getText();
        String message = ChatUtility.newJoinRequest(username);

//        this.joinBtn.setDisable(true);

        if (!this.receiverService.isRunning()) {
            this.receiverService.start();
        }

        this.queueMessage(message);


//        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "chat-view.fxml");
//
//        Stage stage = Initializer.getStage(this.joinBtn);
//        stage.setScene(sceneContext.getScene());
    }

    public void showSettings(ActionEvent event) {
        event.consume();

        Stage stage = Initializer.buildStage("Settings", Modality.APPLICATION_MODAL);
        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "settings-view.fxml");

        stage.setX(600);
        stage.setY(250);

        stage.setScene(sceneContext.getScene());
        stage.showAndWait();
    }

    public void onSend(ActionEvent event) {
        event.consume();

        String message = ChatUtility.newPublicMessage(this.messageInput.getText());
        this.queueMessage(message);
    }

    public void onChangeName(ActionEvent event) {
        event.consume();

        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "join-view.fxml");

        Stage stage = Initializer.getStage(this.backBtn);
        stage.setScene(sceneContext.getScene());
    }

    public void onShowMessages(ActionEvent event) {
        event.consume();

        Stage stage = Initializer.buildStage("Direct Messages", Modality.NONE);
        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "message-view.fxml");

        stage.setX(200);
        stage.setY(200);

        stage.setScene(sceneContext.getScene());
        stage.show();
    }

    public void onClose(WindowEvent event, Stage stage) {
        event.consume();

        if (this.client != null) {
            this.client.sendMessage(ChatUtility.newQuitRequest());
        }

        if (this.client != null) {
            this.client.shutdown();
        }

        stage.close();
    }

    public void configureClient() {
        try {
            this.userMessages = new ArrayDeque<>();
            this.chatHistory = new ArrayList<>();

            this.client = new ChatClient();
            this.senderService = new SenderService(this.client);
            this.receiverService = new ReceiverService(this.client);

            this.receiverService.latestMessageProperty().addListener(this.getChangeHandler());

            this.senderService.setOnSucceeded((event -> {
                System.out.println("onSucc");
                String msg = this.userMessages.poll();

                if (msg != null) {
                    this.senderService.reset();
                    this.senderService.setCurrentMessage(msg);
                    this.senderService.restart();
                }
            }));

            this.senderService.setOnFailed((event -> {
                System.out.println("onFail");
                System.err.printf("SenderTask failed for message - \"%s\"%n", this.senderService.getCurrentMessage());
            }));

        } catch (IOException e) {
            this.joinPageError.setText("Client initialization failed");
            this.joinPageError.setVisible(true);
            this.joinBtn.setDisable(true);
        }
    }

    private ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.chatHistory.add(newValue);
                System.out.println(newValue);

                if (this.chatArea != null) {
                    this.chatArea.appendText(newValue);
                }
            }
        };
    }

    private void queueMessage(String message) {
        if (!this.senderService.isRunning()) {
            this.senderService.setCurrentMessage(message);
            this.senderService.reset();
            this.senderService.start();

            return;
        }

        this.userMessages.offer(message);
    }
}