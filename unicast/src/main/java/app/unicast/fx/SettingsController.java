package app.unicast.fx;

import app.unicast.nio.Constants;
import app.unicast.service.ValidationService;
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

            boolean isInitialized = ChatContext.isNotNull(this.context);
            boolean saveHistory = false;

            String oldAddress = isInitialized ? this.context.getClient().getAddress() : "";
            int oldPort = isInitialized ? this.context.getClient().getPort() : 0;

            Stage stage = Initializer.getStage(this.addressInput);

            if (newAddress.equals(oldAddress) && newPort == oldPort) {
                if (this.context.isClientLive()) {
                    stage.close();

                    return;
                }

                saveHistory = true;
            }

            if (!this.portIsValid(newPort)) {
                this.setErrorMessage("Invalid port!");
                return;
            }

            this.initializeContext(stage, newAddress, newPort, saveHistory);

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
                this.setErrorMessage(Constants.CONNECTION_ERROR);
            }

            return;
        }

        this.setErrorMessage("Chat server is down!");
        this.addressInput.setText(Constants.HOST);
        this.portInput.setText(String.valueOf(Constants.PORT));
    }

    private void initializeContext(Stage stage, String newAddress, int newPort, boolean saveHistory) {
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

                if (saveHistory) {
                    this.service.getValue().copyHistory(this.context.getChatHistory());
                    this.service.getValue().copyUnseenMessages(this.context.getUnseenMessages());
                }

                this.context = this.service.getValue();

                stage.setOnCloseRequest(null);
                stage.close();
            });

            this.service.setOnFailed((e) -> {
                e.consume();

                String cause = e.getSource().getException().getClass().getSimpleName();
                String message = Constants.CONTEXT_ERROR;


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
    }

    private boolean portIsValid(int port) {
        return port >= 1024 && port <= 65535;
    }

    private void setErrorMessage(String message) {
        this.settingsError.setText(message);
        this.settingsError.setVisible(true);
    }
}