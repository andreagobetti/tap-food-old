package com.lynkteam.tapmanager.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by robertov on 31/07/2015.
 */
public class CryptoUtil {

    public static String SHA256(String password) {
        MessageDigest digest = null;
        String hash = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(password.getBytes());

            hash = bytesToHexString(digest.digest());

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();

        }

        return hash;
    }

    //stringa casuale 24 caratteri
    public static String getRandomString() {
        SecureRandom random = new SecureRandom();

        return new BigInteger(120, random).toString(32);
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


}
