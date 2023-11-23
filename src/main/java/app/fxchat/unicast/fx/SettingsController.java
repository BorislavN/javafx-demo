package app.fxchat.unicast.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.InetAddress;

//TODO: crate a separate task for validating the context, the current way is time-consuming and makes the application slow
public class SettingsController {
    @FXML
    private Label settingsError;
    @FXML
    private TextField addressInput;
    @FXML
    private TextField portInput;
    private ChatContext context;

    public void saveSettings(ActionEvent event) {
        event.consume();

        String newAddress = this.addressInput.getText();
        int newPort = Integer.parseInt(this.portInput.getText());

        if (!newAddress.equals(this.context.getClient().getAddress()) || newPort != this.context.getClient().getPort()) {
            try {
                this.validateSettings(newAddress, newPort);

                ChatContext tempContext = new ChatContext(newAddress, newPort);

                if (!tempContext.isInitialized()) {
                    this.settingsError.setText("ChatContext failed initialization");
                    this.settingsError.setVisible(true);

                    return;
                }

                this.context.shutdown();
                this.context = tempContext;

            } catch (IllegalArgumentException e) {
                this.settingsError.setText(e.getMessage());
                this.settingsError.setVisible(true);

                return;
            }
        }

        Initializer.getStage(this.addressInput).close();
    }

    public ChatContext getContext() {
        return this.context;
    }

    public void setContext(ChatContext context) {
        this.context = context;
        this.addressInput.setText(this.context.getClient().getAddress());
        this.portInput.setText(String.valueOf(this.context.getClient().getPort()));
    }

    private void validateSettings(String address, int port) {
        try {
            boolean reachable = InetAddress.getByName(address).isReachable(1000);

            if (!reachable) {
                throw new IllegalArgumentException("Address unreachable!");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid address!");
        }

        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Invalid port!");
        }
    }
}