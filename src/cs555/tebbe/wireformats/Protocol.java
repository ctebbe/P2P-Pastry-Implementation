package cs555.tebbe.wireformats;
public class Protocol {


    public static final double CHUNK_SIZE_KB = 64.0;
    public static final int NUM_REPLICAS_PER_CHUNK = 3;

    // event types
    public static final int NOTYPE                  = -101;
    public static final int REGISTER                = 100;

    // routing
    public static final int CHUNK_ROUTE             = 102;

    // store
    public static final int STORE_FILE_REQ          = 103;
    public static final int STORE_CHUNK             = 104;

    // read
    public static final int READ_FILE_REQ           = 105;
    public static final int READ_FILE_RESP          = 115;
    public static final int CHUNK_REQ               = 106;

    // error detection
    public static final int CORRUPT_CHUNK_REQ       = 107;

    // erasure
    public static final int STORE_ERASURE           = 121;
    public static final int ERASURE_REQ             = 122;
    public static final int STORE_ERASURE_REQ       = 123;

    // heartbeats
    public static final int MAJOR_HEARTBEAT         = 108;
    public static final int MINOR_HEARTBEAT         = 109;

    public static final int LIVE_NODES          = 131;

    // status codes
    public static final byte NOSTATUS               = (byte) 0x00;
    public static final byte SUCCESS                = (byte) 0x01;
    public static final byte FAILURE                = (byte) 0x02;

    public static String getProtocolString(int protocol) {
        switch(protocol) {
            case NOTYPE:                return "NOTYPE";
            case REGISTER:              return "REGISTER";
            default:                    return "UNKNOWN";
        }
    }
}
