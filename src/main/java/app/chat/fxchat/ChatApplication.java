package app.chat.fxchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

//Currently just a mockup, no real TCP/UDO connection is made
//Having a "module-info.java" lets us avoid manual JavaFx configuration
//If we delete it, we need to set manually - "-module-path /path/to/javafx/lib --add-modules=javafx.controls,javafx.fxml"
public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        URL cssUrl = getClass().getResource("style.css");

        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest((event -> controller.onClose(event,stage)));
        controller.setStage(stage);

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}