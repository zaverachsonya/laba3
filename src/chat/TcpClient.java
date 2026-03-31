package chat;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClient {
    public static void connect(ChatManager manager, String host, int port, String name) {
        try {
            Socket s = new Socket();
            s.bind(new InetSocketAddress(manager.getMyIp(), 0));
            s.connect(new InetSocketAddress(host, port), 1500);
            Peer p = new Peer(name, host, port);
            p.initStream(s);
            manager.addActivePeer(p);
            new Thread(new ClientHandler(manager, s, p)).start();
            manager.processIncoming(new ProtocolMessage(MessageType.SYSTEM_JOIN, "", p.getNickname(), p.getAddress(), p.getPort()));
            manager.transmit(p, new ProtocolMessage(MessageType.HELLO, "", manager.getMyName(), manager.getMyIp(), manager.getMyPort()));
        } catch (Exception ignored) {}
    }
}