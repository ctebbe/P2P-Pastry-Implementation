package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ctebbe
 */
public class StoreFile implements Event {

    private final Header header;
    public final String filename;
    public final byte[] bytes;

    protected StoreFile(int protocol, NodeConnection connection, String name, byte[] bytes) {
        header = new Header(protocol, connection);
        this.filename = name;
        this.bytes = bytes;
    }

    protected StoreFile(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // filename
        int nameLen = din.readInt();
        byte[] ipBytes = new byte[nameLen];
        din.readFully(ipBytes);
        filename = new String(ipBytes);

        // bytes
        int bLen = din.readInt();
        this.bytes = new byte[bLen];
        din.readFully(this.bytes);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // filename
        byte[] fBytes = filename.getBytes();
        dout.writeInt(fBytes.length);
        dout.write(fBytes);

        // bytes
        dout.writeInt(bytes.length);
        dout.write(bytes);

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
