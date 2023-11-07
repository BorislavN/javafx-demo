package app.fxchat;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//This is just a demo, I will try to implement a proper JavaFx Chat Client, using my TCP/UDP chat implementations
//First "app" I created using JavaFx
public class DummyChatClient extends Application {
    private Stage window;
    private Scene joinScene;
    private Scene mainScene;
    private String username;
    private TextArea area;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.window = stage;
        this.window.setTitle("Chat Client");

        this.joinScene = getJoinScene();
        this.mainScene = getmainScene();

        this.window.setOnCloseRequest((event) -> this.closeClient());

        this.window.setScene(this.joinScene);
        this.window.show();
    }

    private Scene getmainScene() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(50, 20, 50, 20));

        //Mid
        VBox mid = new VBox(10);
        mid.setAlignment(Pos.CENTER);

        this.area = new TextArea();
        this.area.setMaxHeight(150);
        this.area.setDisable(true);

        Button backButton = new Button("Back");
        backButton.setOnAction((event -> this.backHandler()));

        mid.getChildren().addAll(area, backButton);

        //Top
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER);

        TextField input = new TextField();
        input.setMinWidth(400);
        input.setPromptText("Enter message...");

        Button sendButton = new Button("Send");
        sendButton.setOnAction((event -> this.handleMessageInput(input)));

        top.getChildren().addAll(input, sendButton);

        pane.setTop(top);
        pane.setCenter(mid);

        return new Scene(pane, 500, 500);
    }

    private void closeClient() {
        if (this.username != null && this.area != null) {
            String history = this.area.getText();
            history += this.username + " left the chat!" + System.lineSeparator();
            this.area.setText(history);
        }
    }

    private void backHandler() {
        this.window.setScene(this.joinScene);
    }

    private Scene getJoinScene() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.setAlignment(Pos.CENTER);

        Label error = new Label("Invalid username!");
        error.setVisible(false);

        Label label = new Label("Enter an username:");

        TextField field = new TextField();

        Button button = new Button("Join");
        button.setOnAction(event -> joinHandler(error, field));

        box.getChildren().addAll(error, label, field, button);

        return new Scene(box, 300, 300);
    }

    private void handleMessageInput(TextField field) {
        String input = field.getText();

        if (input != null && !input.isBlank() && input.length() <= 50) {
            field.setText("");

            String history = this.area.getText();
            history += this.username + ": " + input + System.lineSeparator();

            this.area.setText(history);
        }
    }

    private void joinHandler(Label error, TextField field) {
        String value = field.getText();

        if (value == null || value.isBlank() || value.length() > 20) {
            error.setVisible(true);
        } else {
            error.setVisible(false);
            this.username = value;
            this.area.setText(this.username + " joined the chat!" + System.lineSeparator());
            this.window.setScene(this.mainScene);
        }
    }
}
