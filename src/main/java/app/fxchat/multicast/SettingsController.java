package app.fxchat.multicast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsController {
    @FXML
    private Label errorMessage;
    @FXML
    private TextField groupInput, portInput;
    private Stage stage;
    private MulticastClient client;
    private boolean wasChanged;

    public SettingsController() {
        this.stage = null;
        this.client = null;
        this.wasChanged = false;
    }

    public void setInitialValues(Stage parent, MulticastClient client) {
        this.stage = parent;
        this.client = client;

        this.groupInput.setText(this.client.getGroupIP());
        this.portInput.setText(String.valueOf(this.client.getPort()));
    }

    public void saveSettings(ActionEvent event) {
        event.consume();

        //Verify that "setInitialValues" was called
        if (this.stage == null || this.client == null) {
            return;
        }

        String newGroup = groupInput.getText();
        String newPort = portInput.getText();

        //This check is first, because if the port was changed, whe have to reinitialize the whole client
        if (this.client.validatePort(newPort)) {
            int port = Integer.parseInt(newPort);

            if (port != this.client.getPort()) {
                this.wasChanged = (this.wasChanged || this.client.changePort(port));
            }
        } else {
            this.errorMessage.setText("Invalid port number!");
            this.errorMessage.setVisible(true);

            return;
        }

        if (this.client.validateIpAddress(newGroup)) {
            if (!newGroup.equals(this.client.getGroupIP())) {
                this.wasChanged = (this.wasChanged || this.client.changeGroup(newGroup));
            }
        } else {
            this.errorMessage.setText("Invalid group IP!");
            this.errorMessage.setVisible(true);

            return;
        }

        this.stage.close();
    }

    public boolean wasChanged() {
        return this.wasChanged;
    }
}