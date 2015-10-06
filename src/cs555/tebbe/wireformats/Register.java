package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;
import java.io.*;
public class Register extends Event {

    public Register(int protocol, NodeConnection connection) {
        super(protocol, connection);
    }

    public Register(DataInputStream din) throws IOException {
        super(din);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(getHeader().getBytes());

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }
}
