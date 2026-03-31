package chat;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) {
        String nodeName = "Peer";
        String hostAddr = "127.0.0.1";
        int serverPort = 11000;
        int broadcastPort = 12121;

        for (String param : args) {
            if (param.startsWith("--name=")) nodeName = param.substring(7);
            if (param.startsWith("--ip=")) hostAddr = param.substring(5);
            if (param.startsWith("--tcp-port=")) serverPort = Integer.parseInt(param.substring(11));
            if (param.startsWith("--udp-port=")) broadcastPort = Integer.parseInt(param.substring(11));
        }
        if (!isPortFree(hostAddr, serverPort)) {
            System.err.println("Fatal: Address " + hostAddr + ":" + serverPort + " is already occupied!");
            System.exit(1);
        }
        System.out.println("\nInitializing Node");
        System.out.println("Identity: " + nodeName);
        System.out.println("Host IP: " + hostAddr);
        System.out.println("TCP Port: " + serverPort);
        System.out.println("UDP Port: " + broadcastPort);
        System.out.println("\n");
        ChatManager manager = new ChatManager(nodeName, hostAddr, serverPort);
        manager.start(broadcastPort);
        manager.processIncoming(new ProtocolMessage(MessageType.SYSTEM_JOIN, "", nodeName, hostAddr, serverPort));
        java.util.Scanner input = new java.util.Scanner(System.in);
        while (input.hasNextLine()) {
            String text = input.nextLine();

            if (text.equalsIgnoreCase("/exit")) {
                manager.stop();
                break;
            }
            if (!text.isEmpty()) manager.sendMessage(text);
        }
        System.exit(0);}

    private static boolean isPortFree(String ip, int port) {
        try (ServerSocket ss = new ServerSocket()) {
            ss.bind(new InetSocketAddress(ip, port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}