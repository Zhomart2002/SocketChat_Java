import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
/**
Open many clients for chatting
*/
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application {

    private Stage window;
    private TextField name;
    private TextField port;
    private TextField host;

    @Override
    public void start(Stage primaryStage){
        window = primaryStage;
        GridPane pane = new GridPane();

        name = new TextField("");
        name.setAlignment(Pos.CENTER);

        port = new TextField("8000");
        port.setAlignment(Pos.CENTER);

        host = new TextField("127.0.0.1");
        host.setAlignment(Pos.CENTER);

        Button connect = new Button("Connect");
        GridPane.setHalignment(connect, HPos.RIGHT);
        connect.setOnAction(connection());

        pane.setAlignment(Pos.CENTER);
        pane.setHgap(5);
        pane.setVgap(8);
        pane.addRow(0, new Text("Name"), name);
        pane.addRow(1, new Text("Port"), port);
        pane.addRow(2, new Text("Host"), host);
        pane.add(connect, 1, 3);


        Scene scene = new Scene(pane, 250, 250);
        window.setScene(scene);
        window.setTitle("Chat project");
        window.show();
    }

    public EventHandler<ActionEvent> connection(){
        return e -> {
            ChatSide chat = new ChatSide(name.getText(), port.getText(), host.getText());
            window.setScene(chat.getScene());
            window.setResizable(false);
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class ChatSide{
    private String name;
    private int port;
    private String host;

    private Socket socket;
    private DataInputStream fromAnother;
    private DataOutputStream toServer;

    private Scene scene;
    private VBox chat;
    private ScrollPane sc;
    private TextField text;
    private Button button;

    public ChatSide(String name, String port, String host) {
        this.name = name;
        this.port = Integer.parseInt(port);
        this.host = host;

        createScene();
        connectToServer();
        updateServer();
    }

    private void createScene(){

        HBox hbox = new HBox(3);
        VBox vbox = new VBox(3);
        sc = new ScrollPane();
        chat = new VBox(2);

        chat.setMouseTransparent(true);
        chat.setFocusTraversable(false);
        chat.setPrefHeight(500);
        chat.setPrefWidth(400);

        text = new TextField();
        text.setPrefWidth(350);
        text.requestFocus();

        sc.setContent(chat);
        sc.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        sc.setStyle("-fx-background: pink; -fx-border-color: pink; -fx-padding: 10px");
        sc.setHbarPolicy(ScrollBarPolicy.NEVER);

        button = new Button("Send");

        hbox.getChildren().addAll(text, button);
        hbox.setPadding(new Insets(3));
        hbox.setAlignment(Pos.CENTER_LEFT);

        vbox.getChildren().addAll(sc, hbox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: pink;");
        
        scene = new Scene(vbox, 450, 500);
    }

    private void connectToServer(){
            try {
                socket = new Socket(host, port);
                fromAnother = new  DataInputStream(socket.getInputStream());
                toServer = new DataOutputStream(socket.getOutputStream());
                scene.setOnKeyPressed(e -> {
                	if (e.getCode() == KeyCode.ENTER)
                		sendToServer();});
                button.setOnAction(e -> sendToServer());
            } catch (IOException e) {
                System.out.println("Port is not open");
            }    
    }

    private void sendToServer(){
            if (text.getText().length() != 0)
                try {
                    chat.getChildren().add(newBox(text.getText(), Pos.CENTER_RIGHT));
                    toServer.writeUTF(getName() + ": " + text.getText());
                    text.clear();
                } catch (IOException e1) {
                    System.out.println("Dicsonnected");
                }
    }
    private void updateServer(){
        new Thread(() -> {
                try {
                   String message;
                    while(true){
                        message = fromAnother.readUTF();
                        HBox hBox = newBox(message, Pos.CENTER_LEFT);
                        Platform.runLater(() -> chat.getChildren().add(hBox));
                    }
                } catch (IOException e) {
                    System.out.println("Dicsonnected");
                }
            }).start();
    }

    private HBox newBox(String text, Pos pos){
        Label label = new Label(text);
        label.setStyle("-fx-background-color: #befaff; -fx-padding: 5px; -fx-background-radius: 7;");
        label.setMaxSize(215, Double.MAX_VALUE);
        label.setWrapText(true);

        HBox hBox = new HBox(label);
        hBox.setAlignment(pos);
        return hBox;
    }

    public Scene getScene() {
        return scene;
    }

    public String getName() {
        return name;
    }
}
