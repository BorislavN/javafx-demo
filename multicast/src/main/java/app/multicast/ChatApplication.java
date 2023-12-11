package app.multicast;

import app.multicast.fx.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

//Having a "module-info.java" lets us avoid manual JavaFx configuration, and we are able to start the project from the "Run" button
//Alternatively we can start it from  maven console with "mvn clean javafx:run"
//More information on "https://openjfx.io/openjfx-docs/"
public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //The package trees should match "java/app.multicast" <===> "resources/app.multicast"
        //Or you will get "Location not set", when you try to load the *.fxml file
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        URL cssUrl = getClass().getResource("style.css");

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest((event -> controller.onClose(event, stage)));
        controller.configureClient();

        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}