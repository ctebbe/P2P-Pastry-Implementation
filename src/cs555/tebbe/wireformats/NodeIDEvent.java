package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ct.
 */
public class NodeIDEvent implements Event {

    private final Header header;
    public final String nodeID;
    public final String IP;
    public final boolean lowLeaf;

    protected NodeIDEvent(int protocol, NodeConnection connection, String nodeID) {
        header = new Header(protocol, connection);
        this.nodeID = nodeID;
        lowLeaf = false;
        IP = "";
    }

    protected NodeIDEvent(int protocol, NodeConnection connection, String nodeID, boolean isLow, String ip) {
        header = new Header(protocol, connection);
        this.nodeID = nodeID;
        lowLeaf = isLow;
        IP = ip;
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
        IP = new String(ipBytes);

        // node ID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        nodeID = new String(idBytes);

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
        byte[] ipBytes = IP.getBytes();
        dout.writeInt(ipBytes.length);
        dout.write(ipBytes);

        // ID
        byte[] idBytes = nodeID.getBytes();
        dout.writeInt(idBytes.length);
        dout.write(idBytes);

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
