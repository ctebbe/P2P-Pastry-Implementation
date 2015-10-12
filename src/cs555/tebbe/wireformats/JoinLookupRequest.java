package cs555.tebbe.wireformats;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.NodeConnection;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ctebbe
 */
public class JoinLookupRequest extends LookupRequest implements Event {

    public final List<List<PeerNodeData>> routingTable;

    protected JoinLookupRequest(int protocol, NodeConnection connection, String lookupID, String myID) {
        super(protocol, connection, lookupID, myID);
        routingTable = new ArrayList<>(4);
        for(int i=0; i < 4; i++)
            routingTable.add(new ArrayList<PeerNodeData>());
    }

    protected JoinLookupRequest(int protocol, NodeConnection connection, String lookupID, String[] route, String myID, List<List<PeerNodeData>> table) {
        super(protocol, connection, lookupID, route, myID);
        routingTable = table;
    }

    protected JoinLookupRequest(byte[] marshalledBytes) throws IOException {
        super();
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        super.parseStream(din);

        int numRows = din.readInt(); // table size
        routingTable = new ArrayList<>();
        for(int i=0; i < numRows; i++) {
            int rowSize = din.readInt(); // row size
            List<PeerNodeData> row = new ArrayList<>();
            for(int j=0; j < rowSize; j++) {
                // host port
                int hLen = din.readInt();
                byte[] hBytes = new byte[hLen];
                din.readFully(hBytes);
                String hostport = new String(hBytes);

                // id
                int iLen = din.readInt();
                byte[] iBytes = new byte[iLen];
                din.readFully(iBytes);
                String id = new String(iBytes);

                row.add(new PeerNodeData(hostport, id));
            }
            routingTable.add(row);
        }

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        super.writeBytes(dout);

        dout.writeInt(routingTable.size()); // table size
        for(List<PeerNodeData> row : routingTable) {
            dout.writeInt(row.size()); // row size
            for(PeerNodeData entry : row) {
                // host_port
                byte[] hBytes = entry.host_port.getBytes();
                dout.writeInt(hBytes.length);
                dout.write(hBytes);

                // identifier
                byte[] iBytes = entry.identifier.getBytes();
                dout.writeInt(iBytes.length);
                dout.write(iBytes);
            }
        }

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }
}
