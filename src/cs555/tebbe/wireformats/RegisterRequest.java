package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;
import java.io.*;
public class RegisterRequest implements Event {

    private final Header header;
    private final String nodeIdentifierRequest;

    public String getNodeIDRequest() {
        return nodeIdentifierRequest;
    }

    protected RegisterRequest(int protocol, NodeConnection connection, String id) {
        header = new Header(protocol, connection);
        if(id==null) nodeIdentifierRequest = "";
        else  nodeIdentifierRequest = id;
    }

    protected RegisterRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // targetNodeID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        nodeIdentifierRequest = new String(idBytes);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // targetNodeID
        byte[] idBytes = nodeIdentifierRequest.getBytes();
        dout.writeInt(idBytes.length);
        dout.write(idBytes);

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
