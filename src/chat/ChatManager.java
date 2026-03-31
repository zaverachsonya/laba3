package chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatManager {
    private final String myName;
    private final String myIp;
    private final int myPort;
    private final Map<String, Peer> activePeers = new ConcurrentHashMap<>();
    private final HistoryManager history = new HistoryManager();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private boolean historyRequested = false;

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
        String realIdentifier = m.getIp() + ":" + m.getPort();
        if (activePeers.containsKey(realIdentifier) && !p.getNickname().equals("Guest")) {
            return;
        }

        if (p.getNickname().equals("Guest")) {
            p.setNickname(m.getSender());
            activePeers.put(realIdentifier, p);

            processIncoming(new ProtocolMessage(MessageType.SYSTEM_JOIN, "", p.getNickname(), m.getIp(), m.getPort()));

            transmit(p, new ProtocolMessage(MessageType.HELLO, "", myName, myIp, myPort));

            if (!historyRequested) {
                historyRequested = true;
                transmit(p, new ProtocolMessage(MessageType.GET_HISTORY, "", myName, myIp, myPort));
            }
        }
    }

    public void tryConnect(String ip, int port, String name) {
        if (!activePeers.containsKey(ip + ":" + port)) {
            TcpClient.connect(this, ip, port, name);
        }
    }

    public void addActivePeer(Peer p) {
        activePeers.put(p.getIdentifier(), p);
    }

    public void sendMessage(String text) {
        ProtocolMessage m = new ProtocolMessage(MessageType.TEXT, text, myName, myIp, myPort);
        processIncoming(m);
        activePeers.values().forEach(p -> transmit(p, m));
    }

    public void transmit(Peer p, ProtocolMessage m) {
        try {
            p.send(m);
        } catch (Exception e) {
            removePeer(p);
        }
    }

    public void processIncoming(ProtocolMessage m) {
        String formatted = m.getDisplayFormat();
        history.save(formatted);
        System.out.println(formatted);
    }

    public void removePeer(Peer p) {
        if (activePeers.remove(p.getIdentifier()) != null) {
            processIncoming(new ProtocolMessage(MessageType.SYSTEM_LEAVE, "", p.getNickname(), p.getAddress(), p.getPort()));
        }
    }

    public void sendHistoryTo(Peer p) {
        transmit(p, new ProtocolMessage(MessageType.SEND_HISTORY, history.exportAll(), myName, myIp, myPort));
    }

    public void applyHistory(String data) {
        if (data == null || data.trim().isEmpty()) return;
        System.out.println("\n----- ИСТОРИЯ ЧАТА -----");
        System.out.print(data);
        System.out.println("\n-----------------------------\n");
        history.importHistory(data);
    }

    public String getMyName() { return myName; }
    public String getMyIp() { return myIp; }
    public int getMyPort() { return myPort; }
}