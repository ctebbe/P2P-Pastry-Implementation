package cs555.tebbe.transport;
import cs555.tebbe.node.*;
import cs555.tebbe.util.*;
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

        node.registerConnection(this);
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
        return socket.getLocalSocketAddress().toString().substring(1);
    }

    public String getRemoteKey() {
        return socket.getRemoteSocketAddress().toString().substring(1);
    }

    public boolean equals(Object o) {
        if(o == this) return true;
        if(!(o instanceof NodeConnection)) return false;
        NodeConnection nc = (NodeConnection) o;
        System.out.println("this: "+this.getRemoteKey());
        System.out.println("other: "+nc.getRemoteKey());
        return this.getRemoteKey().equals(nc.getRemoteKey());
    }
}
