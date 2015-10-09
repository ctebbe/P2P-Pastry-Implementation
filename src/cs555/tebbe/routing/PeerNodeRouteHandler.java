package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.NodeConnection;

/**
 * Created by ctebbe
 */
public class PeerNodeRouteHandler {

    public final String _Identifier;
    public PeerNodeData lowLeaf;
    public PeerNodeData highLeaf;

    public PeerNodeRouteHandler(String id) {
        this._Identifier = id;
    }

    public void replaceLeaf(String id, NodeConnection connection) {

    }
}
