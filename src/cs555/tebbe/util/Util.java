package cs555.tebbe.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Util {

    public static String convertBytesToHex(byte[] buf) {
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            int byteValue = (int) buf[i] & 0xff;
            if (byteValue <= 15) {
                strBuf.append("0");
            }
            strBuf.append(Integer.toString(byteValue, 16));
        }
        return strBuf.toString();
    }

    public static byte[] convertHexToBytes(String hexString) {
        int size = hexString.length();
        byte[] buf = new byte[size / 2];
        int j = 0;
        for (int i = 0; i < size; i++) {
            String a = hexString.substring(i, i + 2);
            int valA = Integer.parseInt(a, 16);
            i++;
            buf[j] = (byte) valA;
            j++;
        }
        return buf;
    }

    // strips away the port in the key format host:port
    public static String removePort(String key) {
        return key.substring(0, key.indexOf(":"));
    }

    // strips away the IP address in the key format host:port
    public static int removeIPAddress(String key) {
        return Integer.parseInt(key.substring(key.indexOf(":") + 1));
    }

    public static int generateRandomNumber(int min, int max) {
        return (int)(Math.random() * ((max-min) + 1) + min);
    }

    public static void main(String[] args) {
        String s = "TEST STRING 0";
        System.out.println(Util.convertBytesToHex(s.getBytes()));
    }
}
