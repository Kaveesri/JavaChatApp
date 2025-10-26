package ChatServerProject;

import java.awt.TextArea;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.tree.DefaultTreeCellEditor.DefaultTextField;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatClientServer extends ChatServerProject {
    private BufferedReader in;
    private PrintWriter out;
    private TextArea messageArea = new TextArea();
    private DefaultTextField inputField = new TextField();

    @Override
    public void start(Stage primaryStage) throws Exception {
        connectToServer();

        messageArea.setEditable(false);
        inputField.setOnAction(e -> {
            String msg = inputField.getText();
            out.println(msg);  // Send to server
            inputField.clear();
        });

        VBox layout = new VBox(10, messageArea, inputField);
        Scene scene = new Scene(layout, 400, 400);

        primaryStage.setTitle("💛 Java Chat");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Thread to read messages from server
        new Thread(() -> {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    String decrypted = decrypt(msg);
                    messageArea.appendText(decrypted + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Connect to server
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask for nickname
            String serverMsg = in.readLine();
            String nickname = askNickname(serverMsg);
            out.println(nickname);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Popup dialog to enter nickname
    private String askNickname(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nickname");
        dialog.setHeaderText(message);
        return dialog.showAndWait().orElse("Guest");
    }

    // Simple decryption (shift letters -1)
    private String decrypt(String msg) {
        StringBuilder sb = new StringBuilder();
        for (char c : msg.toCharArray()) {
            sb.append((char)(c - 1));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}