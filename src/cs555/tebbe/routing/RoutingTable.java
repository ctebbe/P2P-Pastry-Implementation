package cs555.tebbe.routing;

import cs555.tebbe.data.PeerNodeData;

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

    private int getRowIndex(String hexDigit) {
        return Integer.parseInt(hexDigit, 16);
    }

    public static void main(String[] args) {
        System.out.println(Integer.parseInt("F",16));
    }
}
