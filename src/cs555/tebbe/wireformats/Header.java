package cs555.tebbe.wireformats;
import cs555.tebbe.transport.*;
import java.util.*;
import java.io.*;
import java.net.*;
public class Header implements Event {

    private int protocol;
    private String senderKey;
    private String receiverKey;

    public Header(int protocol, NodeConnection connection) {
        this.protocol = protocol;
        this.senderKey = connection.getLocalKey();
        this.receiverKey = connection.getRemoteKey();
    }

    public Header(int protocol, String senderKey, String receiverKey) {
        this.protocol = protocol;
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
    }

    // strips a header out of the input stream and returns a new header
    public static Header parseHeader(DataInputStream din) throws IOException {
        // type
        int type = din.readInt();

        // sender
        int senderLen = din.readInt();
        byte[] senderBytes = new byte[senderLen];
        din.readFully(senderBytes);
        String sender = new String(senderBytes);

        // receiver
        int receiverLen = din.readInt();
        byte[] receiverBytes = new byte[receiverLen];
        din.readFully(receiverBytes);
        String receiver = new String(receiverBytes);

        return new Header(type, sender, receiver);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // type
        dout.writeInt(getType());
        // sender
        byte[] senderBytes = getSenderKey().getBytes();
        dout.writeInt(senderBytes.length);
        dout.write(senderBytes);
        // receiver
        byte[] receiverBytes = getReceiverKey().getBytes();
        dout.writeInt(receiverBytes.length);
        dout.write(receiverBytes);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public int getType() {
        return this.protocol;
    }

    public String getSenderKey() {
        return this.senderKey;
    }

    public String getReceiverKey() {
        return this.receiverKey;
    }

    @Override public String toString() {
        return "["+Protocol.getProtocolString(getType()) + " SenderKey:"+getSenderKey() + " RecKey:"+getReceiverKey() +"]";
    }
}
