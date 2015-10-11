package cs555.tebbe.node;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.transport.TCPServerThread;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18080;

    private TCPServerThread serverThread = null;                            // listens for incoming connections
    private ConcurrentHashMap<String, NodeConnection> bufferMap    = null;  // buffers incoming unregistered connections
    private ConcurrentHashMap<String, PeerNodeData> peerMap    = null;    // registered peer nodes
    private Set<String> identifierSet = new HashSet<>();

    //private ConcurrentHashMap<String, LiveChunkNodeData> chunkNodeMap = null;       // holds registered chunk nodes

    public DiscoveryNode(int port) {
        try {
            bufferMap = new ConcurrentHashMap<>();
            peerMap = new ConcurrentHashMap<>();

            serverThread = new TCPServerThread(this, new ServerSocket(port));
            serverThread.start();

            run();
        } catch(IOException ioe) {
            display("IOException on DiscoveryNode:"+ioe.toString());
        }
    }

    private void run() {
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("list-nodes")) {
                printListNodes();
            }
            input = keyboard.nextLine();
        }
    }

    private void printListNodes() {
        List<PeerNodeData> nodes = new ArrayList<>(peerMap.values());
        for(PeerNodeData node : nodes) {
            System.out.println(node);
        }
    }

    public synchronized void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.REGISTER_REQ:
                try {
                    processRegisterRequest((RegisterRequest) event);
                } catch (IOException e) {
                    System.out.println("IOE throws processing register event.");
                    e.printStackTrace();
                }
                break;
            case Protocol.RANDOM_PEER_REQ:
                try {
                    processRandomPeerRequest((RandomPeerNodeRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_COMP:
                try {
                    processJoinComplete((NodeIDEvent) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                display("unknown event type:"+event.getType());
        }
    }

    private void processJoinComplete(NodeIDEvent event) throws IOException {
        String key = event.getHeader().getSenderKey();
        peerMap.put(key, new PeerNodeData(event.getHeader().getSenderKey(), event.nodeID));
        Log.printDiagnostic(event);

        isNodeJoining = false;
        if(reqQueue.size() > 0) processRegisterRequest(reqQueue.remove(0));
    }

    private void processRandomPeerRequest(RandomPeerNodeRequest event) throws IOException {
        NodeConnection connection = bufferMap.get(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildRandomPeerResponseEvent(connection, getRandomPeerNode()));
    }

    private List<RegisterRequest> reqQueue = new ArrayList<>();
    private boolean isNodeJoining = false;
    private void processRegisterRequest(RegisterRequest event) throws IOException {
        if(isNodeJoining) {
            reqQueue.add(event);
            return;
        }

        boolean success = !identifierSet.contains(event.getNodeIDRequest()); // check if id is taken
        if(success) {
            identifierSet.add(event.getNodeIDRequest());
            isNodeJoining = true;
        }

        NodeConnection connection = bufferMap.get(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildRegisterResponseEvent(connection, event.getNodeIDRequest(), success, getRandomPeerNode()));
    }

    private static final Random random = new Random();
    private String getRandomPeerNode() {
        if(peerMap.size() > 0) {
            List<String> keys = new ArrayList(peerMap.keySet());
            String randKey = keys.get(random.nextInt(keys.size()));
            return Util.removePort(peerMap.get(randKey).host_port);
        }
        return "";
    }

    public synchronized void newConnectionMade(NodeConnection connection) {
        bufferMap.put(connection.getRemoteKey(), connection);
    }

    public synchronized void lostConnection(String disconnectedConnectionKey) {
        System.out.println("Lost connection:" + disconnectedConnectionKey);
    }

    public void display(String str) {
        System.out.println(str);
    }

    public static void main(String args[]) {
        new DiscoveryNode(DEFAULT_SERVER_PORT);
    }
}
