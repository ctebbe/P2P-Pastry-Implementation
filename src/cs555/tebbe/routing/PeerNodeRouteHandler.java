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
            return identifiers.get(0); // return closest node
        }
    }

    public List<PeerNodeData> findRow(String queryID) {
        return table.getRow(queryID);
    }

    public String queryIPFromNodeID(String queryID) {
        if(lowLeaf.identifier.equalsIgnoreCase(queryID))
            return Util.removePort(lowLeaf.host_port);
        else if(highLeaf.identifier.equals(queryID))
            return Util.removePort(highLeaf.host_port);
        else {
            System.out.println("* Routing from routing table");
            return table.getIPFromID(queryID);
        }
    }


    public void updateTable(String host_port, String id) {
        table.updateTable(new PeerNodeData(Util.removePort(host_port), id));
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

    public String printTable() {
        return table.toString();
    }

    public void updateTableEntries(List<List<PeerNodeData>> newEntries) {
        for(List<PeerNodeData> row : newEntries) {
            for(PeerNodeData entry : row) {
                table.updateTable(entry);
            }
        }
    }
}
