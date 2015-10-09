package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;

/**
 * Created by ctebbe
 */
public class LookupRequest { //implements Event {

    private Header header;
    private String lookupID;
    private String[] route;

    protected LookupRequest() {}
    protected LookupRequest(int protocol, NodeConnection connection, String id) {
        header = new Header(protocol, connection);
        lookupID = id;
        route = new String[]{Util.removePort(connection.getLocalKey())};
    }

    protected void parseStream(DataInputStream din) throws IOException {
        // header
        this.header = Header.parseHeader(din);

        // lookup targetNodeID
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
    }

    protected void writeBytes(DataOutputStream dout) throws IOException {
        // header
        dout.write(header.getBytes());

        // lookup targetNodeID
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
    }

    public String getLookupID() {
        return lookupID;
    }

    public String[] getRoute() {
        return route;
    }

    public int getType() {
        return this.header.getType();
    }

    public String getQueryNodeIP() {
        return Util.removePort(route[0]);
    }

    public Header getHeader() {
        return header;
    }
}
