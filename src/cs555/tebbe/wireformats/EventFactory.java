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

    // REGISTER
    public static Event buildRegisterEvent(NodeConnection connection) throws IOException {
        return new Register(Protocol.REGISTER, connection);
    }

    public static Event buildEvent(byte[] marshalledBytes) throws IOException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
            DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

            int type = din.readInt(); // read protocol type byte
            din.reset();

            switch(type) {
                case Protocol.REGISTER:
                    return new Register(din);
                default: return null;
            }
        } catch(IOException ioe) {
            System.out.println(ioe.toString()); 
        }
        return null;
    }
}
