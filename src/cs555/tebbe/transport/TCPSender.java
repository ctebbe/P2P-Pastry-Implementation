package cs555.tebbe.transport;
import cs555.tebbe.wireformats.*;
import java.io.*;
import java.net.*;

/*
    opens an output stream to push events over
 */
public class TCPSender {

    private final DataOutputStream dataOutputStream;

    public TCPSender(Socket socket) throws IOException {
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void sendEvent(Event event) throws IOException {
        byte[] toSend = event.getBytes();
        int len = toSend.length;
        dataOutputStream.writeInt(len);
        dataOutputStream.write(toSend, 0, len);
        dataOutputStream.flush();
    }
}
