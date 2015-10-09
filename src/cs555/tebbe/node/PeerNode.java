package cs555.tebbe.node;
import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Logger;
import cs555.tebbe.routing.PeerNodeRouteHandler;
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
    private TCPServerThread serverThread = null;                                        // listens for incoming nodes
    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();     // buffers all current connections for reuse
    private PeerNodeRouteHandler router;                                                // maintains leafset and routing table & related logic
    private Logger logger = new Logger();                                               // logs events and prints diagnostic messages

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
        if(event.lowLeafIP.isEmpty() && event.highLeafIP.isEmpty()) { // no leafset, set each other as leaf set
            NodeConnection leafSetConnection = getNodeConnection(event.getHeader().getSenderKey());
            router.lowLeaf = new PeerNodeData(leafSetConnection, event.targetNodeID);
            router.highLeaf = new PeerNodeData(leafSetConnection, event.targetNodeID);
        }

        System.out.println("\n* New leaf set:");
        System.out.println("low leaf:" + router.lowLeaf.ID);
        System.out.println("high leaf:" + router.highLeaf.ID);
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

        boolean sendJoinResponse = false;
        if(router.lowLeaf == null && router.highLeaf == null) {                     // first connection of the overlay, no neighbors yet
            sendJoinResponse = true;
        } else if(router.lowLeaf.ID.equals(router.highLeaf.ID)) {                   // currently two nodes in overlay, joining node is in this leafset
            sendJoinResponse = true;
        }

        if(sendJoinResponse) { // send leafset to the query node
            NodeConnection newNode = getNodeConnection(event.getQueryNodeIP());
            newNode.sendEvent(EventFactory.buildJoinResponseEvent(newNode, router._Identifier, router.lowLeaf, router.highLeaf, event.getRoute()));
        }

        System.out.println();
        System.out.println("*** Processing join request ***");
        System.out.println("\tLookup targetNodeID:" + event.getLookupID());
        System.out.println("\tHop count:" + event.getRoute().length);
        System.out.println();
    }

    private void processRegisterResponse(RegisterResponse event) throws IOException {
        System.out.println();
        System.out.println("Peer Node targetNodeID:" + event.assignedID);
        System.out.println("Random Peer Node to contact:" + event.randomNodeIP);
        System.out.println();
        router = new PeerNodeRouteHandler(event.assignedID);

        if(!event.randomNodeIP.isEmpty()) { // send join request lookup
            NodeConnection entryConnection = getNodeConnection(event.randomNodeIP);
            entryConnection.sendEvent(EventFactory.buildJoinRequestEvent(entryConnection, router._Identifier));
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
