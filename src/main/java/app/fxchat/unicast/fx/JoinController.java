package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//TODO: introduce better exceptuion handling - check if the client is connected,
// when the server is closed we can disable the buttons, show an error popup...
// when the SenderTask fails show error (see if we can get exception message from the task)
public class JoinController {
    @FXML
    private Label joinPageError;
    @FXML
    private TextField usernameInput;
    @FXML
    private Button joinBtn;
    private ChatContext context;
    private String chosenUsername;

    public void setContext(ChatContext context) {
        this.context = context;

        if (this.context.getUsername() != null) {
            this.chosenUsername = this.context.getUsername();
            this.usernameInput.setText(this.chosenUsername);
        }

        this.context.setMessageListener(this.getChangeHandler());
    }

    public void onJoin(ActionEvent event) {
        event.consume();

        try {
            this.joinBtn.setDisable(true);

            String username = this.usernameInput.getText();

            if (username.equals(this.context.getUsername())) {
                this.showMainView();

                return;
            }

            ChatUtility.validateField("Username", username);
            this.chosenUsername = username;

            String message = ChatUtility.newJoinRequest(username);
            this.context.enqueueMessage(message);

        } catch (IllegalArgumentException e) {
            this.joinPageError.setText(e.getMessage());
            this.joinPageError.setVisible(true);

            this.joinBtn.setDisable(false);
        }
    }

    public void onEnter(ActionEvent event) {
        event.consume();

        this.joinBtn.fire();
    }

    public void showSettings(ActionEvent event) {
        event.consume();

        this.joinBtn.setDisable(true);

        this.setContext(Initializer.buildSettingsStage(this.context));

        if (this.context.isInitialized()) {
            this.joinBtn.setDisable(false);
        }
    }

    private void showMainView() {
        Scene scene = Initializer.buildMainScene(this.context);
        Stage stage = Initializer.getStage(this.joinBtn);
        stage.setScene(scene);
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println(newValue);

                String value = this.context.extractUserMessage(newValue);
                String joinPartial = String.format("%s|%s", Constants.JOINED_FLAG, this.chosenUsername);
                String changedPartial = String.format("%s|%s", Constants.CHANGED_FLAG, this.context.getUsername());

                if (newValue.startsWith(joinPartial) || newValue.startsWith(changedPartial)) {
                    String[] data = this.context.extractMessageData(newValue, "\\|");

                    if (this.chosenUsername.equals(data[1])) {
                        this.context.setUsername(this.chosenUsername);
                    } else {
                        this.context.setUsername(this.context.extractMessageData(data[1], ";")[1]);
                    }

                    this.context.addToHistory("public", value);

                    this.showMainView();
                }

                if (newValue.startsWith(Constants.USERNAME_EXCEPTION_FLAG)) {
                    this.joinPageError.setText(value);
                    this.joinPageError.setVisible(true);
                }
            }
        };
    }
}