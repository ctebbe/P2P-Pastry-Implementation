package cs555.tebbe.data;

/**
 * Created by ctebbe
 */
public class PeerNodeData {

    public final String host_port;
    public final String identifier;

    public PeerNodeData(String host_port, String identifier) {
        this.host_port = host_port;
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object object) {
        if(object == null || object.getClass() != getClass()) {
            return false;
        } else {
            PeerNodeData other = (PeerNodeData) object;
            return identifier.equals(other.identifier);
        }
    }

    public String toString() {
        return "\t" + host_port + "\t" + identifier;
    }
}
