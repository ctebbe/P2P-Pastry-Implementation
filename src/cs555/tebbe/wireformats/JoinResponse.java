package cs555.tebbe.wireformats;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;

/**
 * Created by ctebbe
 */
public class JoinResponse implements Event {

    private final Header header;

    public final String targetNodeID;

    public final String lowLeafIP;
    public final String lowLeafIdentifier;

    public final String highLeafIP;
    public final String highLeafIdentifier;

    public final String[] route;

    protected JoinResponse(int protocol, NodeConnection connection, String ID, PeerNodeData lowLeaf, PeerNodeData highLeaf, String[] prevRoute) {
        header = new Header(protocol, connection);

        this.targetNodeID = ID;

        if(lowLeaf == null) {
            this.lowLeafIP = "";
            this.lowLeafIdentifier = "";
        } else {
            this.lowLeafIP = Util.removePort(lowLeaf.connection.getRemoteKey());
            this.lowLeafIdentifier = lowLeaf.ID;
        }

        if(highLeaf == null) {
            this.highLeafIP = "";
            this.highLeafIdentifier = "";
        } else {
            this.highLeafIP = Util.removePort(highLeaf.connection.getRemoteKey());
            this.highLeafIdentifier = highLeaf.ID;
        }

        route = new String[prevRoute.length+1];
        for(int i=0; i < prevRoute.length; i++)
            route[i] = prevRoute[i];
        route[route.length-1] = Util.removePort(connection.getLocalKey());
    }

    protected JoinResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // targetNodeID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        targetNodeID = new String(idBytes);

        // lowLeafIP IP
        int lowLen = din.readInt();
        byte[] lowBytes = new byte[lowLen];
        din.readFully(lowBytes);
        lowLeafIP = new String(lowBytes);

        // lowLeafIP targetNodeID
        int lowIDLen = din.readInt();
        byte[] lowIDBytes = new byte[lowIDLen];
        din.readFully(lowIDBytes);
        lowLeafIdentifier = new String(lowIDBytes);

        // highLeafIP IP
        int highLen = din.readInt();
        byte[] highBytes = new byte[highLen];
        din.readFully(highBytes);
        highLeafIP = new String(highBytes);

        // highLeafIP targetNodeID
        int highIDLen = din.readInt();
        byte[] highIDBytes = new byte[highIDLen];
        din.readFully(highIDBytes);
        highLeafIdentifier = new String(highIDBytes);

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

        // targetNodeID
        byte[] idBytes = targetNodeID.getBytes();
        dout.writeInt(idBytes.length);
        dout.write(idBytes);

        // lowLeafIP
        byte[] lowBytes = lowLeafIP.getBytes();
        dout.writeInt(lowBytes.length);
        dout.write(lowBytes);

        // lowLeafID
        byte[] lowIDBytes = lowLeafIdentifier.getBytes();
        dout.writeInt(lowIDBytes.length);
        dout.write(lowIDBytes);

        // highLeafIP
        byte[] highBytes = highLeafIP.getBytes();
        dout.writeInt(highBytes.length);
        dout.write(highBytes);

        // highLeafID
        byte[] highIDBytes = highLeafIdentifier.getBytes();
        dout.writeInt(highIDBytes.length);
        dout.write(highIDBytes);

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

    public Header getHeader() {
        return header;
    }

    public void printRouteTrace() {
        for(int i=0; i < route.length; i++) {
            System.out.println("\t" + route[i]);
        }
    }
}
