package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

//TODO: Fix the many errors, add a singular event handler, not for each contact
//TODO: Fix CSS, add CSS stying when messages are pending on an contact
//TODO: Fix messages not being received/sent
public class MessageController {
    @FXML
    private VBox contacts;
    @FXML
    private Label receiverLabel;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendBtn;
    private ChatContext context;
    private Stage stage;
    private String currentDestination;

    public void onEnter(ActionEvent event) {
        event.consume();

        this.sendBtn.fire();
    }

    public void onSend(ActionEvent event) {
        event.consume();

        try {
            String text = this.messageInput.getText();

            ChatUtility.validateField("Message", text);

            text = this.context.wrapMessage(text);

            this.context.addToHistory(this.currentDestination, text);
            this.context.enqueueMessage(ChatUtility.newDirectMessageRequest(this.currentDestination, text));

            this.appendToChatArea(text);

            this.setDestinationLabel();
            this.messageInput.clear();

        } catch (IllegalArgumentException e) {
            this.setErrorMessage(e.getMessage());
        }
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                System.out.println(newValue);

                if (this.handleMembersFlag(newValue)) {
                    return;
                }

                if (this.handleJoinFlag(newValue)) {
                    return;
                }

                if (this.handleLeftFlag(newValue)) {
                    return;
                }

                this.handleFromFlag(newValue);
            }
        };
    }

    public void setContext(Stage stage, ChatContext context) {
        this.context = context;
        this.stage = stage;

        ChangeListener<String> changeListener = this.getChangeHandler();

        //Add listener
        this.context.getReceiverService().latestMessageProperty().addListener(changeListener);

        //Remove listener before close
        this.stage.setOnCloseRequest((e) -> {
            this.context.getReceiverService().latestMessageProperty().removeListener(changeListener);
        });

        //Request online users
        this.context.enqueueMessage(ChatUtility.newMembersRequest());
    }

    private boolean handleMembersFlag(String message) {
        if (message.startsWith(Constants.MEMBERS_COMMAND)) {
            String names = this.context.extractUserMessage(message);
            String[] users = this.context.extractMessageData(names, ";");

            for (String user : users) {
                if (!user.equals(this.context.getUsername())) {
                    Button button = this.createNewContact(user);
                    this.contacts.getChildren().add(button);
                }
            }

            this.selectFirstContact();
            this.stage.show();

            return true;
        }

        return false;
    }

    private boolean handleLeftFlag(String message) {
        if (message.startsWith(Constants.LEFT_FLAG)) {
            String[] data = this.context.extractMessageData(message, "\\|");
            ObservableList<Node> onlineUsers = this.contacts.getChildren();

            String user = data[1];

            boolean result = onlineUsers.removeIf(node -> user.equals(node.getId()));

            if (result && this.currentDestination.equals(user)) {
                this.selectFirstContact();
            }

            return true;
        }

        return false;
    }

    private boolean handleJoinFlag(String message) {
        if (message.startsWith(Constants.JOINED_FLAG)) {
            String[] data = this.context.extractMessageData(message, "\\|");

            Button button = this.createNewContact(data[1]);

            this.contacts.getChildren().add(button);

            if (this.currentDestination == null) {
                this.selectFirstContact();
            }

            return true;
        }

        return false;
    }

    private boolean handleFromFlag(String message) {
        if (message.startsWith(Constants.FROM_FLAG)) {
            String[] data = this.context.extractMessageData(message, "\\|");

            String sender = data[1];
            String text = data[2];

            this.context.addToHistory(sender, text);

            if (sender.equals(this.currentDestination)) {
                this.appendToChatArea(text);
            }

            return true;
        }

        return false;
    }


    private void setDestinationLabel() {
        this.receiverLabel.setStyle("");
        this.receiverLabel.setText(String.format("To: %s", this.currentDestination));
    }

    private void setErrorMessage(String errorMessage) {
        this.receiverLabel.setStyle("-fx-background-color: #eb4d42");
        this.receiverLabel.setText(errorMessage);
    }

    private Button createNewContact(String username) {
        Button button = new Button(username);
        button.setId(username);

//        VBox.setVgrow(button, Priority.ALWAYS);

        button.getStyleClass().add("atButton");
        button.setOnAction((e) -> this.contactAction(e, button));

        return button;
    }

    private void contactAction(ActionEvent event, Button button) {
        event.consume();

        this.contacts.getChildren().stream()
                .filter(n -> n.getId().equals(this.currentDestination))
                .forEach(e -> e.getStyleClass().remove("selectedButton"));

        button.getStyleClass().add("selectedButton");

        this.currentDestination = button.getId();

        this.setDestinationLabel();
        this.loadFromHistory();
    }

    private void selectFirstContact() {
        ObservableList<Node> contacts = this.contacts.getChildren();

        if (contacts.isEmpty()) {
            this.setErrorMessage("Currently you are the only user!");
            this.sendBtn.setDisable(true);

            return;
        }

        Node firstContact = contacts.get(0);
        firstContact.getStyleClass().add("selectedButton");

        this.currentDestination = firstContact.getId();

        this.sendBtn.setDisable(false);
        this.setDestinationLabel();
        this.loadFromHistory();
    }

    private void loadFromHistory() {
        List<String> history = this.context.getChatHistory().get(this.currentDestination);

        if (history != null) {
            history.forEach(this::appendToChatArea);
        }
    }

    private void appendToChatArea(String text) {
        this.chatArea.appendText(text);
        this.chatArea.appendText(System.lineSeparator());
    }
}