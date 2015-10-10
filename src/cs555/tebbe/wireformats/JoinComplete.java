package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class JoinComplete implements Event {

    private final Header header;
    public final String nodeID;

    protected JoinComplete(int protocol, NodeConnection connection, String nodeID) {
        header = new Header(protocol, connection);
        this.nodeID = nodeID;
    }

    protected JoinComplete(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // node IP
        int ipLen = din.readInt();
        byte[] ipBytes = new byte[ipLen];
        din.readFully(ipBytes);
        nodeID = new String(ipBytes);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // IP
        byte[] ipBytes = nodeID.getBytes();
        dout.writeInt(ipBytes.length);
        dout.write(ipBytes);

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

    public Header getHeader() {
        return this.header;
    }
}
