package cs555.tebbe.wireformats;
import java.io.*;
public interface Event {
    int      getType();
    byte[]   getBytes() throws IOException;
    //Header   getHeader();

}
