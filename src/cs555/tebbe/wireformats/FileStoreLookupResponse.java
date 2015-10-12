package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ctebbe
 */
public class FileStoreLookupResponse extends LookupRequest implements Event {

    protected FileStoreLookupResponse(int protocol, NodeConnection connection, FileStoreLookupRequest request, String id) {
        super(protocol, connection, request.getLookupID(), request.getRoute(), id);
    }

    protected FileStoreLookupResponse(byte[] marshalledBytes) throws IOException {
        super();
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        super.parseStream(din);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        super.writeBytes(dout);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }
}
