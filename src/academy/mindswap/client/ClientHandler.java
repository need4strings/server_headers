package academy.mindswap.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler {

    public void handleResponse(Socket client) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = reader.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];
        String host = requestsLines[1].split(" ")[1];

        String accessLog = String.format("Client: %s\nMethod: %s\nPath: %s\nVersion: %s\nHost: %s\n",
                client.toString(), method, path, version, host);

        Path filePath = getFilePath(path);
        if (Files.exists(filePath) && method.equals("GET")) {
            // file exist
            String contentType = guessContentType(filePath);
            sendResponse(client, "200 Document Follows", contentType, Files.readAllBytes(filePath));
        } else if(!method.equals("GET")) {
            Path notAllowedPath = Path.of("www/405.html");
            sendResponse(client, "405 Method Not Allowed", "text/html", Files.readAllBytes(notAllowedPath));
        } else {
            // 404
            Path notFoundPath = Path.of("www/404.html");
            sendResponse(client, "404 Not Found", "text/html", Files.readAllBytes(notFoundPath));
        }
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        return Paths.get("www", path);
    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        switch (status) {
            case "200 Document Follows" -> {
                clientOutput.write(("HTTP/1.1" + status + "\r\n").getBytes());
                clientOutput.write(("ContentType: " + contentType + "; charset = UTF-8 \r\n").getBytes());
                clientOutput.write("\r\n".getBytes());
                System.out.println("HTTP/1.1 " + status);
                System.out.println("ContentType: " + contentType);
                System.out.println("Content-Length: " + content.length + "\r\n");
                System.out.println("\r\n");
                clientOutput.write(content);
                clientOutput.flush();
                client.close();
            }
            case "404 Not Found", "405 Method Not Allowed" -> {
                clientOutput.write(("HTTP/1.1" + status + "\r\n").getBytes());
                clientOutput.write(("ContentType: " + contentType + "; charset = UTF-8 \r\n").getBytes());
                clientOutput.write("\r\n".getBytes());
                System.out.println("HTTP/1.1 " + status);
                System.out.println("ContentType: " + contentType);
                System.out.println("Content-Length: " + content.length + "\r\n");
                System.out.println("\r\n");
                clientOutput.write(content);
                clientOutput.write("\r\n\r\n".getBytes());
                clientOutput.flush();
                client.close();
            }
        }
    }
}
