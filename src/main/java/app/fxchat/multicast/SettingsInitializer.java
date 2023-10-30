package app.fxchat.multicast;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SettingsInitializer {
    //The method returns "true" if the settings were changed
    public static boolean showSettings(MulticastClient client) {
        try {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("settings-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            URL cssUrl = SettingsController.class.getResource("style.css");

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            popup.setTitle("Settings");
            popup.setScene(scene);

            SettingsController controller = fxmlLoader.getController();
            controller.setInitialValues(popup, client);

            popup.showAndWait();

            return controller.wasChanged();

        } catch (IOException e) {
            System.err.println("Failed to crate \"Settings-Window\"!");

            return false;
        }
    }
}