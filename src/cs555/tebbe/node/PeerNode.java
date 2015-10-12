package cs555.tebbe.node;
import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.routing.PeerNodeRouteHandler;
import cs555.tebbe.transport.*;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18081;
    public static final String BASE_SAVE_DIR = "/tmp/ctebbe/";

    private NodeConnection _DiscoveryNode = null;
    private TCPServerThread serverThread = null;                                        // listens for incoming nodes
    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();     // buffers all current connections for reuse
    private PeerNodeRouteHandler router;                                                // maintains leafset and routing table & related logic
    private List<String> files = new ArrayList<>();
    private Log logger = new Log();                                                     // logs events and prints diagnostic messages
    private final boolean isCustomID;

    public PeerNode(String host, int port, boolean isCustomID) {
        this.isCustomID = isCustomID;
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:"+ioe.getMessage());
            System.exit(0);
        }

        try {
            _DiscoveryNode = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, port));
            String id;
            if(!isCustomID)
                id = Util.getTimestampHexID();
            else {
                Scanner keyboard = new Scanner(System.in);
                System.out.println("Peer Node ID?");
                id = keyboard.nextLine();
            }
            _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode, id));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:"+ioe.getMessage());
            ioe.printStackTrace();
            System.exit(0);
        }
        run();
    }

    private void run() {
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("state")) {
                print();
            } else if(input.contains("files")) {
                System.out.println("Stored files:");
                for(String fname : files) {
                    System.out.println("\t" + fname + "\t" + Util.getDataHexID(fname.getBytes()));
                }
            }
            input = keyboard.nextLine();
        }
    }

    private void print() {
        System.out.println("ID:");
        System.out.println(router._Identifier);
        System.out.println("Low leaf:");
        System.out.println(router.getLowLeaf().toString());
        System.out.println("High leaf:");
        System.out.println(router.getHighLeaf().toString());
        System.out.println(router.printTable());
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            case Protocol.REGISTER_ACK:
                try {
                    processRegisterResponse((RegisterAck) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_REQ:
                try {
                    processJoinRequest((JoinLookupRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_RESP:
                try {
                    processJoinResponse((JoinResponse) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.LEAFSET_UPDATE:
                try {
                    processLeafsetUpdate((NodeIDEvent) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE_REQ:
                try {
                    processFileStoreRequest((FileStoreLookupRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE:
                try {
                    processFileStore((StoreFile) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.TABLE_UPDATE:
                processRoutingTableUpdateEvent((NodeIDEvent) event);
                break;
            case Protocol.FILE_STORE_COMP: // ignore
                break;
            default:
                System.out.println("Unknown event type");
        }
    }

    private void processRoutingTableUpdateEvent(NodeIDEvent event) {
        router.updateTable(event.getHeader().getSenderKey(), event.nodeID);
        logger.printDiagnostic(router);
    }

    private void processFileStore(StoreFile event) throws IOException {
        File file = new File(BASE_SAVE_DIR + event.filename);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(event.bytes);
        fos.close();
        files.add(event.filename);
        logger.printDiagnostic(event);

        // send completion
        NodeConnection connection = getNodeConnection(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildFileStoreCompleteEvent(connection,""));
    }

    private void processFileStoreRequest(FileStoreLookupRequest event) throws IOException {
        String lookupID = router.lookup(event.getLookupID());
        if(!lookupID.equals(router._Identifier)) {                                          // re-route request to closer node
            NodeConnection forwardNode = getNodeConnection(router.queryIPFromNodeID(lookupID));
            Event fEvent = EventFactory.buildFileStoreRequestEvent(forwardNode, event, router._Identifier);
            logger.printDiagnostic((FileStoreLookupRequest) fEvent);
            forwardNode.sendEvent(fEvent);
        } else { // file is stored here
            NodeConnection respNode = getNodeConnection(event.getQueryNodeIP());
            respNode.sendEvent(EventFactory.buildFileStoreResponseEvent(respNode, event, router._Identifier));
        }
    }

    private void processLeafsetUpdate(NodeIDEvent event) throws IOException {
        if(event.lowLeaf) {
            router.setLowLeaf(new PeerNodeData(event.getHeader().getSenderKey(), event.nodeID));
        } else {
            router.setHighLeaf(new PeerNodeData(event.getHeader().getSenderKey(), event.nodeID));
        }

        // migrate any necessary files closer to new leaf
        Iterator<String> fIterator = files.iterator();
        while(fIterator.hasNext()) {
            String fname = fIterator.next();
            String fId = Util.getDataHexID(fname.getBytes());
            int dist = Util.getAbsoluteHexDifference(router._Identifier,fId);
            int distLeaf = Util.getAbsoluteHexDifference(event.nodeID,fId);
            if(distLeaf < dist || (distLeaf == dist && Util.getHexDifference(event.nodeID,router._Identifier) > 0)) { // migrate file
                File fToMigrate = new File(BASE_SAVE_DIR + fname);
                NodeConnection connection = getNodeConnection(event.getHeader().getSenderKey());
                connection.sendEvent(EventFactory.buildFileStoreEvent(connection, fToMigrate.getName(), Files.readAllBytes(fToMigrate.toPath())));
                logger.printDiagnostic(fToMigrate);
                fIterator.remove();
            }
        }
    }

    private void processJoinResponse(JoinResponse event) throws IOException {
        if(event.lowLeafIP.isEmpty() && event.highLeafIP.isEmpty()) {                                   // no leafset, set each other as leaf set
            NodeConnection leafSetConnection = getNodeConnection(event.getHeader().getSenderKey());
            router.setLowLeaf(new PeerNodeData(leafSetConnection.getRemoteKey(), event.targetNodeID));
            router.setHighLeaf(new PeerNodeData(leafSetConnection.getRemoteKey(), event.targetNodeID));
            leafSetConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(leafSetConnection, router._Identifier, false));
            leafSetConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(leafSetConnection, router._Identifier, true));
        } else if(event.lowLeafIP.equals(event.highLeafIP)) {                                           // 3rd node in, must position between them
            NodeConnection senderLeafConnection = getNodeConnection(event.getHeader().getSenderKey());
            NodeConnection otherLeafConnection = getNodeConnection(event.highLeafIP);
            String id_1 = event.targetNodeID;
            String id_2 = event.highLeafIdentifier;

            boolean isNotMiddleNode = (Util.getHexDifference(router._Identifier, id_1) > 0 && Util.getHexDifference(router._Identifier, id_2) > 0) ||
                    (Util.getHexDifference(router._Identifier, id_1) < 0 && Util.getHexDifference(router._Identifier, id_2) < 0);
            boolean isSenderLowLeaf = Util.getHexDifference(id_1, id_2) < 0; // id1 < id2
            if(!isNotMiddleNode)
                isSenderLowLeaf = !isSenderLowLeaf; // if this is the middle node, make it sender low leaf instead

            if(isSenderLowLeaf) {
                router.setHighLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), id_1));
                router.setLowLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), id_2));
            } else {
                router.setLowLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), id_1));
                router.setHighLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), id_2));
            }
            senderLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(senderLeafConnection, router._Identifier, isSenderLowLeaf));
            otherLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(otherLeafConnection, router._Identifier, !isSenderLowLeaf));

        } else {                                                                                        // lookup result
            boolean isSenderLowLeaf = Util.getHexDifference(event.targetNodeID, router._Identifier) > 0;
            NodeConnection senderLeafConnection = getNodeConnection(event.getHeader().getSenderKey());
            NodeConnection otherLeafConnection;
            if(isSenderLowLeaf) {
                otherLeafConnection = getNodeConnection(event.lowLeafIP);
                router.setHighLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), event.targetNodeID));
                router.setLowLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), event.lowLeafIdentifier));
            } else {
                otherLeafConnection = getNodeConnection(event.highLeafIP);
                router.setLowLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), event.targetNodeID));
                router.setHighLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), event.highLeafIdentifier));
            }

            senderLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(senderLeafConnection, router._Identifier, isSenderLowLeaf));
            senderLeafConnection.sendEvent(EventFactory.buildRouteTableUpdateEvent(senderLeafConnection, router._Identifier));

            otherLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(otherLeafConnection, router._Identifier, !isSenderLowLeaf));
            otherLeafConnection.sendEvent(EventFactory.buildRouteTableUpdateEvent(otherLeafConnection, router._Identifier));

            // update routing table of all nodes on route
            String[] route = event.route;
            for(int i=1; i < route.length; i++) {
                NodeConnection connection = getNodeConnection(route[i].split("\\t")[0]);
                if(!(connection.getRemoteKey().equals(senderLeafConnection.getRemoteKey()) ||
                        connection.getRemoteKey().equals(otherLeafConnection.getRemoteKey()))) // avoid double-sending updates
                    connection.sendEvent(EventFactory.buildRouteTableUpdateEvent(connection, router._Identifier));
            }
            router.updateTableEntries(event.table);
            logger.printDiagnostic(router);
        }
        logger.printDiagnostic(event.route);
        _DiscoveryNode.sendEvent(EventFactory.buildJoinCompleteEvent(_DiscoveryNode, router._Identifier));
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

    private void processJoinRequest(JoinLookupRequest event) throws IOException {

        boolean sendJoinResponse = false;
        if(router.getLowLeaf() == null && router.getHighLeaf() == null) {                       // first connection of the overlay, no neighbors yet
            sendJoinResponse = true;
        } else if(router.getLowLeaf().identifier.equals(router.getHighLeaf().identifier)) {   // currently two nodes in overlay, joining node is in this leafset
            sendJoinResponse = true;
        } else {                                                                                // decide if node is in this leafset or should be re-routed
            String closestID = router.lookup(event.getLookupID());
            if(!closestID.equals(router._Identifier)) {                                          // re-route request to closer node
                NodeConnection forwardNode = getNodeConnection(router.queryIPFromNodeID(closestID));
                Event fEvent = EventFactory.buildJoinRequestEvent(forwardNode, event, router._Identifier, router.findRow(event.getLookupID()));
                logger.printDiagnostic((JoinLookupRequest) fEvent);
                forwardNode.sendEvent(fEvent);
            } else
                sendJoinResponse = true;
        }

        if(sendJoinResponse) {                                                                  // send leafset to the query node
            NodeConnection newNode = getNodeConnection(event.getQueryNodeIP());
            newNode.sendEvent(EventFactory.buildJoinResponseEvent(newNode, router._Identifier,
                    router.getLowLeaf(), router.getHighLeaf(), event.getRoute(), event.routingTable));
        }
    }

    private void processRegisterResponse(RegisterAck event) throws IOException {
        if(event.success) { // claimed and received ID
            logger.printDiagnostic(event);
            router = new PeerNodeRouteHandler(event.assignedID);
            if(!event.randomNodeIP.isEmpty()) { // send join request lookup
                NodeConnection entryConnection = getNodeConnection(event.randomNodeIP);
                entryConnection.sendEvent(EventFactory.buildJoinRequestEvent(entryConnection, router._Identifier));
            } else {
                _DiscoveryNode.sendEvent(EventFactory.buildJoinCompleteEvent(_DiscoveryNode, event.assignedID));
            }
        } else {
            if(!isCustomID)
                _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode, Util.getTimestampHexID()));
        }
    }

    public void newConnectionMade(NodeConnection connection) {
        connectionsMap.put(Util.removePort(connection.getRemoteKey()), connection);
    }

    public void lostConnection(String disconnectedIP) {
        System.out.println("Lost connection to:" + disconnectedIP);
    }

    public static void main(String args[]) {
        new PeerNode(args[0], DiscoveryNode.DEFAULT_SERVER_PORT, args.length > 1);
    }
}
