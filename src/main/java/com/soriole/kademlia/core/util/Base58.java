package com.soriole.kademlia.core.util;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Base58 {
    public static class ValidationException extends Exception {
        public ValidationException(String s) {
            super(s);
        }

        public ValidationException(Exception e) {
            super(e);
        }
    }

    private static final char[] b58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int[] r58 = new int[256];

    static {
        for (int i = 0; i < 256; ++i) {
            r58[i] = -1;
        }
        for (int i = 0; i < b58.length; ++i) {
            r58[b58[i]] = i;
        }
    }

    public static String toBase58(byte[] b) {
        if (b.length == 0) {
            return "";
        }

        int lz = 0;
        while (lz < b.length && b[lz] == 0) {
            ++lz;
        }

        StringBuffer s = new StringBuffer();
        BigInteger n = new BigInteger(1, b);
        while (n.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] r = n.divideAndRemainder(BigInteger.valueOf(58));
            n = r[0];
            char digit = b58[r[1].intValue()];
            s.append(digit);
        }
        while (lz > 0) {
            --lz;
            s.append("1");
        }
        return s.reverse().toString();
    }

    public static String toBase58WithChecksum(byte[] b) {
        byte[] cs = hash(b);
        byte[] extended = new byte[b.length + 4];
        System.arraycopy(b, 0, extended, 0, b.length);
        System.arraycopy(cs, 0, extended, b.length, 4);
        return toBase58(extended);
    }

    public static byte[] fromBase58WithChecksum(String s) throws ValidationException {
        byte[] b = fromBase58(s);
        if (b.length < 4) {
            throw new ValidationException("Too short for checksum " + s);
        }
        byte[] cs = new byte[4];
        System.arraycopy(b, b.length - 4, cs, 0, 4);
        byte[] data = new byte[b.length - 4];
        System.arraycopy(b, 0, data, 0, b.length - 4);
        byte[] h = new byte[4];
        System.arraycopy(hash(data), 0, h, 0, 4);
        if (Arrays.equals(cs, h)) {
            return data;
        }
        throw new ValidationException("Checksum mismatch " + s);
    }

    public static byte[] fromBase58(String s) throws ValidationException {
        try {
            boolean leading = true;
            int lz = 0;
            BigInteger b = BigInteger.ZERO;
            for (char c : s.toCharArray()) {
                if (leading && c == '1') {
                    ++lz;
                } else {
                    leading = false;
                    b = b.multiply(BigInteger.valueOf(58));
                    b = b.add(BigInteger.valueOf(r58[c]));
                }
            }
            byte[] encoded = b.toByteArray();
            if (encoded[0] == 0) {
                if (lz > 0) {
                    --lz;
                } else {
                    byte[] e = new byte[encoded.length - 1];
                    System.arraycopy(encoded, 1, e, 0, e.length);
                    encoded = e;
                }
            }
            byte[] result = new byte[encoded.length + lz];
            System.arraycopy(encoded, 0, result, lz, encoded.length);

            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ValidationException("Invalid character in address");
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    public static byte[] hash(byte[] data, int offset, int len) {
        try {
            MessageDigest a = MessageDigest.getInstance("SHA-256");
            a.update(data, offset, len);
            return a.digest(a.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hash(byte[] data) {
        return hash(data, 0, data.length);
    }
}
