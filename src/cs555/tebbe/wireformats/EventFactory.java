package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;

import java.io.*;

public class EventFactory {

    protected EventFactory() {}
    private static EventFactory factory = null;
    public static EventFactory getInstance() {
        if(factory == null) factory = new EventFactory();
        return factory;
    }

    public static Event buildLiveNodeEvent(NodeConnection connection, String name, String[] machines) throws IOException {
        return new StoreChunk(Protocol.LIVE_NODES, connection, name, "", 0, new byte[]{}, 0, new ChunkReplicaInformation(machines));
    }

    // REGISTER
    public static Event buildRegisterEvent(NodeConnection connection) throws IOException {
        return new Register(Protocol.REGISTER, connection);
    }

    // STORE FILE REQ
    public static Event buildStoreFileRequestEvent(NodeConnection connection, String file_name, int file_size) throws IOException {
        return new StoreFileRequest(Protocol.STORE_FILE_REQ, connection, file_name, file_size);
    }

    // STORE FILE(chunks) ROUTE
    public static Event buildChunkRouteEvent(NodeConnection connection, String fileName, ChunkReplicaInformation[] chunkReplicases) throws IOException {
        return new ChunkRoute(Protocol.CHUNK_ROUTE, connection, fileName, chunkReplicases);
    }

    public static Event buildChunkRouteEvent(NodeConnection connection, String fileName, int sequence, ChunkReplicaInformation[] chunkReplicases) throws IOException {
        return new ChunkRoute(Protocol.CHUNK_ROUTE, connection, fileName, sequence, chunkReplicases);
    }

    // STORE CHUNK EVENT
    public static Event buildStoreChunkEvent(NodeConnection connection, String name, String version, int chunk_sequence, byte[] bytes, ChunkReplicaInformation replicaInformation) throws IOException {
        return new StoreChunk(Protocol.STORE_CHUNK, connection, name, version, chunk_sequence, bytes, replicaInformation);
    }

    // for chaining store chunk events
    public static Event buildStoreChunkEvent(NodeConnection connection, StoreChunk storeChunk) throws IOException {
        return new StoreChunk(Protocol.STORE_CHUNK, connection, storeChunk.getFileName(), storeChunk.getVersion(), storeChunk.getChunkSequenceID(), storeChunk.getBytesToStore(), storeChunk.getChunkReplicaInformation());
    }

    // erasure
    public static Event buildStoreErasureFragmentRequest(NodeConnection connection, String name) {
        return new StoreFileRequest(Protocol.STORE_ERASURE_REQ, connection, name, 0);
    }

    public static Event buildStoreErasureFragment(NodeConnection connection, String name, int sequence, byte[] bytes, int fragment) {
        return new StoreChunk(Protocol.STORE_ERASURE, connection, name, "0.0", sequence, bytes, fragment, new ChunkReplicaInformation(new String[]{}));
    }

    public static Event buildRequestErasureFragment(NodeConnection conntion, String name, int sequence, int fragment) {
        return new ChunkIdentifier(Protocol.ERASURE_REQ, conntion, name, sequence, fragment);
    }

    public static Event buildStoreChunkEvent(NodeConnection connection, ChunkStorage storage, byte[] bytesToStore, ChunkReplicaInformation info) throws IOException {
        return new StoreChunk(Protocol.STORE_CHUNK, connection, storage.getFileName(), storage.getVersion(), storage.getSequence(), bytesToStore, info);
    }

    // MAJOR HEARTBEAT
    public static Event buildMajorHeartbeat(NodeConnection connection, ChunkStorage[] records) throws IOException {
        return new Heartbeat(Protocol.MAJOR_HEARTBEAT, connection, records);
    }

    // MINOR HEARTBEAT
    public static Event buildMinorHeartbeat(NodeConnection connection, ChunkStorage[] records) throws IOException {
        return new Heartbeat(Protocol.MINOR_HEARTBEAT, connection, records);
    }

    // REQUEST CHUNK
    public static Event buildRequestChunk(NodeConnection connection, String filename, int sequence) {
        return new ChunkIdentifier(Protocol.CHUNK_REQ, connection, filename, sequence);
    }

    // CORRUPTION DETECTION
    public static Event buildCorruptChunkRequest(NodeConnection connection, String filename, int sequence) {
        return new ChunkIdentifier(Protocol.CORRUPT_CHUNK_REQ, connection, filename, sequence);
    }

    // REQUEST FILE
    public static Event buildRequestReadFile(NodeConnection connection, String filename) {
        return new ChunkIdentifier(Protocol.READ_FILE_REQ, connection, filename, 0);
    }

    // RESPONSE READ FILE
    public static Event buildFileRouteEvent(NodeConnection connection, String fileName, ChunkReplicaInformation[] chunkReplicases) throws IOException {
        return new ChunkRoute(Protocol.READ_FILE_RESP, connection, fileName, chunkReplicases);
    }

    public static Event buildEvent(byte[] marshalledBytes) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

            switch(din.readInt()) { // read protocol type byte
                case Protocol.REGISTER:
                    return new Register(marshalledBytes);
                case Protocol.STORE_FILE_REQ:
                    return new StoreFileRequest(marshalledBytes);
                case Protocol.CHUNK_ROUTE:
                    return new ChunkRoute(marshalledBytes);
                case Protocol.STORE_CHUNK:
                    return new StoreChunk(marshalledBytes);
                case Protocol.MAJOR_HEARTBEAT:
                    return new Heartbeat(marshalledBytes);
                case Protocol.MINOR_HEARTBEAT:
                    return new Heartbeat(marshalledBytes);
                case Protocol.CHUNK_REQ:
                    return new ChunkIdentifier(marshalledBytes);
                case Protocol.READ_FILE_REQ:
                    return new ChunkIdentifier(marshalledBytes);
                case Protocol.READ_FILE_RESP:
                    return new ChunkRoute(marshalledBytes);
                case Protocol.CORRUPT_CHUNK_REQ:
                    return new ChunkIdentifier(marshalledBytes);
                case Protocol.STORE_ERASURE:
                    return new StoreChunk(marshalledBytes);
                case Protocol.ERASURE_REQ:
                    return new ChunkIdentifier(marshalledBytes);
                case Protocol.STORE_ERASURE_REQ:
                    return new StoreFileRequest(marshalledBytes);
                case Protocol.LIVE_NODES:
                    return new StoreChunk(marshalledBytes);
                default: return null;
            }
        } catch(IOException ioe) { 
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
