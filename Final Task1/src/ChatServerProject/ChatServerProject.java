package ChatServerProject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerProject {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("💛 Chat Server is running...");
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(socket);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send message to all clients (group chat)
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {  // Don't send back to sender
                client.sendMessage(message);
            }
        }
    }

    // Remove client from the set when disconnected
    public static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
    }
}

// Handles each client in a separate thread
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println("Enter your nickname:");
            this.nickname = in.readLine();
            System.out.println(nickname + " joined the chat!");
            broadcast(nickname + " joined the chat!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                // Simple encryption: shift letters by 1
                String encryptedMessage = encrypt(message);
                System.out.println(nickname + ": " + encryptedMessage);
                broadcast(nickname + ": " + encryptedMessage);
            }
        } catch (IOException e) {
            System.out.println(nickname + " disconnected.");
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            ChatServerProject.removeClient(this);
            broadcast(nickname + " left the chat.");
        }
    }

    // Send message to this client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Basic encryption (Caesar cipher +1)
    private String encrypt(String msg) {
        StringBuilder sb = new StringBuilder();
        for (char c : msg.toCharArray()) {
            sb.append((char)(c + 1));
        }
        return sb.toString();
    }

    private void broadcast(String msg) {
        ChatServerProject.broadcast(msg, this);
    }
}