import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
public class ChatServer {
    private static final int PORT = 5000;
    // Thread-safe set of all connected clients' writers, keyed by client handler
  //Why not hashset instead of concurrenthashmap? its not thread safe
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();//nstead of storing sockets directly, we store ClientHandler objects. Each handler represents 1 connected user.
    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();//threads can be reused in poll ucoz singly itll be expensive
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {//creates server
            System.out.println("Chat server started on port " + PORT);
            while (true) {
                // Blocks until a client connects
                Socket clientSocket = serverSocket.accept();//Only after a client connects does execution continue.
                ClientHandler handler = new ClientHandler(clientSocket);//Now we create an object responsible for this specific client.
                clients.add(handler);
                pool.execute(handler); // run each client on its own thread
            }
        }
    }
    // Send a message to every connected client
    private static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {//dont send msg back to sender
                client.send(message);
            }
        }
    }
    // Remove a client when they disconnect
    private static void removeClient(ClientHandler client) {//called when someone disconnects
        clients.remove(client);
        broadcast(client.username + " has left the chat.", client);
        System.out.println(client.username + " disconnected.");
    }
    // Handles communication with a single client
    private static class ClientHandler implements Runnable {//every client gets one clientHandler and that can run inside a thread
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        ClientHandler(Socket socket) {
            this.socket = socket;
        }
        public void run() {//this is where all communication happens
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Enter your username:");
                username = in.readLine();
                if (username == null || username.isBlank()) {
                    username = "Anonymous-" + socket.getPort();
                }
                System.out.println(username + " connected from " + socket.getInetAddress());
                broadcast(username + " has joined the chat.", this);
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    broadcast(username + ": " + message, this);
                }
            } catch (IOException e) {
                System.out.println("Connection error with " + username + ": " + e.getMessage());
            } finally {
                closeConnection();
                removeClient(this);
            }
        }
        void send(String message) {
            out.println(message);
        }
        private void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
/*Server starts
      ↓
Create ServerSocket
      ↓
accept()
      ↓
Client connects
      ↓
Create ClientHandler
      ↓
Add to clients set
      ↓
Run in thread pool
      ↓
Ask username
      ↓
Read messages
      ↓
Broadcast to everyone
      ↓
Client quits
      ↓
Remove from set
      ↓
Close socket
*/
