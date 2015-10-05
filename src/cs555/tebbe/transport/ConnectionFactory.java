package cs555.tebbe.transport;
import cs555.tebbe.node.*;
import java.io.*;
import java.net.*;
public class ConnectionFactory {

    protected ConnectionFactory() {}

    private static ConnectionFactory factory = null;
    public static ConnectionFactory getInstance() {
        if(factory == null) factory = new ConnectionFactory();
        return factory;
    }

    public static NodeConnection buildConnection(Node node, Socket sock) throws IOException {
        return new NodeConnection(node, sock);
    }
    public static NodeConnection buildConnection(Node node, String host, int port) throws IOException {
        return buildConnection(node, new Socket(host, port));
    }
}
