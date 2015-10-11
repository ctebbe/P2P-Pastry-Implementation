package cs555.tebbe.data;

/**
 * Created by ctebbe
 */
public class PeerNodeData {

    public final String host_port;
    public final String indentifier;

    public PeerNodeData(String host_port, String identifier) {
        this.host_port = host_port;
        this.indentifier = identifier;
    }

    public String toString() {
        return "\t" + host_port + "\t" + indentifier;
    }
}
