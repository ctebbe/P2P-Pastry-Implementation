package cs555.tebbe.node;
import cs555.tebbe.transport.*;
import cs555.tebbe.wireformats.*;
public interface Node {
    void onEvent(Event event);
    void newConnectionMade(NodeConnection connection);
    void lostConnection(String disconnectedKey);
}
