package cs555.tebbe.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Util {

    /*
        return a 16 digit checksum
     */
    public static String getCheckSumSHA1(byte[] bytes) {
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-1");
            hasher.reset();
            hasher.update(bytes);
            return new BigInteger(1, hasher.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return "";
    }

    // strips away the IP in the key format
    public static String removePort(String key) {
        return key.substring(0, key.indexOf(":"));
    }
    public static int removeIPAddress(String key) {
        return Integer.parseInt(key.substring(key.indexOf(":") + 1));
    }

    public static int generateRandomNumber(int min, int max) {
        return (int)(Math.random() * ((max-min) + 1) + min);
    }

    public static int generateRandomNumber() {
        return new Random().nextInt();
    }

    public static String getOtherReplica(ChunkReplicaInformation replicaInformation, String failedNode) {

        List<String> replicas = Arrays.asList(replicaInformation.getReplicaChunkNodes());
        Collections.reverse(replicas);
        for(String replica : replicas) {
            if(!replica.equals(failedNode))
                return replica;
        } return null;
    }
}
