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
                        while (true) {
                            String msg = in.readUTF();
                            if (msg.equals("/end")) {
                                System.out.println("Client out");
                                break;
                            }
                            server.sendToAllMsg(msg);//отправка сообщения
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
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
