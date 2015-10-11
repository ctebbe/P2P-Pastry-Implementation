package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public String lookup(final String lookupID) {
        if(Util.getHexDifference(lookupID, lowLeaf.identifier) > 0 && Util.getHexDifference(highLeaf.identifier, lookupID) > 0) { // low < lookup < high
            return _Identifier;
        } else {                                                                // find a closer node to forward to
            List<String> identifiers = new ArrayList<>();
            identifiers.add(_Identifier);
            identifiers.add(lowLeaf.identifier);
            identifiers.add(highLeaf.identifier);
            PeerNodeData tableEntry = table.findClosestEntry(lookupID);
            if(tableEntry != null)
                identifiers.add(tableEntry.identifier);

            Collections.sort(identifiers, new Comparator<String>() {            // sort list by closest id to lookupID
                @Override
                public int compare(String id1, String id2) {
                    final int BEFORE=-1, AFTER=1;
                    int dist1 = Util.getAbsoluteHexDifference(lookupID, id1);
                    int dist2 = Util.getAbsoluteHexDifference(lookupID, id2);
                    if(dist1 < dist2)
                        return BEFORE;
                    else if(dist2 < dist1)
                        return AFTER;
                    else { // equal distances, use larger ID
                        if(Util.getHexDifference(id1,id2) > 0) // id1 > id2
                            return BEFORE;
                        return AFTER;
                    }
                }
            });
            return identifiers.get(0);
            /*
            int distTable = Integer.MAX_VALUE;
            PeerNodeData tableEntry = table.findClosestEntry(lookupID);
            if(tableEntry != null)
                distTable = Util.getAbsoluteHexDifference(lookupID, tableEntry.identifier);
            int distLow = Util.getAbsoluteHexDifference(lookupID, lowLeaf.identifier);
            int distHigh = Util.getAbsoluteHexDifference(lookupID, highLeaf.identifier);
            int dist = Util.getAbsoluteHexDifference(lookupID, _Identifier);

            if(dist < distLow && dist < distHigh && dist < distTable) // _Id is closest node
                return _Identifier;
            else if(distLow < distHigh && distLow < distTable) // low leaf closest
                return lowLeaf.identifier;
            else if(dist == distHigh) // send to higher id to break ties
                return highLeaf.identifier;
            else //if(distHigh < distHigh)                                      // high leaf closest
                return highLeaf.identifier;
            */
        }
    }

    public String queryIPFromNodeID(String queryID) {
        if(lowLeaf.identifier.equalsIgnoreCase(queryID))
            return Util.removePort(lowLeaf.host_port);
        else if(highLeaf.identifier.equals(queryID))
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
