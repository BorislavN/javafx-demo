package app.unicast.fx;

import app.unicast.nio.ChatUtility;
import app.unicast.nio.Constants;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
        if (!ChatContext.isNotNull(context)) {
            this.setErrorMessage(Constants.CONTEXT_ERROR, true);
            return;
        }

        this.context = context;

        if (this.context.getUsername() != null) {
            this.chosenUsername = this.context.getUsername();
            this.usernameInput.setText(this.chosenUsername);
            this.usernameInput.positionCaret(this.chosenUsername.length());
        }

        if (!this.context.isClientLive()) {
            this.setErrorMessage(Constants.CONNECTION_ERROR, true);
            return;
        }

        this.context.setMessageListener(this.getChangeHandler());
        this.context.setReceiverServiceFailHandler(this.failureHandler());

        Stage currentStage = Initializer.getStage(this.joinBtn);

        currentStage.setOnCloseRequest(this.cleanupBeforeClose(context, currentStage));
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
            this.setErrorMessage(e.getMessage(), false);
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

        if (ChatContext.isNotNull(this.context) && this.context.isClientLive()) {
            this.joinPageError.setVisible(false);
            this.joinBtn.setDisable(false);
        }
    }

    private void showMainView() {
        Scene scene = Initializer.buildMainScene(this.context, Initializer.getStage(this.joinBtn));
        Stage stage = Initializer.getStage(this.joinBtn);
        stage.setScene(scene);
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Received: " + newValue);

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

                    this.context.addToHistory(Constants.DEFAULT_KEY, value);

                    this.showMainView();
                }

                if (newValue.startsWith(Constants.USERNAME_EXCEPTION_FLAG)) {
                    this.setErrorMessage(value, false);
                }
            }
        };
    }

    private EventHandler<WorkerStateEvent> failureHandler() {
        return (event) -> {
            this.setErrorMessage(Constants.CONNECTION_ERROR, true);

            this.context.addToHistory(Constants.DEFAULT_KEY, Constants.CONNECTION_ERROR);
            this.context.shutdown();
        };
    }

    private void setErrorMessage(String message, boolean disableJoin) {
        this.joinPageError.setText(message);
        this.joinPageError.setVisible(true);
        this.joinBtn.setDisable(disableJoin);
    }

    private EventHandler<WindowEvent> cleanupBeforeClose(ChatContext context, Stage stage) {
        return event -> {
            event.consume();

            context.getSenderService().setOnSucceeded((e) -> {
                if (context.isMessageQueueEmpty()) {
                    this.closeWindow(context, stage);
                }
            });

            context.getSenderService().setOnFailed((e) -> this.closeWindow(context, stage));

            context.enqueueMessage(ChatUtility.newQuitRequest());
        };
    }

    private void closeWindow(ChatContext context, Stage stage) {
        context.shutdown();
        stage.close();
    }
}