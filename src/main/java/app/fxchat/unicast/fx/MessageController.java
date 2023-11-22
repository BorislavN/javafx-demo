package app.fxchat.unicast.fx;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MessageController {
    @FXML
    public VBox contacts, messageBox;
    @FXML
    public Label receiverLabel;
    @FXML
    public TextArea chatArea;
    @FXML
    public TextField messageInput;
    @FXML
    public Button sendBtn;

    public void onEnter(ActionEvent event) {
    }

    public void onSend(ActionEvent event) {
    }

    public ChangeListener<String> getChangeHandler() {
        return null;
    }

    public void setContext(ChatContext context) {
    }
}
