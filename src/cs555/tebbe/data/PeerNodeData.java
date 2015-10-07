package cs555.tebbe.data;

import cs555.tebbe.transport.NodeConnection;

/**
 * Created by ctebbe
 */
public class PeerNodeData {

    public final NodeConnection connection;
    public final String ID;

    public PeerNodeData(NodeConnection connection, String id) {
        this.connection = connection;
        ID = id;
    }

    public String toString() {
        return "\t" + connection.getRemoteKey() + ID;
    }
}
