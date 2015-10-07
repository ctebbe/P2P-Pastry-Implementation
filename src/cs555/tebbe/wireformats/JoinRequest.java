package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;

/**
 * Created by ctebbe
 */
public class JoinRequest implements Event {

    private final Header header;

    public final String lookupID;
    public final String[] route;

    protected JoinRequest(int protocol, NodeConnection connection, String id) {
        header = new Header(protocol, connection);
        lookupID = id;
        route = new String[]{Util.removePort(connection.getLocalKey())};
    }

    protected JoinRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // lookup ID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        lookupID = new String(idBytes);

        // route
        route = new String[din.readInt()];
        for(int i=0; i < route.length; i++) {
            int routeLen = din.readInt();
            byte[] routeBytes = new byte[routeLen];
            din.readFully(routeBytes);
            route[i] = new String(routeBytes);
        }

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // lookup ID
        byte[] lookupBytes = lookupID.getBytes();
        dout.writeInt(lookupBytes.length);
        dout.write(lookupBytes);

        // route
        dout.writeInt(route.length);
        for(int i=0; i < route.length; i++) {
            byte[] routeBytes = route[i].getBytes();
            dout.writeInt(routeBytes.length);
            dout.write(routeBytes);
        }

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

    public String getOriginalSenderIP() {
        return Util.removePort(route[0]);
    }

    public Header getHeader() {
        return header;
    }
}
