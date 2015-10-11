package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class NodeIDEvent implements Event {

    private final Header header;
    public final String nodeID;
    public final boolean lowLeaf;

    protected NodeIDEvent(int protocol, NodeConnection connection, String nodeID) {
        header = new Header(protocol, connection);
        this.nodeID = nodeID;
        lowLeaf = false;
    }

    protected NodeIDEvent(int protocol, NodeConnection connection, String nodeID, boolean isLow) {
        header = new Header(protocol, connection);
        this.nodeID = nodeID;
        lowLeaf = isLow;
    }

    protected NodeIDEvent(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // node IP
        int ipLen = din.readInt();
        byte[] ipBytes = new byte[ipLen];
        din.readFully(ipBytes);
        nodeID = new String(ipBytes);

        lowLeaf = din.readBoolean();

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

        dout.writeBoolean(lowLeaf);

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
