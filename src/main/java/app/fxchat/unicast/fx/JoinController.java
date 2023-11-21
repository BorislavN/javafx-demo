package app.fxchat.unicast.fx;

import app.fxchat.unicast.ChatApp;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
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
        this.context = context;
    }

    public void onJoin(ActionEvent event) {
        event.consume();

        this.joinBtn.setDisable(true);

        String username = this.usernameInput.getText();
        this.chosenUsername = username;

        String message = ChatUtility.newJoinRequest(username);

        if (!this.context.getReceiverService().isRunning()) {
            this.context.getReceiverService().start();
            this.context.getReceiverService().latestMessageProperty().addListener(this.getChangeHandler());
        }

        this.context.enqueueMessage(message, false);
    }

    public void onEnter(ActionEvent event) {
        event.consume();

        this.joinBtn.fire();
    }

    public void onClose(WindowEvent event, Stage stage) {
        event.consume();

        this.context.enqueueMessage(ChatUtility.newQuitRequest(), false);

        this.context.getReceiverService().cancel();
        this.context.getReceiverService().latestMessageProperty().removeListener(this.getChangeHandler());

        this.context.shutdown();

        stage.close();
    }

    public void showSettings(ActionEvent event) {
        event.consume();

        Stage stage = Initializer.buildStage("Settings", Modality.APPLICATION_MODAL);
        SceneWrapper sceneWrapper = Initializer.buildScene(ChatApp.class, "settings-view.fxml");

        stage.setX(600);
        stage.setY(250);

        stage.setScene(sceneWrapper.getScene());
        stage.showAndWait();
    }

    private void showMainView() {
        SceneWrapper sceneWrapper = Initializer.buildScene(ChatApp.class, "chat-view.fxml");
        MainController controller = sceneWrapper.getLoader().getController();
        controller.setContext(this.context);

        Stage stage = Initializer.getStage(this.joinBtn);
        stage.setScene(sceneWrapper.getScene());
    }

    private ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println(newValue);

                if (newValue.startsWith(String.format("%s|%s", Constants.JOINED_FLAG, this.chosenUsername))) {
                    this.context.setUsername(this.chosenUsername);

                    this.context.getReceiverService().cancel();
                    this.context.getReceiverService().latestMessageProperty().removeListener(this.getChangeHandler());

                    this.showMainView();
                }

                if (newValue.startsWith(Constants.USERNAME_EXCEPTION_FLAG)) {
                    this.joinPageError.setText(newValue.split("\\|")[1]);
                    this.joinPageError.setVisible(true);
                }
            }
        };
    }
}