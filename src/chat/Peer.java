package chat;

import java.io.DataOutputStream;
import java.net.Socket;

public class Peer {
    private String nickname;
    private final String address;
    private final int port;
    private DataOutputStream out;

    public Peer(String nickname, String address, int port) {
        this.nickname = nickname;
        this.address = address;
        this.port = port;
    }

    public void initStream(Socket socket) throws Exception {
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public synchronized void send(ProtocolMessage m) throws Exception {
        if (out == null) return;
        out.writeInt(m.getType().ordinal()); // 1. Тип сообщения
        out.writeUTF(m.getSender());         // 2. Имя отправителя
        out.writeUTF(m.getIp());             // 3. IP
        out.writeInt(m.getPort());           // 4. Порт
        out.writeUTF(m.getText());           // 5. Текст сообщения
        out.flush();
    }

    public String getIdentifier() { return address + ":" + port; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAddress() { return address; }
    public int getPort() { return port; }
}