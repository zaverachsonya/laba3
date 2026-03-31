package chat;
import java.util.Map;
import java.util.concurrent.*;

public class ChatManager {
    private final String myName;
    private final String myIp;
    private final int myPort;
    private final Map<String, Peer> activePeers = new ConcurrentHashMap<>();
    private final HistoryManager history = new HistoryManager();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private boolean isStopping = false;
    private boolean historyReceived = false;

    public ChatManager(String name, String ip, int port) {
        this.myName = name;
        this.myIp = ip;
        this.myPort = port;
    }

    public void start(int udp) {
        pool.execute(new TcpServer(this, myPort));
        pool.execute(new UdpDiscoveryService(this, udp));
    }

    public synchronized void confirmPeer(Peer p, ProtocolMessage m) {
        String realId = m.getIp() + ":" + m.getPort();
        if (realId.equals(myIp + ":" + myPort)) { p.close(); return; }

        if (activePeers.containsKey(realId)) {
            if (activePeers.get(realId) != p) { p.close(); return; }
        }

        if (p.getNickname().equals("Guest")) {
            p.setNickname(m.getSender());
            p.setRemoteServerPort(m.getPort());
            activePeers.put(realId, p);
            processIncoming(new ProtocolMessage(MessageType.SYSTEM_JOIN, "", p.getNickname(), m.getIp(), m.getPort()));
            transmit(p, new ProtocolMessage(MessageType.HELLO, "", myName, myIp, myPort));

            if (!historyReceived) {
                transmit(p, new ProtocolMessage(MessageType.GET_HISTORY, "", myName, myIp, myPort));
            }
        }
    }

    public void tryConnect(String ip, int port, String name) {
        String myId = myIp + ":" + myPort;
        String theirId = ip + ":" + port;
        if (!activePeers.containsKey(theirId) && myId.compareTo(theirId) < 0) {
            TcpClient.connect(this, ip, port, name);
        }
    }

    public void addActivePeer(Peer p) { activePeers.put(p.getIdentifier(), p); }

    public void sendMessage(String text) {
        ProtocolMessage m = new ProtocolMessage(MessageType.TEXT, text, myName, myIp, myPort);
        processIncoming(m);
        activePeers.values().forEach(p -> transmit(p, m));
    }

    public void transmit(Peer p, ProtocolMessage m) {
        try { p.send(m); } catch (Exception e) { removePeer(p); }
    }

    public void processIncoming(ProtocolMessage m) {
        String formatted = m.getDisplayFormat();
        history.save(formatted);
        System.out.println(formatted);
    }

    public void removePeer(Peer p) {
        if (isStopping) return;
        if (activePeers.get(p.getIdentifier()) == p) {
            if (activePeers.remove(p.getIdentifier()) != null) {
                processIncoming(new ProtocolMessage(MessageType.SYSTEM_LEAVE, "", p.getNickname(), p.getAddress(), p.getPort()));
            }
        }
    }

    public void sendHistoryTo(Peer p) {
        transmit(p, new ProtocolMessage(MessageType.SEND_HISTORY, history.exportAll(), myName, myIp, myPort));
    }

    public synchronized void applyHistory(String data) {
        if (data == null || data.trim().isEmpty() || historyReceived) return;

        this.historyReceived = true;

        System.out.println("\n----- ИСТОРИЯ ЧАТА -----");
        System.out.print(data);
        System.out.println("\n-----------------------------\n");
        history.importHistory(data);
    }

    public void stop() {
        this.isStopping = true;
        for (Peer p : activePeers.values()) {
            transmit(p, new ProtocolMessage(MessageType.SYSTEM_LEAVE, "", myName, myIp, myPort));
            p.close();
        }
        pool.shutdownNow();
    }

    public String getMyName() { return myName; }
    public String getMyIp() { return myIp; }
    public int getMyPort() { return myPort; }
}