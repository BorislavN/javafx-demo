package app.fxchat.unicast.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

// TODO:Before we connect to a new address we need to close the current client,
//  then restart it with the new target address
public class SettingsController {
    @FXML
    public Label settingsError;
    @FXML
    public TextField addressInput;
    @FXML
    public TextField portInput;

    public boolean saveSettings(ActionEvent event) {
        return false;
    }
}
