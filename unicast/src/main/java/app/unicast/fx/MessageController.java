package app.unicast.fx;

import app.unicast.nio.ChatUtility;
import app.unicast.nio.Constants;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private FadeTransition animation;
    private Map<String, Button> buttonMap;

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

    public void setContext(Stage stage, ChatContext context) {
        this.context = context;
        this.animation = Initializer.newButtonAnimation(new Button("Temp"));
        this.stage = stage;
        this.buttonMap = new HashMap<>();

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

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                System.out.println("Received: " + newValue);

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

    public void displayConnectionLoss() {
        this.sendBtn.setDisable(true);
        this.setErrorMessage(Constants.CONNECTION_ERROR);
    }

    private boolean handleMembersFlag(String message) {
        if (message.startsWith(Constants.MEMBERS_COMMAND)) {
            String names = this.context.extractUserMessage(message);
            String[] users = this.context.extractMessageData(names, ";");

            Set<String> unseenMessages = this.context.getUnseenMessages();

            for (String user : users) {
                if (!user.equals(this.context.getUsername())) {
                    Button button = this.createNewContact(user);

                    if (unseenMessages.contains(user)) {
                        this.startAnimation(button);
                    }

                    this.contacts.getChildren().add(button);
                }
            }

            this.contacts.addEventHandler(ActionEvent.ACTION, this.contactClickHandler());

            this.selectFirstContact();
            this.stage.show();

            return true;
        }

        return false;
    }

    private boolean handleLeftFlag(String message) {
        if (message.startsWith(Constants.LEFT_FLAG)) {
            String[] data = this.context.extractMessageData(message, "\\|");
            String user = data[1];

            Button removed = this.buttonMap.remove(user);

            if (removed != null) {
                this.contacts.getChildren().remove(removed);
                this.stopAnimation(removed);

                if (user.equals(this.currentDestination)) {
                    this.selectFirstContact();
                }
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

            Button button = this.buttonMap.get(sender);

            if (sender == null) {
                return true;
            }

            if (sender.equals(this.currentDestination)) {
                this.appendToChatArea(text);
                this.stopAnimation(button);

                this.context.markAsSeen(sender);
            }

            this.startAnimation(button);

            return true;
        }

        return false;
    }

    private void startAnimation(Node node) {
        if (node.getId() != null && !node.getId().equals(this.currentDestination)) {
            node.opacityProperty().bind(this.animation.getNode().opacityProperty());
            this.animation.play();
        }
    }

    private void stopAnimation(Node node) {
        node.opacityProperty().unbind();
        node.setOpacity(1);
    }

    private void setDestinationLabel() {
        if (this.context.isConnectionLost()) {
            this.setErrorMessage(Constants.CONNECTION_ERROR);

            return;
        }

        this.receiverLabel.setStyle("");
        this.receiverLabel.setText(String.format("To: %s", this.currentDestination));
    }

    private void setErrorMessage(String errorMessage) {
        this.receiverLabel.setStyle("-fx-background-color: #eb4d42");
        this.receiverLabel.setText(errorMessage);
    }

    private Button createNewContact(String username) {
        Button button = new Button(username);
        button.getStyleClass().add("atButton");
        button.setId(username);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setPadding(new Insets(10, 0, 10, 0));

        this.buttonMap.putIfAbsent(button.getId(), button);

        return button;
    }

    private EventHandler<ActionEvent> contactClickHandler() {
        return (event) -> {
            Node node = (Node) event.getTarget();

            if (node.getId() != null && !node.getId().equals(this.contacts.getId())) {
                this.selectContact(node);
            }
        };
    }

    private void selectFirstContact() {
        ObservableList<Node> contacts = this.contacts.getChildren();

        if (contacts.isEmpty()) {
            this.setErrorMessage("Currently you are the only user!");
            this.currentDestination = null;
            this.sendBtn.setDisable(true);
            this.chatArea.clear();

            return;
        }

        this.selectContact(contacts.get(0));
        this.sendBtn.setDisable(false);
    }

    private void selectContact(Node button) {
        this.contacts.getChildren().stream()
                .filter(e -> e.getId().equals(this.currentDestination))
                .forEach(e -> e.getStyleClass().remove("selectedButton"));

        button.getStyleClass().add("selectedButton");

        this.currentDestination = button.getId();

        this.context.markAsSeen(this.currentDestination);

        this.stopAnimation(button);
        this.setDestinationLabel();
        this.loadFromHistory();
    }

    private void loadFromHistory() {
        List<String> history = this.context.getChatHistory().get(this.currentDestination);
        this.chatArea.clear();

        if (history != null) {
            history.forEach(this::appendToChatArea);
        }
    }

    private void appendToChatArea(String text) {
        this.chatArea.appendText(text);
        this.chatArea.appendText(System.lineSeparator());
    }
}