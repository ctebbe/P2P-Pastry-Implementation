package cs555.tebbe.wireformats;
public class Protocol {

    // event types
    public static final int NOTYPE                  = -101;
    public static final int REGISTER                = 100;

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
