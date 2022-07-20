package mka.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class Server {

    ServerSocket server;//серверСокет
    Socket socket;//клиент
    Vector<ClientHandler> clients;//подключенные клиенты

    public void start() {

        clients = new Vector<>();

        try {
            AuthServer.connect();

            server = new ServerSocket(8080);
            System.out.println("Server start");

            while (true) {
                socket = server.accept();
                System.out.println("Client connect");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendToAllMsg("/end");
        }
        AuthServer.disconnect();
    }

    //для личных сообщений
    public void sendToClient(String nickTo, ClientHandler nickFrom, String msg) {
        for (ClientHandler client :
                clients) {
            if (client.getNickname().equals(nickTo)) {
                client.sendMsg("от " + nickFrom.getNickname() + ": " + msg);
                nickFrom.sendMsg("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        nickFrom.sendMsg(nickTo + " не в чате");
    }

    public void sendToAllMsg(String msg) {
        for (ClientHandler client :
                clients) {
            client.sendMsg(msg);
        }
    }

    public void sendOnlineUsers() {
        StringBuilder sb = new StringBuilder(" ");
        List<String> list = clients.stream().map(ClientHandler::getNickname).collect(Collectors.toList());

        for (String s :
                list) {
            sb.append(s);
            sb.append(" ");
        }
        sendToAllMsg("/show " + sb.toString().trim());
    }

    //проверка на свободность ника
    public boolean isNickFree(String nickname) {
        if (nickname != null) {
            if (clients.isEmpty()) {
                return true;
            }
            for (ClientHandler client :
                    clients) {
                if (client.getNickname().equals(nickname)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        sendToAllMsg("Пользователь " + client.getNickname() + " покинул чат");
        sendOnlineUsers();
        clients.remove(client);
    }
}
