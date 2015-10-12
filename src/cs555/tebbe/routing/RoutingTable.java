package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public void updateTable(PeerNodeData newData) {
        List<PeerNodeData> row = table.get(getRowIndex(newData.identifier));
        if(!row.contains(newData))
            row.add(newData);
    }

    public PeerNodeData findClosestEntry(String queryID) {
        PeerNodeData closest = null;
        for(int i=3; i >= 0; i--) { // work backwards up the table and find closest node
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

    public List<PeerNodeData> getRow(String queryID) {
        return new ArrayList<>(table.get(getRowIndex(queryID))); // get the relevant row according to the queryID
    }

    private int getRowIndex(String queryID) {
        for(int index=0; index < 4; index++) {
            if(_Id.charAt(index) == queryID.charAt(index)) continue;
            else
                return index;
        }
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int rowIndex = 0;
        for(char rowChar : _Id.toCharArray()) {
            sb.append("Row:"+rowChar+"\n");

            List<PeerNodeData> row = table.get(rowIndex++);
            Collections.sort(row, new Comparator<PeerNodeData>() {
                @Override
                public int compare(PeerNodeData o1, PeerNodeData o2) {
                    return new Integer(Integer.parseInt(o1.identifier,16)).compareTo(Integer.parseInt(o2.identifier,16));
                }
            });

            for(PeerNodeData entry : row) {
                sb.append("\t" + entry.host_port + "\t" + entry.identifier + "\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getIPFromID(String queryID) {
        for(List<PeerNodeData> row : table) {
            for(PeerNodeData entry : row) {
                if(entry.identifier.equals(queryID))
                    return entry.host_port;
            }
        }
        return null;
    }
}
