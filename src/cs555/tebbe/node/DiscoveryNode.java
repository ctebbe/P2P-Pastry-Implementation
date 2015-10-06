package cs555.tebbe.node;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.transport.TCPServerThread;
import cs555.tebbe.wireformats.Event;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.Register;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DiscoveryNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18080;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
            case Protocol.REGISTER:
                registerPeerNode((Register) event);
                break;
            default:
                display("unknown event type:"+event.getType());
        }
    }

    private void registerPeerNode(Register event) {
        String key = event.getHeader().getSenderKey();
        System.out.println("Registering Peer Node: " + key);
        //chunkNodeMap.put(key, new LiveChunkNodeData(bufferMap.get(key)));
    }

    public synchronized void newConnectionMade(NodeConnection connection) {
        bufferMap.put(connection.getRemoteKey(), connection);
    }

    @Override
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
