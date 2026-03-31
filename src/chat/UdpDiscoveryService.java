package chat;

import java.net.*;

public class UdpDiscoveryService implements Runnable {
    private final ChatManager manager;
    private final int udpPort;

    public UdpDiscoveryService(ChatManager manager, int udpPort) {
        this.manager = manager;
        this.udpPort = udpPort;
    }

    public void announce() {
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.setBroadcast(true);
            String packet = manager.getMyName() + ";" + manager.getMyIp() + ";" + manager.getMyPort();
            byte[] data = packet.getBytes();
            DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), udpPort);
            ds.send(dp);
        } catch (Exception e) {
            System.err.println("UDP Send Error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        Thread listener = new Thread(() -> {
            try (DatagramSocket ds = new DatagramSocket(null)) {
                ds.setReuseAddress(true);
                ds.bind(new InetSocketAddress("0.0.0.0", udpPort));
                byte[] buffer = new byte[512];
                while (true) {
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    ds.receive(dp);
                    String info = new String(dp.getData(), 0, dp.getLength());
                    String[] parts = info.split(";");
                    if (parts.length == 3) {
                        String name = parts[0], ip = parts[1];
                        int port = Integer.parseInt(parts[2]);
                        boolean isMe = ip.equals(manager.getMyIp()) && port == manager.getMyPort();
                        if (!isMe) {
                            manager.tryConnect(ip, port, name);
                        }
                    }
                }
            } catch (Exception ignored) {}
        });
        listener.setDaemon(true);
        listener.start();

        while (true) {
            announce();
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}