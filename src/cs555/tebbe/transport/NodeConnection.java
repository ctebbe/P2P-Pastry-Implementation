package cs555.tebbe.transport;
import cs555.tebbe.node.*;
import cs555.tebbe.wireformats.*;
import java.io.*;
import java.net.*;

/*
 * @author ct
 * represents a single connection to send and receive events
 * */
public class NodeConnection {

    private final Node node;
    private final Socket socket;
    private final TCPSender sender;
    private final TCPReceiverThread receiver;

    public NodeConnection(Node node, Socket sock) throws IOException {
        this.node = node;
        this.socket = sock;
        this.sender = new TCPSender(sock);
        this.receiver = new TCPReceiverThread(node, sock);

        node.newConnectionMade(this);
        receiver.start();
    }

    public synchronized void sendEvent(Event event) {
        try { 
            this.sender.sendEvent(event);
        } catch(IOException ioe) { 
            //System.out.println("Error sending event:"+ioe.toString());
        }
    }

    public String getLocalKey() {
        String key = socket.getLocalSocketAddress().toString();
        return key.substring(key.indexOf('/')+1);
    }

    public String getRemoteKey() {
        String key = socket.getRemoteSocketAddress().toString();
        return key.substring(key.indexOf('/')+1);
    }
}
