package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;
import java.io.*;
public class Register implements Event {

    private Header header;

    public Register(int protocol, NodeConnection connection) {
        header = new Header(protocol, connection);
    }

    public Register(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        byte[] headerBytes = header.getBytes();
        dout.write(headerBytes);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public int getType() {
        return this.header.getType();
    }

    public String getSenderKey() {
        return this.header.getSenderKey();
    }
}
