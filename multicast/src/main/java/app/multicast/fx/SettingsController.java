package app.multicast.fx;

import app.multicast.client.MulticastClient;
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

        //Careful on which side of "||" you put your function call, because the operator can "short circuit"
        //In this case if "wasChanged" is in front and "true", the function will not be called
        //"||" will return after the first "true"
        //"&&" will return after the first "false"
        if (this.client.validateIpAddress(newGroup)) {
            if (!newGroup.equals(this.client.getGroupIP())) {
                this.wasChanged = (this.client.changeGroup(newGroup) || this.wasChanged);
            }
        } else {
            this.errorMessage.setText("Invalid IP, must start with \"239\"!");
            this.errorMessage.setVisible(true);

            return;
        }

        if (this.client.validatePort(newPort)) {
            int port = Integer.parseInt(newPort);

            if (port != this.client.getPort()) {
                this.wasChanged = (this.client.changePort(port) || this.wasChanged);
            }
        } else {
            this.errorMessage.setText("Invalid port number!");
            this.errorMessage.setVisible(true);

            return;
        }

        this.stage.close();
    }

    public boolean wasChanged() {
        return this.wasChanged;
    }
}