package course.java.tcp.chatserver;

import course.java.tcp.server.TcpTimeServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class Handler implements Runnable {
    private ChatServerTcp server;
    private final Socket  socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public Handler(ChatServerTcp server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            String message = "";
            // chat application protocol: 1) read nickname as first message
            nickname = in.readLine();
            log.info("User {} logged in.", nickname);

            // 2) Read chat messages until <END> message is received
            var finished = false;
            while(!finished && !Thread.interrupted()) {
                // read request
                message = in.readLine();
                if (message.equals("<END>")) {
                    finished = true;
                    continue;
                }
                server.sendToAll(message);
            }
            // 3) Close session when <END> is received
            log.info("Closing session for user: {}", nickname);
            socket.close();
        } catch (IOException e) {
            log.error("Error reading/writing from/to TCP socket:", e);
            throw new RuntimeException(e);
        }
    }
}

@Slf4j
public class ChatServerTcp implements Runnable {
    public static final int PORT = 9090;

    private volatile boolean canceled = false;

    private ExecutorService executor = Executors.newCachedThreadPool();

    public void cancel() {
        this.canceled = true;
    }

    @Override
    public void run() {
        try(ServerSocket ssoc = new ServerSocket(PORT, -1,
                InetAddress.getByAddress(new byte[] {127, 0, 0, 1}))) {
            log.info("Chat Server is listening for connections on: {}", ssoc);
            while(!canceled && !Thread.interrupted()) {
                try(Socket s = ssoc.accept()) {
                    log.info("Time Server connection accepted: {}", s);
                    executor.execute(new Handler(this, s));
                }
            }
            log.info("Closing Chat Server.");
        } catch (IOException e) {
            log.error("Error running Chat Server:", e);
            throw new RuntimeException(e);
        } finally {
            executor.shutdownNow();
        }
    }

    public void sendToAll(String message) {
    }

    public static void main(String[] args) {
        new TcpTimeServer().run();
    }


}
