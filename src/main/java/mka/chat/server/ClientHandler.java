package mka.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//обработчик одного пользователя
public class ClientHandler {
    Server server;//наш сервер
    Socket socket;

    DataInputStream in;
    DataOutputStream out;

    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    String[] creds;
                    //авторизация
                    while (true) {
                        String account = in.readUTF();
                        if (account.startsWith("/auth")) {
                            creds = account.split(" ");
                            nickname = AuthServer.getNickByLoginPassword(creds[1], creds[2]);

                            if (isUserCorrect(nickname, server)) {
                                break;
                            }
                        }
                    }

                    //сообщения
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                out.writeUTF("/end");
                                System.out.println("Client " + nickname + " out");
                                break;
                            }
                            if (msg.startsWith("/getMyProfile")) {
                                out.writeUTF("/getMyProfile Логин: " + creds[1] + "\nНик: " + nickname);
                            }
                            if (msg.startsWith("/newNick")) {
                                String newNick = msg.substring(8);
                                if (server.isNickFree(newNick)) {
                                    AuthServer.changeNickByLogin(newNick, creds[1]);
                                    nickname = AuthServer.getNickByLoginPassword(creds[1], creds[2]);
                                    out.writeUTF("/newNick Логин: " + creds[1] + "\nНик: " + nickname);
                                }
                                server.sendOnlineUsers();
                            }
                            //личные сообщения
                            if (msg.startsWith("/w")) {
                                String[] personalMsg = msg.split(" ");
                                String nickTo = personalMsg[1];
                                String totalMsg = msg.substring(3 + nickTo.length());
                                server.sendToClient(nickTo, ClientHandler.this, totalMsg);
                            }
                            if (msg.startsWith("/show")){
                                server.sendOnlineUsers();
                            }
                            continue;
                        } else {
                            server.sendToAllMsg(nickname + ": " + msg);//отправка сообщения
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        out.writeUTF("/end");
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                server.unsubscribe(ClientHandler.this);
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserCorrect(String nickname, Server server) {
        if (server.isNickFree(nickname)) {
            server.subscribe(ClientHandler.this);
            sendServiceMsg("/authok" + " Вы зашли под ником " + nickname);
            server.sendOnlineUsers();
            return true;
        } else {
            sendServiceMsg("Некорректные данные");
            return false;
        }
    }

    public void sendMsg(String msg) {
        System.out.println("Client send msg: " + msg);
        try {
            out.writeUTF(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServiceMsg(String msg) {
        System.out.println("Server send msg: " + msg);
        try {
            out.writeUTF(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
