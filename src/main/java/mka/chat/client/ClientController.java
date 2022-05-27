package mka.chat.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController {
    @FXML
    private Button buttonSend;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;


    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button authButton;
    @FXML
    TextArea authTextArea;

    @FXML
    VBox authorizationPanel;
    @FXML
    HBox chatPanel;

    String IP_ADDRESS = "localhost";
    int PORT = 8080;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String msg = in.readUTF();
                            textArea.appendText(msg);
                            if (msg.equals("/end")) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                            in.close();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth() {
        if (loginField.getText().isBlank() || passwordField.getText().isBlank()) {
            authTextArea.clear();
            authTextArea.appendText("Введите Логин/Пароль");
            return;
        }
        if (socket == null || socket.isClosed()) {
            connect();
        }
    }
}