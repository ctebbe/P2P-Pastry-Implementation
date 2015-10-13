package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.util.Util;

import java.util.*;

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
        int rowIndex = getRowIndex(newData.identifier);
        List<PeerNodeData> row = table.get(rowIndex);
        if(!row.contains(newData)) {
            Iterator<PeerNodeData> rowIter = row.iterator();
            while(rowIter.hasNext()) {
                PeerNodeData entry = rowIter.next();
                if(entry.identifier.charAt(rowIndex) == newData.identifier.charAt(rowIndex))
                    rowIter.remove();
            }
            row.add(newData);
        }
    }

    public PeerNodeData findClosestEntry(String queryID) {
        PeerNodeData closest = null;
        for(int i=3; i >= 0; i--) {
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

    public List<PeerNodeData> getAll() {
        List<PeerNodeData> all = new ArrayList<>();
        for(List<PeerNodeData> row : table) {
            for(PeerNodeData entry : row) {
                all.add(entry);
            }
        }
        return all;
    }
}
