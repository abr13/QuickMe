package com.abr.quickme;

import java.math.BigInteger;
import java.util.Random;

public class MessageEncryption {

    public BigInteger N;
    private BigInteger p;
    private BigInteger q;
    private BigInteger phi;

    private BigInteger e;

    private BigInteger d;

    private int bitlength = 1024;

    private int blocksize = 256;

    //blocksize in byte

    private Random r;

    public MessageEncryption() {

        r = new Random();

        p = BigInteger.probablePrime(bitlength, r);

        q = BigInteger.probablePrime(bitlength, r);

        N = p.multiply(q);

        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        e = BigInteger.probablePrime(bitlength / 2, r);

        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {

            e.add(BigInteger.ONE);

        }

        d = e.modInverse(phi);

    }

    public MessageEncryption(BigInteger e, BigInteger d, BigInteger N) {

        this.e = e;

        this.d = d;

        this.N = N;

    }

    public static String bytesToString(byte[] encrypted) {

        String test = "";

        for (byte b : encrypted) {

            test += Byte.toString(b);

        }

        return test;

    }

    //Encrypt message

    public byte[] encrypt(byte[] message) {

        return (new BigInteger(message)).modPow(e, N).toByteArray();

    }

    // Decrypt message

    public byte[] decrypt(byte[] message, BigInteger M) {

        return (new BigInteger(message)).modPow(d, M).toByteArray();

    }

}