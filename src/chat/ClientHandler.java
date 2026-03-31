package chat;
import java.io.DataInputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final ChatManager manager;
    private final Socket socket;
    private final Peer peer;

    public ClientHandler(ChatManager manager, Socket socket, Peer peer) {
        this.manager = manager;
        this.socket = socket;
        this.peer = peer;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                int typeOrdinal = in.read();
                if (typeOrdinal == -1) break;
                int length = in.readInt();
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                String content = new String(buffer, java.nio.charset.StandardCharsets.UTF_8);
                String[] parts = content.split("\\|", 4);
                ProtocolMessage msg = new ProtocolMessage(
                        MessageType.values()[typeOrdinal], parts[3], parts[0], parts[1], Integer.parseInt(parts[2])
                );
                handle(msg);
            }
        } catch (Exception e) {
            if (!peer.isForcedClose()) manager.removePeer(peer);
        }
    }

    private void handle(ProtocolMessage m) {
        switch (m.getType()) {
            case HELLO -> manager.confirmPeer(peer, m);
            case TEXT -> manager.processIncoming(m);
            case GET_HISTORY -> manager.sendHistoryTo(peer);
            case SEND_HISTORY -> manager.applyHistory(m.getText());
            case SYSTEM_LEAVE -> manager.removePeer(peer);
        }
    }
}