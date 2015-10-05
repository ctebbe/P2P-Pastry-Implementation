package cs555.tebbe.transport;
import cs555.tebbe.node.*;
import java.io.*;
import java.net.*;

/*
    accepts new connections on a separate thread
 */
public class TCPServerThread extends Thread {

    private final Node node;
    private final ServerSocket serverSocket;

    public TCPServerThread(Node node, ServerSocket ssocket) throws IOException {
        this.node = node;
        this.serverSocket = ssocket;
        display("Server Thread listening on IP:\t"+getIP());
        display("Server Thread listening on port:\t"+getPort());
    }

    public void run() {
        while(serverSocket != null) {
            try {
                ConnectionFactory.getInstance().buildConnection(node, serverSocket.accept());
            } catch(IOException ioe) { 
                display("Error accepting new node connection:"+ioe.getMessage()); 
            }
        }
    }

    private void display(String s) { 
        System.out.println(s); 
    }

    public String getIP() throws IOException {
        return serverSocket.getInetAddress().getLocalHost().toString();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
