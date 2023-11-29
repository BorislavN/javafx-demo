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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//TODO: Fix the many errors, add a singular event handler, not for each contact
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

            this.chatArea.appendText(System.lineSeparator());
            this.chatArea.appendText(text);

            this.setDestinationLabel();
            this.messageInput.clear();

        } catch (IllegalArgumentException e) {
            this.setErrorMessage(e.getMessage());
        }
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.startsWith(Constants.MEMBERS_COMMAND)) {
                    String names = this.context.extractUserMessage(newValue);
                    String[] users = this.context.extractMessageData(names, ";");

                    if (users.length == 1) {
                        this.setErrorMessage("Currently you are the only user!");
                        this.sendBtn.setDisable(true);
                        this.stage.show();

                        return;
                    }

                    for (String user : users) {
                        if (!user.equals(this.context.getUsername())) {
                            Button button = this.createNewContact(user);

                            this.contacts.getChildren().add(button);
                        }
                    }

                    this.selectFirstContact();
                    this.stage.show();
                }

                if (newValue.startsWith(Constants.LEFT_FLAG)) {
                    String[] data = this.context.extractMessageData(newValue, "\\|");
                    ObservableList<Node> onlineUsers = this.contacts.getChildren();

                    String user = data[1];

                    onlineUsers.removeIf(node -> user.equals(node.getId()));
                }

                if (newValue.startsWith(Constants.JOINED_FLAG)) {
                    String[] data = this.context.extractMessageData(newValue, "\\|");

                    Button button = this.createNewContact(data[1]);

                    this.contacts.getChildren().add(button);
                }

                if (newValue.startsWith(Constants.FROM_FLAG)) {
                    String[] data = this.context.extractMessageData(newValue, "\\|");

                    this.context.addToHistory(data[1], data[2]);
                }
            }
        };
    }

    public void setContext(Stage stage, ChatContext context) {
        this.context = context;
        this.stage = stage;

        //Add listener
        this.context.getReceiverService().latestMessageProperty().addListener(this.getChangeHandler());

        //Remove listener before close
        this.stage.setOnCloseRequest((e) -> {
            this.context.getReceiverService().latestMessageProperty().removeListener(this.getChangeHandler());
        });

        //Request online users
        this.context.enqueueMessage(ChatUtility.newMembersRequest());
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

        VBox.setVgrow(button, Priority.ALWAYS);

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
    }

    private void selectFirstContact() {
        Node firstContact = this.contacts.getChildren().get(0);
        firstContact.getStyleClass().add("selectedButton");

        this.currentDestination = firstContact.getId();
        this.setDestinationLabel();
    }
}