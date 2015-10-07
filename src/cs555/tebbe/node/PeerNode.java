package cs555.tebbe.node;
import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.*;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18081;
    public static final String BASE_SAVE_DIR = "/tmp/ctebbe/";

    private NodeConnection _DiscoveryNode = null;
    private TCPServerThread serverThread = null;                                // listens for incoming client nodes
    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();

    private String _Identifier;
    private PeerNodeData lowLeaf = null;
    private PeerNodeData highLeaf = null;

    public PeerNode(String host, int port, String id) {
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:"+ioe.getMessage());
            System.exit(0);
        }

        try {
            _DiscoveryNode = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, port));
            _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode, id));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:"+ioe.getMessage());
            System.exit(0);
        }
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            case Protocol.REGISTER_RESP:
                try {
                    processRegisterResponse((RegisterResponse) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_REQ:
                try {
                    processJoinRequest((JoinRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_RESP:
                processJoinResponse((JoinResponse) event);
                break;
            default:
                System.out.println("Unknown event type");
        }
    }

    private void processJoinResponse(JoinResponse event) {
        if(event.lowLeafIP.isEmpty() && event.highLeafIP.isEmpty()) { // set each other as leaf set
            NodeConnection leafSetConnection = getNodeConnection(event.getHeader().getSenderKey());
            lowLeaf = new PeerNodeData(leafSetConnection, event.ID);
            highLeaf = new PeerNodeData(leafSetConnection, event.ID);
        }

        System.out.println("\n* New leaf set:");
        System.out.println("low leaf:" + lowLeaf.ID);
        System.out.println("high leaf:" + highLeaf.ID);
        System.out.println("route:");
        event.printRouteTrace();
    }

    private NodeConnection getNodeConnection(String key) {
        String IP = Util.removePort(key);
        try {
            if(connectionsMap.containsKey(IP))
                return connectionsMap.get(IP);
            return ConnectionFactory.buildConnection(this, IP, DEFAULT_SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        } return null;
    }

    private void processJoinRequest(JoinRequest event) throws IOException {

        if(lowLeaf == null && highLeaf == null) { // first connection of the overlay, no neighbors yet
            NodeConnection newNode = getNodeConnection(event.getOriginalSenderIP());
            newNode.sendEvent(EventFactory.buildJoinResponseEvent(newNode, _Identifier, lowLeaf, highLeaf, event.route));
        }

        System.out.println();
        System.out.println("*** Processing join request ***");
        System.out.println("\tLookup ID:" + event.lookupID);
        System.out.println("\tHop count:" + event.route.length);
        System.out.println();
    }

    private void processRegisterResponse(RegisterResponse event) throws IOException {
        System.out.println();
        System.out.println("Peer Node ID:" + event.assignedID);
        System.out.println("Random Peer Node to contact:" + event.nodeIP);
        System.out.println();

        _Identifier = event.assignedID;
        if(!event.nodeIP.isEmpty()) { // send join request lookup
            NodeConnection entryConnection = getNodeConnection(event.nodeIP);
            entryConnection.sendEvent(EventFactory.buildJoinRequestEvent(entryConnection, _Identifier));
        }
    }

    public void newConnectionMade(NodeConnection connection) {
        connectionsMap.put(Util.removePort(connection.getRemoteKey()), connection);
    }

    public void lostConnection(String disconnectedIP) {
        System.out.println("Lost connection to:" + disconnectedIP);
    }

    public static void main(String args[]) {
        new PeerNode(args[0], DiscoveryNode.DEFAULT_SERVER_PORT, null);
    }
}
