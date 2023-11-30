package app.fxchat.unicast.fx;

import app.fxchat.unicast.service.ValidationService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsController {
    @FXML
    private Label settingsError;
    @FXML
    private TextField addressInput;
    @FXML
    private TextField portInput;
    private ChatContext context;
    private final ValidationService service;

    public SettingsController() {
        this.service = new ValidationService();
    }

    public void saveSettings(ActionEvent event) {
        event.consume();

        try {
            String newAddress = this.addressInput.getText();
            int newPort = Integer.parseInt(this.portInput.getText());

            String oldAddress = ChatContext.isValid(this.context) ? this.context.getClient().getAddress() : "";
            int oldPort = ChatContext.isValid(this.context) ? this.context.getClient().getPort() : 0;

            Stage stage = Initializer.getStage(this.addressInput);

            if (newAddress.equals(oldAddress) && newPort == oldPort) {
                stage.close();
                return;
            }

            if (!this.portIsValid(newPort)) {
                this.settingsError.setText("Invalid port!");
                this.settingsError.setVisible(true);

                return;
            }

            if (!this.service.isRunning()) {
                stage.setOnCloseRequest(Event::consume);

                this.service.reset();
                this.service.setParameters(newAddress, newPort);
                this.service.start();

                this.service.setOnSucceeded((e) -> {
                    e.consume();

                    this.context.shutdown();
                    this.context = this.service.getValue();

                    stage.setOnCloseRequest(null);

                    stage.close();
                });

                this.service.setOnFailed((e) -> {
                    e.consume();

                    this.settingsError.setText("Invalid address!");
                    this.settingsError.setVisible(true);

                    stage.setOnCloseRequest(null);
                });
            }

        } catch (NumberFormatException e) {
            this.settingsError.setText("Port must be integer!");
            this.settingsError.setVisible(true);
        }
    }

    public ChatContext getContext() {
        return this.context;
    }

    public void setContext(ChatContext context) {
        if (ChatContext.isValid(context)) {
            this.context = context;
            this.addressInput.setText(this.context.getClient().getAddress());
            this.portInput.setText(String.valueOf(this.context.getClient().getPort()));
        }
    }

    private boolean portIsValid(int port) {
        return port >= 1024 && port <= 65535;
    }
}