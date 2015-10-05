package cs555.tebbe.wireformats;
import java.io.*;
public abstract class Event {

    private Header header;

    abstract int getType();
    abstract byte[] getBytes() throws IOException;
}
