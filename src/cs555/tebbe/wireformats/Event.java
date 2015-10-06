package cs555.tebbe.wireformats;
import cs555.tebbe.transport.NodeConnection;

import java.io.*;
public abstract class Event {

    private final Header header;

    public Event(int type, NodeConnection connection) {
        header = new Header(type, connection);
    }

    public Event(DataInputStream din) throws IOException {
        header = Header.parseHeader(din);
    }

    public Header getHeader() {
        return this.header;
    }

    public int getType() {
        return this.header.getType();
    }

    /* ABSTRACT METHODS */
    public abstract byte[] getBytes() throws IOException;
}
