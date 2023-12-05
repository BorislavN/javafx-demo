package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.Constants;
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

            boolean isValid = ChatContext.isNotNull(this.context) && this.context.isClientLive();

            String oldAddress = isValid ? this.context.getClient().getAddress() : "";
            int oldPort = isValid ? this.context.getClient().getPort() : 0;

            Stage stage = Initializer.getStage(this.addressInput);

            if (newAddress.equals(oldAddress) && newPort == oldPort) {
                stage.close();
                return;
            }

            if (!this.portIsValid(newPort)) {
                this.setErrorMessage("Invalid port!");
                return;
            }

            if (!this.service.isRunning()) {
                stage.setOnCloseRequest(Event::consume);

                this.service.reset();
                this.service.setParameters(newAddress, newPort);
                this.service.start();

                this.service.setOnSucceeded((e) -> {
                    e.consume();

                    if (this.context != null) {
                        this.context.shutdown();
                    }

                    this.context = this.service.getValue();

                    stage.setOnCloseRequest(null);
                    stage.close();
                });

                this.service.setOnFailed((e) -> {
                    e.consume();

                    String cause = e.getSource().getException().getClass().getSimpleName();
                    String message = "Client failed initialization!";


                    if ("UnresolvedAddressException".equals(cause)) {
                        message = "Address cannot be resolved!";
                    }

                    if ("ConnectException".equals(cause)) {
                        message = "Connection attempt failed!";
                    }

                    this.setErrorMessage(message);

                    stage.setOnCloseRequest(null);
                });
            }

        } catch (NumberFormatException e) {
            this.setErrorMessage("Port must be integer!");
        }
    }

    public ChatContext getContext() {
        return this.context;
    }

    public void setContext(ChatContext context) {
        if (ChatContext.isNotNull(context)) {
            this.context = context;
            this.addressInput.setText(this.context.getClient().getAddress());
            this.portInput.setText(String.valueOf(this.context.getClient().getPort()));

            if (!this.context.isClientLive()) {
                this.setErrorMessage("Connection was lost!");
            }

            return;
        }

        this.setErrorMessage("Chat server is down!");
        this.addressInput.setText(Constants.HOST);
        this.portInput.setText(String.valueOf(Constants.PORT));
    }

    private boolean portIsValid(int port) {
        return port >= 1024 && port <= 65535;
    }

    private void setErrorMessage(String message) {
        this.settingsError.setText(message);
        this.settingsError.setVisible(true);
    }
}