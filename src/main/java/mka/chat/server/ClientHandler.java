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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //авторизация
                        while (true) {
                            String account = in.readUTF();
                            if (account.startsWith("/auth")) {
                                String[] creds = account.split(" ");
                                nickname = AuthServer.getNickByLoginPassword(creds[1], creds[2]);

                                if (isUserCorrect(nickname, server)) {
                                    break;
                                }
                            }
                        }

                        //сообщения
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/end");
                                    System.out.println("Client " + nickname + " out");
                                    break;
                                }
                                if (str.startsWith("/show")){
                                    server.sendOnlineUsers();
                                }
                                //личные сообщения
                                if (str.startsWith("/w")) {
                                    String[] personalMsg = str.split(" ");
                                    String nickTo = personalMsg[1];
                                    String msg = str.substring(3 + nickTo.length());
                                    server.sendToClient(nickTo, ClientHandler.this, msg);
                                }
                                continue;
                            }
                            server.sendToAllMsg(nickname + ": " + str);//отправка сообщения
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
                }
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
