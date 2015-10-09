package cs555.tebbe.node;

import cs555.tebbe.transport.ConnectionFactory;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.Event;
import cs555.tebbe.wireformats.EventFactory;
import cs555.tebbe.wireformats.Protocol;
import cs555.tebbe.wireformats.RandomPeerNodeResponse;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by ct.
 */
public class DataStoreNode implements Node {

    private NodeConnection _DiscoveryNode;

    public DataStoreNode(String host) {
        try {
            _DiscoveryNode = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, DiscoveryNode.DEFAULT_SERVER_PORT));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:"+ioe.getMessage());
            System.exit(0);
        }
        run();
    }

    private void run() {
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("store-file")) {
                System.out.println("File path?\n");
                String fpath = keyboard.nextLine();
                File f = new File(fpath);
                try {
                    sendStoreDataRequest(f);
                } catch (IOException e) { e.printStackTrace(); }
            }
            input = keyboard.nextLine();
        }
    }

    private void sendStoreDataRequest(File toStore) throws IOException {
        String dataID = Util.getFormattedHexID(toStore.getName().getBytes());
        System.out.println("Data targetNodeID to store:" + dataID);
        _DiscoveryNode.sendEvent(EventFactory.buildRandomPeerRequestEvent(_DiscoveryNode));
    }

    @Override
    public void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.RANDOM_PEER_RESP:
                System.out.println("* Random Peer Node for data lookup:"+((RandomPeerNodeResponse) event).nodeIP);
                break;
            default:
                System.out.println("unknown event type:"+event.getType());
        }
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
