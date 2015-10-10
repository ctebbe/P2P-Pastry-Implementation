package cs555.tebbe.util;

import java.sql.Timestamp;
import java.util.Date;

public class Util {

    public static String getTimestampHexID() {
        return Util.getFormattedHexID(new Timestamp(new Date().getTime()).toString().getBytes());
    }

    public static int getAbsoluteHexDifference(String h1, String h2) {
        return Math.abs(Integer.parseInt(h1, 16) - Integer.parseInt(h2, 16));
    }

    public static String getFormattedHexID(byte[] buf) {
        String hex = convertBytesToHex(buf);
        int len = hex.length();
        return hex.substring(len-4, len); // get last 4 hex digits
    }

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
        if(!key.contains(":")) return key;
        return key.substring(0, key.indexOf(":"));
    }

    // strips away the IP address in the key format host:port
    public static int removeIPAddress(String key) {
        return Integer.parseInt(key.substring(key.indexOf(":") + 1));
    }

    public static int generateRandomNumber(int min, int max) {
        return (int)(Math.random() * ((max-min) + 1) + min);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Util.convertBytesToHex(new Timestamp(new Date().getTime()).toString().getBytes()));
        Thread.sleep(100);
        System.out.println(Util.convertBytesToHex(new Timestamp(new Date().getTime()).toString().getBytes()));
        Thread.sleep(1000);
        System.out.println(Util.convertBytesToHex(new Timestamp(new Date().getTime()).toString().getBytes()));
        System.out.println(Util.convertBytesToHex(new Timestamp(new Date().getTime()).toString().getBytes()));
        System.out.println(Util.convertBytesToHex("hello".getBytes()));


    }
}
