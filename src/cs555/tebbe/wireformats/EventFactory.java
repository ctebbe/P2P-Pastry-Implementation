package cs555.tebbe.wireformats;
import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.NodeConnection;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {

    protected EventFactory() {}
    private static EventFactory factory = null;
    public static EventFactory getInstance() {
        if(factory == null) factory = new EventFactory();
        return factory;
    }

    // REGISTER
    public static Event buildRegisterEvent(NodeConnection connection, String id) throws IOException {
        return new Register(Protocol.REGISTER, connection, id);
    }

    // REGISTER RESP
    public static Event buildRegisterResponseEvent(NodeConnection connection, String id, String node) throws IOException {
        return new RegisterResponse(Protocol.REGISTER_RESP, connection, id, node);
    }

    // JOIN REQ
    public static Event buildJoinRequestEvent(NodeConnection connection, String toLookup) throws IOException {
        return new JoinRequest(Protocol.JOIN_REQ, connection, toLookup);
    }

    // JOIN RESP
    public static Event buildJoinResponseEvent(NodeConnection connection, String ID, PeerNodeData lowLeaf, PeerNodeData highLeaf, String[] route) throws IOException {
        return new JoinResponse(Protocol.JOIN_RESP, connection, ID, lowLeaf, highLeaf, route);
    }

    // RANDOM PEER REQ
    public static Event buildRandomPeerRequestEvent(NodeConnection connection) throws IOException {
        return new RandomPeerNodeRequest(Protocol.RANDOM_PEER_REQ, connection);
    }

    // RANDOM PEER RESP
    public static Event buildRandomPeerResponseEvent(NodeConnection connection, String IP) throws IOException {
        return new RandomPeerNodeResponse(Protocol.RANDOM_PEER_RESP, connection, IP);
    }

    public static Event buildEvent(byte[] marshalledBytes) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

            switch(din.readInt()) { // read protocol type byte
                case Protocol.REGISTER:
                    return new Register(marshalledBytes);
                case Protocol.REGISTER_RESP:
                    return new RegisterResponse(marshalledBytes);
                case Protocol.JOIN_REQ:
                    return new JoinRequest(marshalledBytes);
                case Protocol.JOIN_RESP:
                    return new JoinResponse(marshalledBytes);
                case Protocol.RANDOM_PEER_REQ:
                    return new RandomPeerNodeRequest(marshalledBytes);
                case Protocol.RANDOM_PEER_RESP:
                    return new RandomPeerNodeResponse(marshalledBytes);
                default: return null;
            }
        } catch(IOException ioe) { 
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
