package app.fxchat.unicast.fx;

import app.fxchat.unicast.ChatApp;
import app.fxchat.unicast.nio.ChatUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

//TODO: finish implementation
public class MainController {
    @FXML
    private Label announcementMessage;
    @FXML
    private TextField messageInput;
    @FXML
    private Button backBtn, sendBtn;
    @FXML
    private TextArea chatArea;
    private ChatContext context;

    public void onEnter(ActionEvent event) {
        event.consume();

        this.sendBtn.fire();
    }

    public void onSend(ActionEvent event) {
        event.consume();

        String message = ChatUtility.newPublicMessage(this.messageInput.getText());

//        this.queueMessage(message);
    }

    public void onChangeName(ActionEvent event) {
        event.consume();

        SceneWrapper sceneWrapper = Initializer.buildScene(ChatApp.class, "join-view.fxml");

        Stage stage = Initializer.getStage(this.backBtn);
        stage.setScene(sceneWrapper.getScene());
    }

    public void onShowMessages(ActionEvent event) {
        event.consume();

        Stage stage = Initializer.buildStage("Direct Messages", Modality.NONE);
        SceneWrapper sceneWrapper = Initializer.buildScene(ChatApp.class, "message-view.fxml");

        stage.setX(200);
        stage.setY(200);

        stage.setScene(sceneWrapper.getScene());
        stage.show();
    }

    public void setContext(ChatContext context) {
        this.context = context;

        this.chatArea.setText(String.join(System.lineSeparator(), this.context.getChatHistory().get("public")));
    }

    //    this.receiverService.latestMessageProperty().addListener(this.getChangeHandler());


//    private ChangeListener<String> getChangeHandler() {
//        return (observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.chatHistory.add(newValue);
//                System.out.println(newValue);
//
//                if (newValue.startsWith("#joined|" + this.latestUsername)) {
//                    this.client.setUsername(this.latestUsername);
//                    this.showMainView();
//                    this.initializeChatArea();
//                }
//
//                if (this.chatArea != null) {
//                    this.chatArea.appendText(newValue);
//                }
//            }
//        };
//    }
}