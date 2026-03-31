package chat;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Runnable {
    private final ChatManager manager;
    private final int port;

    public TcpServer(ChatManager manager, int port) {
        this.manager = manager;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(manager.getMyIp(), port));
            while (true) {
                Socket s = server.accept();
                Peer p = new Peer("Guest", s.getInetAddress().getHostAddress(), s.getPort());
                p.initStream(s);
                new Thread(new ClientHandler(manager, s, p)).start();
            }
        } catch (Exception ignored) {}
    }
}