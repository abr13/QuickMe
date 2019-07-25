package com.abr.quickme;

//import java.math.BigInteger;
//import java.util.Random;
//
//public class MessageEncryption {
//
//    public BigInteger N;
//    private BigInteger p;
//    private BigInteger q;
//    private BigInteger phi;
//
//    private BigInteger e;
//
//    private BigInteger d;
//
//    private int bitlength = 1024;
//
//    private int blocksize = 256;
//
//    //blocksize in byte
//
//    private Random r;
//
//    public MessageEncryption() {
//
//        r = new Random();
//
//        p = BigInteger.probablePrime(bitlength, r);
//
//        q = BigInteger.probablePrime(bitlength, r);
//
//        N = p.multiply(q);
//
//        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
//
//        e = BigInteger.probablePrime(bitlength / 2, r);
//
//        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
//
//            e.add(BigInteger.ONE);
//
//        }
//
//        d = e.modInverse(phi);
//
//    }
//
//    public MessageEncryption(BigInteger e, BigInteger d, BigInteger N) {
//
//        this.e = e;
//
//        this.d = d;
//
//        this.N = N;
//
//    }
//
//    public static String bytesToString(byte[] encrypted) {
//
//        String test = "";
//
//        for (byte b : encrypted) {
//
//            test += Byte.toString(b);
//
//        }
//
//        return test;
//
//    }
//
//    //Encrypt message
//
//    public byte[] encrypt(byte[] message) {
//
//        return (new BigInteger(message)).modPow(e, N).toByteArray();
//
//    }
//
//    // Decrypt message
//
//    public byte[] decrypt(byte[] message, BigInteger M) {
//
//        return (new BigInteger(message)).modPow(d, M).toByteArray();
//
//    }
//
//}

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryption {

    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}