package cs555.tebbe.node;

import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.transport.ConnectionFactory;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.transport.TCPServerThread;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct.
 */
public class DataStoreNode implements Node {

    private NodeConnection _DiscoveryNode;
    private TCPServerThread serverThread = null;                                        // listens for incoming nodes
    private static final Log logger = new Log();

    public DataStoreNode(String host) {
        try {
            _DiscoveryNode = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, DiscoveryNode.DEFAULT_SERVER_PORT));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:"+ioe.getMessage());
            System.exit(0);
        }
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(PeerNode.DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:"+ioe.getMessage());
            System.exit(0);
        }
        run();
    }

    private void run() {
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("store")) {
                System.out.println("File path?");
                String fpath = keyboard.nextLine();
                File f = new File(fpath);
                try {
                    sendStoreDataRequest(f);
                } catch (IOException e) { e.printStackTrace(); }
            }
            input = keyboard.nextLine();
        }
    }

    private File cacheFile;
    private void sendStoreDataRequest(File toStore) throws IOException {
        cacheFile = toStore;
        _DiscoveryNode.sendEvent(EventFactory.buildRandomPeerRequestEvent(_DiscoveryNode));
    }

    @Override
    public void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.RANDOM_PEER_RESP:
                try {
                    sendLookupQuery((RandomPeerNodeResponse) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE_RESP:
                try {
                    processFileLookupResponse((FileStoreLookupResponse) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE_COMP:
                System.out.println("File storage successful.");
                break;
            default:
                System.out.println("unknown event type:"+event.getType());
        }
    }

    private void processFileLookupResponse(FileStoreLookupResponse event) throws IOException {
        logger.printDiagnostic(event);
        NodeConnection connection = getNodeConnection(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildFileStoreEvent(connection, cacheFile.getName(), Files.readAllBytes(cacheFile.toPath())));
    }

    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();
    private NodeConnection getNodeConnection(String key) {
        String IP = Util.removePort(key);
        try {
            if(connectionsMap.containsKey(IP))
                return connectionsMap.get(IP);
            return ConnectionFactory.buildConnection(this, IP, PeerNode.DEFAULT_SERVER_PORT);

        } catch (IOException e) {
            e.printStackTrace();
        } return null;
    }

    private void sendLookupQuery(RandomPeerNodeResponse event) throws IOException {
        String id = Util.getDataHexID(cacheFile.getName().getBytes());
        logger.printDiagnostic(event, id);
        NodeConnection entry = getNodeConnection(event.nodeIP);
        entry.sendEvent(EventFactory.buildFileStoreRequestEvent(entry, id, ""));
    }

    @Override
    public void newConnectionMade(NodeConnection connection) {

    }

    @Override
    public void lostConnection(String disconnectedKey) {

    }

    public static void main(String[] args) {
        new DataStoreNode(args[0]);
    }
}
