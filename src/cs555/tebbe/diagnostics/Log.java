package cs555.tebbe.diagnostics;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.wireformats.JoinComplete;
import cs555.tebbe.wireformats.RegisterAck;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Created by ctebbe
 */
public class Log {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void printDiagnostic(JoinComplete event) {
        print("New PeerNode joined");
        print(event.getHeader().getSenderKey());
        print(event.nodeID);
    }

    public static void print(String payload) {
        LOGGER.log(Level.INFO, payload);
    }

    public static void printDiagnostic(PeerNodeData oldLeaf, PeerNodeData newLeaf) {
        print("Leafset update");
        print("Old leaf:"+oldLeaf.toString());
        print("New leaf:" + newLeaf.toString());
    }

    public void printDiagnostic(RegisterAck event) {
        print("Assigned ID:"+event.assignedID);
        print("Random node to contact:"+event.randomNodeIP);
    }
}
