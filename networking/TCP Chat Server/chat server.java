import java.io.*;//input/output classes
import java.net.*;//networking ops
public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";//my comp ka ip 
    private static final int SERVER_PORT = 5000;//listening port
    public static void main(String[] args) throws IOException {//networking ops can fail
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {//tcp handshake(->syn)(syn+ack<-)(->ack)
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//socket.getInputStream() gets raw bytes coming from the server. Then those bytes become chars using InputStreamReader and finally buffering is added to use ops like readLine etc.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);//to send data. True means auto flush and w/o that msgs might stay in mem.
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            //Thread to continuously listen for incoming messages from the server
            Thread listener = new Thread(() -> {
                String serverMessage;//stores incoming msgs
                try {
                    while ((serverMessage = in.readLine()) != null) {//keeps reading forever until server closes connection
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {//handing disconnect
                    System.out.println("Disconnected from server.");
                }
            });
            //Daemon thread is a bg thread and stops when main thread exits. Without this prg will keep running coz listener thread is still alive
            listener.setDaemon(true);
            listener.start();
            // Main thread reads from the console and sends to the server
            String userInput;
            while ((userInput = consoleIn.readLine()) != null) {//waits for keyboard input
                out.println(userInput);
                if (userInput.equalsIgnoreCase("/quit")) {//quit cmd
                    break;
                }
            }
        }
    }
}
