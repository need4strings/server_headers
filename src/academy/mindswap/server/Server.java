package academy.mindswap.server;

import academy.mindswap.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        ClientHandler client = new ClientHandler();
        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                client.handleResponse(clientSocket);
            }
        }
    }
}
