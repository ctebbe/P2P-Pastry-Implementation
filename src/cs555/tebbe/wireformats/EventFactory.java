package cs555.tebbe.wireformats;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.NodeConnection;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class EventFactory {

    protected EventFactory() {}
    private static EventFactory factory = null;
    public static EventFactory getInstance() {
        if(factory == null) factory = new EventFactory();
        return factory;
    }

    // REGISTER_REQ
    public static Event buildRegisterEvent(NodeConnection connection, String id) throws IOException {
        return new RegisterRequest(Protocol.REGISTER_REQ, connection, id);
    }

    // REGISTER_REQ RESP
    public static Event buildRegisterResponseEvent(NodeConnection connection, String id, boolean success, String node) throws IOException {
        return new RegisterAck(Protocol.REGISTER_ACK, connection, id, success, node);
    }

    // JOIN REQ
    public static Event buildJoinRequestEvent(NodeConnection connection, String toLookup) throws IOException {
        return new JoinLookupRequest(Protocol.JOIN_REQ, connection, toLookup, toLookup);
    }

    public static Event buildJoinRequestEvent(NodeConnection connection, JoinLookupRequest event, String id, List<PeerNodeData> newRow) throws IOException {
        if(newRow.size() > 0)
            event.routingTable.add(newRow); // append new row to accumulated routing table
        return new JoinLookupRequest(Protocol.JOIN_REQ, connection, event.getLookupID(), event.getRoute(), id, event.routingTable);
    }

    // JOIN RESP
    public static Event buildJoinResponseEvent(NodeConnection connection, String ID, PeerNodeData lowLeaf, PeerNodeData highLeaf, String[] route, List<List<PeerNodeData>> routingTable) throws IOException {
        return new JoinResponse(Protocol.JOIN_RESP, connection, ID, lowLeaf, highLeaf, route, routingTable);
    }

    // JOIN COMP
    public static Event buildJoinCompleteEvent(NodeConnection connection, String ID) throws IOException {
        return new NodeIDEvent(Protocol.JOIN_COMP, connection, ID);
    }

    // RANDOM PEER REQ
    public static Event buildRandomPeerRequestEvent(NodeConnection connection) throws IOException {
        return new RandomPeerNodeRequest(Protocol.RANDOM_PEER_REQ, connection);
    }

    // RANDOM PEER RESP
    public static Event buildRandomPeerResponseEvent(NodeConnection connection, String IP) throws IOException {
        return new RandomPeerNodeResponse(Protocol.RANDOM_PEER_RESP, connection, IP);
    }

    // LEAF SET UPDATE
    public static Event buildLeafsetUpdateEvent(NodeConnection connection, String ID, boolean lowLeaf) throws IOException {
        return new NodeIDEvent(Protocol.LEAFSET_UPDATE, connection, ID, lowLeaf,"");
    }

    public static Event buildLeafsetUpdateEvent(NodeConnection connection, PeerNodeData leaf, boolean lowLeaf) throws IOException {
        return new NodeIDEvent(Protocol.LEAFSET_UPDATE, connection, leaf.identifier, lowLeaf, leaf.host_port);
    }

    // FILE STORE REQ
    public static Event buildFileStoreRequestEvent(NodeConnection connection, String toLookup, String id) throws IOException {
        return new FileStoreLookupRequest(Protocol.FILE_STORE_REQ, connection, toLookup, id);
    }

    public static Event buildFileStoreRequestEvent(NodeConnection connection, FileStoreLookupRequest event, String id) throws IOException {
        return new FileStoreLookupRequest(Protocol.FILE_STORE_REQ, connection, event.getLookupID(), event.getRoute(), id);
    }

    // FILE STORE RESP
    public static Event buildFileStoreResponseEvent(NodeConnection connection, FileStoreLookupRequest request, String id) throws IOException {
        return new FileStoreLookupResponse(Protocol.FILE_STORE_RESP, connection, request, id);
    }

    // FILE STORE
    public static Event buildFileStoreEvent(NodeConnection connection, String fname, byte[] fbytes) throws IOException {
        return new StoreFile(Protocol.FILE_STORE, connection, fname, fbytes);
    }

    // FILE STORE COMP
    public static Event buildFileStoreCompleteEvent(NodeConnection connection, String fname) throws IOException {
        return new NodeIDEvent(Protocol.FILE_STORE_COMP, connection, fname);
    }

    // ROUTE TABLE UPDATE
    public static Event buildRouteTableUpdateEvent(NodeConnection connection, String id) throws IOException {
        return new NodeIDEvent(Protocol.TABLE_UPDATE, connection, id);
    }

    // EXIT OVERLAY
    public static Event buildExitOverlayEvent(NodeConnection connection, String id) throws IOException {
        return new NodeIDEvent(Protocol.EXIT, connection, id);
    }

    public static Event buildEvent(byte[] marshalledBytes) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

            switch(din.readInt()) { // read protocol type byte
                case Protocol.REGISTER_REQ:
                    return new RegisterRequest(marshalledBytes);
                case Protocol.REGISTER_ACK:
                    return new RegisterAck(marshalledBytes);
                case Protocol.JOIN_REQ:
                    return new JoinLookupRequest(marshalledBytes);
                case Protocol.JOIN_RESP:
                    return new JoinResponse(marshalledBytes);
                case Protocol.JOIN_COMP:
                    return new NodeIDEvent(marshalledBytes);
                case Protocol.RANDOM_PEER_REQ:
                    return new RandomPeerNodeRequest(marshalledBytes);
                case Protocol.RANDOM_PEER_RESP:
                    return new RandomPeerNodeResponse(marshalledBytes);
                case Protocol.LEAFSET_UPDATE:
                    return new NodeIDEvent(marshalledBytes);
                case Protocol.FILE_STORE_REQ:
                    return new FileStoreLookupRequest(marshalledBytes);
                case Protocol.FILE_STORE_RESP:
                    return new FileStoreLookupResponse(marshalledBytes);
                case Protocol.FILE_STORE:
                    return new StoreFile(marshalledBytes);
                case Protocol.FILE_STORE_COMP:
                    return new NodeIDEvent(marshalledBytes);
                case Protocol.TABLE_UPDATE:
                    return new NodeIDEvent(marshalledBytes);
                case Protocol.EXIT:
                    return new NodeIDEvent(marshalledBytes);
                default: return null;
            }
        } catch(IOException ioe) { 
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
