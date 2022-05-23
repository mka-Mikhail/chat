package mka.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    ServerSocket server;//серверСокет
    Socket socket;//клиент
    Vector<ClientHandler> clients;

    public void start() {

        clients = new Vector<>();

        try {
            server = new ServerSocket(8080);
            System.out.println("Server start");

            while (true) {
                socket = server.accept();
                System.out.println("Client connect");
                clients.add(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendToAllMsg(String msg) {
        System.out.println("Client send msg: " + msg);
        for (ClientHandler client :
                clients) {
            client.sendMsg(msg);
        }
    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }
}
