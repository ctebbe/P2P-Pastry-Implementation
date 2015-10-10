package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.transport.NodeConnection;

/**
 * Created by ctebbe
 */
public class PeerNodeRouteHandler {

    public final String _Identifier;
    private PeerNodeData lowLeaf;
    private PeerNodeData highLeaf;

    public PeerNodeRouteHandler(String id) {
        this._Identifier = id;
    }

    public void replaceLeaf(String id, NodeConnection connection) {

    }

    public void setLowLeaf(PeerNodeData newLeaf) {
        Log.printDiagnostic(lowLeaf, newLeaf);
        lowLeaf = newLeaf;
    }

    public void setHighLeaf(PeerNodeData newLeaf) {
        Log.printDiagnostic(highLeaf, newLeaf);
        highLeaf = newLeaf;
    }

    public PeerNodeData getLowLeaf() {
        return lowLeaf;
    }

    public PeerNodeData getHighLeaf() {
        return highLeaf;
    }
}
