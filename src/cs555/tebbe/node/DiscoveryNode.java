package cs555.tebbe.node;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.transport.TCPServerThread;
import cs555.tebbe.wireformats.Event;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.Register;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryNode implements Node {


    private TCPServerThread serverThread = null;                            // listens for incoming connections
    private ConcurrentHashMap<String, NodeConnection> bufferMap    = null;  // buffers incoming unregistered connections

    //private ConcurrentHashMap<String, LiveChunkNodeData> chunkNodeMap = null;       // holds registered chunk nodes

    public DiscoveryNode(int port) {
        try {
            bufferMap = new ConcurrentHashMap<>();
            serverThread = new TCPServerThread(this, new ServerSocket(port));
            serverThread.start();
        } catch(IOException ioe) {
            display("IOException on DiscoveryNode:"+ioe.toString());
        }
    }

    public synchronized void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.REGISTER: // node registration
                registerChunkNode((Register) event);
                break;
            default:
                display("unknown event type:"+event.getType());
        }
    }

    private synchronized void registerChunkNode(Register event) {
        String key = event.getSenderKey();
        System.out.println("Registering Chunk Node: " + key);
        //chunkNodeMap.put(key, new LiveChunkNodeData(bufferMap.get(key)));
    }

    public synchronized void registerConnection(NodeConnection connection) {
        bufferMap.put(connection.getRemoteKey(), connection);
    }

    @Override
    public synchronized void lostConnection(String disconnectedConnectionKey) {
        /*
        if(chunkNodeMap.containsKey(disconnectedConnectionKey)) {
            try {
                chunkTracker.processDeadNode(disconnectedConnectionKey, new ArrayList<>(chunkNodeMap.keySet()));
            } catch (IOException e) {
                System.out.println("error processing dead node");
                e.printStackTrace();
            }
        }
        */
        System.out.println("Lost connection:"+disconnectedConnectionKey);
    }

    public void display(String str) {
        System.out.println(str);
    }

    public static void main(String args[]) {
        int port = 8080; // default listening port
        if(args.length > 0) port = Integer.parseInt(args[0]);
        new DiscoveryNode(port);
    }
}
