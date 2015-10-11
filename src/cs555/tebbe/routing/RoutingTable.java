package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ctebbe
 */
public class RoutingTable {

    private final String _Id;
    private final List<List<PeerNodeData>> table = new ArrayList<>(4);

    public RoutingTable(String id) {
        this._Id = id;
        for(int i=0; i < 4; i++)
            table.add(new ArrayList<PeerNodeData>());
    }

    public List<PeerNodeData> getRow(String queryID) {
        return table.get(getRowIndex(queryID)); // get the relevant row according to the queryID
    }

    public PeerNodeData findClosestEntry(String queryID) {
        //int startRowIndex = getRowIndex(queryID);
        PeerNodeData closest = null;
        for(int i=4; i >= 0; i--) { // work backwards up the table and find closest node
            for(PeerNodeData entry : table.get(i)) {
                if(closest == null) {
                    closest = entry;
                } else {
                    int dClosest = Util.getAbsoluteHexDifference(closest.identifier, queryID);
                    int dEntry = Util.getAbsoluteHexDifference(entry.identifier, queryID);
                    if(dEntry < dClosest)
                        closest = entry;
                }
            }
        }
        return closest;
    }

    private int getRowIndex(String queryID) {
        for(int index=0; index < 4; index++) {
            if(_Id.charAt(index) == queryID.charAt(index)) continue;
            else
                return index;
        }
        return 0;
    }
}
