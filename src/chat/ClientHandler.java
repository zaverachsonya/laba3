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
                int typeOrdinal = in.readInt();
                String sender = in.readUTF();
                String ip = in.readUTF();
                int port = in.readInt();
                String text = in.readUTF();

                ProtocolMessage msg = new ProtocolMessage(
                        MessageType.values()[typeOrdinal],
                        text, sender, ip, port
                );
                handle(msg);
            }
        } catch (Exception e) {
            manager.removePeer(peer);
        }
    }

    private void handle(ProtocolMessage m) {
        switch (m.getType()) {
            case HELLO -> manager.confirmPeer(peer, m);
            case TEXT -> manager.processIncoming(m);
            case GET_HISTORY -> manager.sendHistoryTo(peer);
            case SEND_HISTORY -> manager.applyHistory(m.getText());
        }
    }
}