package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkNode implements Node {

    public static final int MAJOR_HB_SECONDS = 60*3;
    public static final int MINOR_HB_SECONDS = MAJOR_HB_SECONDS/10;

    public static final int DEFAULT_SERVER_PORT = 18080;
    public static final String BASE_SAVE_DIR = "/tmp/cs555_data/";

    private NodeConnection _Controller = null;
    private TCPServerThread serverThread = null;                                // listens for incoming client nodes
    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ChunkStorage> storedChunksMap = new ConcurrentHashMap<>();
    private List<ChunkStorage> newChunksStored = new ArrayList<>();             // holds data items for minor heartbeats

    public ChunkNode(String host, int port) {
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:"+ioe.getMessage());
            System.exit(0);
        }

        try {
            _Controller = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, port));
            _Controller.sendEvent(EventFactory.buildRegisterEvent(_Controller));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:"+ioe.getMessage());
            System.exit(0);
        }
        new Timer().schedule(new HeartbeatTaskManager(), 0, MINOR_HB_SECONDS*1000);
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            case Protocol.STORE_CHUNK:
                processStoreChunk((StoreChunk) event);
                break;
            case Protocol.CHUNK_ROUTE: // used for requesting replica chunks to correct errors or downed nodes
                try {
                    processChunkRoute((ChunkRoute) event);
                } catch (IOException e) {
                    System.out.println("error correcting corrupt chunk");
                    e.printStackTrace();
                }
                break;
            case Protocol.CHUNK_REQ:
                try {
                    processChunkRequest((ChunkIdentifier) event);
                } catch (IOException e) {
                    System.out.println("Error processing chunk request.");
                    e.printStackTrace();
                }
                break;
            case Protocol.STORE_ERASURE:
                System.out.println();
                System.out.println(((StoreChunk) event).getFileName() + " storing erasure for chunk " + ((StoreChunk) event).getChunkSequenceID() + " fragment " + ((StoreChunk) event).getErasureFragmentID() );
                System.out.println();
                processStoreErasure((StoreChunk) event);
                break;
            case Protocol.ERASURE_REQ:
                try {
                    processErasureFragmentRequest((ChunkIdentifier) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void processErasureFragmentRequest(ChunkIdentifier event) throws IOException {
        NodeConnection client = connectionsMap.get(event.getHeader().getSenderKey());

        // get erasure data
        Path path = Paths.get(BASE_SAVE_DIR+event.getErasureStorageName());
        byte[] bytesToSend = Files.readAllBytes(path);

        System.out.println("sent erasure fragment:" + event.getFilename() + " : " + event.getSequence() + " : " + event.getFragment());
        client.sendEvent(EventFactory.buildStoreErasureFragment(client, event.getFilename(), event.getSequence(), bytesToSend, event.getFragment()));
    }

    private void processStoreErasure(StoreChunk event) {
        ChunkStorage cs = new ChunkStorage(event.getFileName(), "0.0", event.getChunkSequenceID(), event.getErasureFragmentID(), 0L, "");
        storedChunksMap.put(cs.getErasureStorageName(), cs);
        storeChunk(cs.getErasureStorageName(), event.getBytesToStore());
    }

    /*
        pulls desired chunk from one of the valid replicas
     */
    private void processChunkRoute(ChunkRoute event) throws IOException {
        String replica = Util.getOtherReplica(event.getChunksInformation()[event.getSequence()], Util.removePort(_Controller.getLocalKey()));
        NodeConnection node = ConnectionFactory.buildConnection(this, replica, DEFAULT_SERVER_PORT);
        node.sendEvent(EventFactory.buildRequestChunk(node, event.getFileName(), event.getSequence()));
    }

    private void processChunkRequest(ChunkIdentifier event) throws IOException {
        System.out.println();
        System.out.println(event.getHeader().getSenderKey() + " requesting chunk " + event.getChunkStorageName());

        NodeConnection client = connectionsMap.get(event.getHeader().getSenderKey());
        ChunkStorage record = storedChunksMap.get(event.getChunkStorageName());

        // get file data
        Path path = Paths.get(BASE_SAVE_DIR+event.getChunkStorageName());
        byte[] bytesToSend = Files.readAllBytes(path);

        // integrity check
        System.out.println("integrity check:" + (record.getChecksum().equals(Util.getCheckSumSHA1(bytesToSend)) ? "passed" : "failed"));
        if(!record.getChecksum().equals(Util.getCheckSumSHA1(bytesToSend))) { // error correction
            client.sendEvent(EventFactory.buildCorruptChunkRequest(client, event.getFilename(), event.getSequence()));
            _Controller.sendEvent(EventFactory.buildCorruptChunkRequest(_Controller, event.getFilename(), event.getSequence()));
        } else
            client.sendEvent(EventFactory.buildStoreChunkEvent(client, record, bytesToSend, new ChunkReplicaInformation(new String[]{})));
        System.out.println();
    }

    private void processStoreChunk(StoreChunk event) {
        System.out.println();
        System.out.println("** Storing new chunk #" + event.getChunkSequenceID() + " for file: " + event.getFileName());
        ChunkStorage record = new ChunkStorage(event.getFileName(), event.getVersion(), event.getChunkSequenceID(), 0, new Date().getTime(), Util.getCheckSumSHA1(event.getBytesToStore()));
        System.out.println("Checksum:" + record.getChecksum());
        storeChunk(record.getChunkStorageName(), event.getBytesToStore());
        storeNewRecord(record);
        if(event.getNextHost() != null) { // forward replicas to other nodes
            try {
                NodeConnection nc = new NodeConnection(this, new Socket(event.getNextHost(), ChunkNode.DEFAULT_SERVER_PORT));
                nc.sendEvent(EventFactory.buildStoreChunkEvent(nc, event));
            } catch (IOException e) {
                System.out.println("Error forwarding process store chunk");
                e.printStackTrace();
            }
        }
        System.out.println();
    }

    private void storeNewRecord(ChunkStorage record) {
        synchronized (newChunksStored) {
            newChunksStored.add(record);
        }
        storedChunksMap.put(record.getChunkStorageName(), record);
    }

    private void storeChunk(String storeFileName, byte[] toStore) {
        BufferedOutputStream writer = null;
        try {
            File file = new File(BASE_SAVE_DIR+storeFileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            writer = new BufferedOutputStream(new FileOutputStream(file));
            writer.write(toStore);
        } catch (IOException e) {
            System.out.println("Error saving file...");
            e.printStackTrace();
        } finally {
            if (writer != null) { try { writer.close(); } catch (IOException e) {} }
        }
    }

    public void registerConnection(NodeConnection connection) {
        System.out.println("New connection: " + connection.getRemoteKey());
        connectionsMap.put(connection.getRemoteKey(), connection);
    }

    @Override
    public void lostConnection(String disconnectedIP) {
        System.out.println("Lost connection to:" + disconnectedIP);
    }

    public static void main(String args[]) {
        if(args.length > 0) {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            new ChunkNode(host,port);
        } else {
            System.out.println("Usage: java ChunkNode controller_host controller_port");
        }
    }

    /*
        sends heartbeats to the _Controller
        every ratioMinorToMajor heartbeats, a major heartbeat is sent
        otherwise a minor heartbeat is sent
     */
    private class HeartbeatTaskManager extends TimerTask {

        private final int ratioMinorToMajor = 10;
        private AtomicInteger numHeartbeats = new AtomicInteger(0);

        @Override public void run() {
            if(numHeartbeats.incrementAndGet() % ratioMinorToMajor == 0) {
                try {
                    _Controller.sendEvent(EventFactory.buildMajorHeartbeat(_Controller, storedChunksMap.values().toArray(new ChunkStorage[]{})));
                } catch (IOException e) { System.out.println("Error sending major heartbeat"); }
                //System.out.println("major heartbeat");
            } else
                try {
                    synchronized (newChunksStored) {
                        _Controller.sendEvent(EventFactory.buildMinorHeartbeat(_Controller, newChunksStored.toArray(new ChunkStorage[]{})));
                        newChunksStored.clear();
                    }
                } catch (IOException e) { System.out.println("Error sending minor heartbeat"); }
                //System.out.println("minor heartbeat");
        }
    }
}
