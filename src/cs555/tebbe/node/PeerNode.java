package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18080;
    public static final String BASE_SAVE_DIR = "/tmp/ctebbe/";

    private NodeConnection _DiscoveryNode = null;
    private TCPServerThread serverThread = null;                                // listens for incoming client nodes
    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();

    public PeerNode(String host, int port) {
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:"+ioe.getMessage());
            System.exit(0);
        }

        try {
            _DiscoveryNode = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, port));
            _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:"+ioe.getMessage());
            System.exit(0);
        }
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            default: ;
        }
    }

    public void newConnectionMade(NodeConnection connection) {
        System.out.println("New connection: " + connection.getRemoteKey());
        connectionsMap.put(connection.getRemoteKey(), connection);
    }

    @Override
    public void lostConnection(String disconnectedIP) {
        System.out.println("Lost connection to:" + disconnectedIP);
    }

    public static void main(String args[]) {
        if(args.length > 0) {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            new PeerNode(host,port);
        } else {
            System.out.println("Usage: java PeerNode controller_host controller_port");
        }
    }
}
