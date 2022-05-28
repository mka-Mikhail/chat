package mka.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
    ListView<String> clientList;


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

    public void setActive() {
        chatPanel.setVisible(true);
        authorizationPanel.setVisible(false);
    }

    public void sendMsg() {
        try {
            if (!textField.getText().isEmpty()) {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            }
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
                        //авторизация
                        while (true) {
                            try {
                                String str = in.readUTF();
                                if (str.startsWith("/authok")) {
                                    setActive();
                                    textArea.appendText(str);
                                    break;
                                } else {
                                    authTextArea.clear();
                                    authTextArea.appendText(str);
                                }
                            } catch (SocketException e) {
                                System.out.println("Server don't callback");
                                break;
                            }
                        }

                        //сообщения
                        while (true) {
                            try {
                                String msg = in.readUTF();
                                if (msg.startsWith("/")) {
                                    if (msg.startsWith("/show")) {
                                        String[] nicknames = msg.split(" ");

                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                clientList.getItems().clear();

                                                for (int i = 1; i < nicknames.length; i++) {
                                                    clientList.getItems().add(nicknames[i]);
                                                }
                                            }
                                        });
                                    }
                                    if (msg.equals("/end")) {
                                        break;
                                    }
                                } else {
                                    textArea.appendText(msg);
                                }
                            } catch (SocketException e){
                                System.out.println("Server don't callback");
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
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}