package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.util.Util;

/**
 * Created by ctebbe
 */
public class PeerNodeRouteHandler {

    public final String _Identifier;
    private PeerNodeData lowLeaf;
    private PeerNodeData highLeaf;
    private final RoutingTable table;

    public PeerNodeRouteHandler(String id) {
        this._Identifier = id;
        table = new RoutingTable(id);
    }

    public String lookup(String lookupID) {
        if(Util.getHexDifference(lookupID, lowLeaf.indentifier) > 0 && Util.getHexDifference(highLeaf.indentifier, lookupID) > 0) { // low < lookup < high
            return _Identifier;
        } else {                                                                // find a closer node to forward to

            int distLow = Util.getAbsoluteHexDifference(lookupID, lowLeaf.indentifier);
            int distHigh = Util.getAbsoluteHexDifference(lookupID, highLeaf.indentifier);
            // find closest in routing table
            int dist = Util.getAbsoluteHexDifference(lookupID, _Identifier);

            if(dist < distLow && dist < distHigh)                             // _Id is closest node
                return _Identifier;
            else if(distLow < distHigh)                                       // low leaf closest
                return lowLeaf.indentifier;
            else if(dist == distHigh)                                         // send to higher id to break ties
                return highLeaf.indentifier;
            else //if(distHigh < distHigh)                                      // high leaf closest
                return highLeaf.indentifier;
        }
    }

    public String queryIPFromNodeID(String queryID) {
        if(lowLeaf.indentifier.equalsIgnoreCase(queryID))
            return Util.removePort(lowLeaf.host_port);
        else if(highLeaf.indentifier.equals(queryID))
            return Util.removePort(highLeaf.host_port);
        // check routing table..
        return null;
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
