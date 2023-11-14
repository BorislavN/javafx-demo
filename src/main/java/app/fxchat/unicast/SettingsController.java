package app.fxchat.unicast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
