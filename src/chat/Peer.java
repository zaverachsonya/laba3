package chat;
import java.io.DataOutputStream;
import java.net.Socket;

public class Peer {
    private String nickname;
    private final String address;
    private int port;
    private DataOutputStream out;
    private Socket socket;
    private boolean forcedClose = false;

    public Peer(String nickname, String address, int port) {
        this.nickname = nickname;
        this.address = address;
        this.port = port;
    }

    public void initStream(Socket socket) throws Exception {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public synchronized void send(ProtocolMessage m) throws Exception {
        if (out == null) return;
        out.write(m.toBytes());
        out.flush();
    }

    public void close() {
        this.forcedClose = true;
        try {
            if (socket != null) {
                socket.shutdownInput();
                socket.close();
            }
        } catch (Exception ignored) {}
    }

    public boolean isForcedClose() { return forcedClose; }
    public void setRemoteServerPort(int port) { this.port = port; }
    public String getIdentifier() { return address + ":" + port; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAddress() { return address; }
    public int getPort() { return port; }
}