package mka.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientController {

    //Сообщение при отключении от сервера
    @FXML
    private Label labelYouDisconnect;

    //Окно информации о пользователе
    @FXML
    private Pane paneTools;
    @FXML
    private TextArea infoTextArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button changeNickButton;
    @FXML
    private Button backButton;
    @FXML
    private TextField newNickTextField;


    //Элементы окна чата
    @FXML
    private Button buttonSend;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private ListView<String> clientList;
    @FXML
    private HBox chatPanel;
    @FXML
    public Button toolsButton;

    //Элементы окна авторизации
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    @FXML
    private TextArea authTextArea;
    @FXML
    private VBox authorizationPanel;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8080;

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

            new Thread(() -> {
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
                                    labelYouDisconnect.setVisible(true);
                                    break;
                                }
                                if (msg.startsWith("/getMyProfile")) {
                                    infoTextArea.clear();
                                    String info = msg.substring(14);
                                    infoTextArea.appendText(info);
                                }
                                if (msg.startsWith("/newNick")) {
                                    infoTextArea.clear();
                                    String info = msg.substring(9);
                                    infoTextArea.appendText(info);
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

    //Открываем панель настроек и запрашиваем у сервера наш Ник
    public void openTools() {
        chatPanel.setVisible(false);
        paneTools.setVisible(true);
        try {
            out.writeUTF("/getMyProfile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Закрываем панель настроек
    public void closeTools() {
        paneTools.setVisible(false);
        chatPanel.setVisible(true);
    }
    public void openFieldForNewNick() {
        newNickTextField.setVisible(true);
    }
    public void changeNick() {
        try {
            out.writeUTF("/newNick " + newNickTextField.getText());
            newNickTextField.clear();
            newNickTextField.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}