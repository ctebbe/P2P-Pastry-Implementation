package cs555.tebbe.transport;
import cs555.tebbe.node.*;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;

/*
    opens an input stream to accept incoming events on a separate thread
*/
public class TCPReceiverThread extends Thread {

    private Node node;
    private Socket socket;
    private DataInputStream dInputStream;
    private final String key;

    public TCPReceiverThread(Node node, Socket socket) throws IOException {
        this.node = node;
        this.socket = socket;
        key = socket.getRemoteSocketAddress().toString().substring(1);
        dInputStream = new DataInputStream(socket.getInputStream());
    }

    /*
     * read all data coming in over the pipe
     * */
    private byte[] receiveData() throws IOException, SocketException {
        int dataLen = dInputStream.readInt();
        byte[] data = new byte[dataLen];
        dInputStream.readFully(data, 0, dataLen);
        return data;
    }

    /*
     * accept events until an exception or the connection is closed
     * */
    public void run() {
        //System.out.println("* Receiver thread ready to accept events.");
        while(socket != null) {
            try {
                node.onEvent(EventFactory.getInstance().buildEvent(receiveData()));
            } catch(SocketException se) {
                //System.out.println("Socket error in receiver thread:"+se.getMessage());
                break;
            } catch(IOException ioe) {
                node.lostConnection(key);
                break;
            }
        }
        closeConnection();
    }

    private void closeConnection() {
        try {
            dInputStream.close();
            socket.close();
        } catch(IOException ioe) {
            System.out.println("Closing connection:" + ioe.getMessage());
        }
    }
}
