package com.welink.biz.common.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

public class AESUtil {

    /**
     * AES加密
     *
     * @param content
     * @param password
     * @return
     */
    public static byte[] encrypt(String content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(byteContent);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     *
     * @param content
     * @param password
     * @return
     */
    public static byte[] decrypt(byte[] content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
    
    /**
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


    //======================================================================================================

    private static final String password = "hahahahahahll";
    private static String initializationVector = "dladoeflefodladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfddladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfdefldladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfdadfoedladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfdldfdladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfdjlajdladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfddofafddladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfdafe234dladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfd32df1`13dladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfd33d2edladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfddfddladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfdfddladoeflefoefladfoeldfjlajdofafdafe23432df1`1333d2edfdfd";
    private static String salt = "saltaldjfld";
    private static int pswdIterations = 2;
    private static int keySize = 256;
    static byte[] ivBytes = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            if (i < initializationVector.getBytes().length) {
                ivBytes[i] = initializationVector.getBytes()[i];
            }
        }
    }

    public static byte[] encrypt(byte[] plainText) throws
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            InvalidParameterSpecException,
            IllegalBlockSizeException,
            BadPaddingException,
            UnsupportedEncodingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException {
        byte[] saltBytes = salt.getBytes("UTF-8");
        byte[] ivBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //initializationVector.getBytes("UTF-8");

        // Derive the key, given password and salt.
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                pswdIterations,
                keySize
        );

        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(ivBytes));

        byte[] encryptedTextBytes = cipher.doFinal(plainText);
        return encryptedTextBytes;//new Base64().encodeAsString(encryptedTextBytes);
    }

    public static byte[] decrypt(byte[] encryptedText) throws
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException {
        byte[] saltBytes = salt.getBytes("UTF-8");
        byte[] ivBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};//initializationVector.getBytes("UTF-8");
        byte[] encryptedTextBytes = encryptedText;//new Base64().decodeBase64(encryptedText);

        // Derive the key, given password and salt.
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                pswdIterations,
                keySize
        );

        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        // Decrypt the message, given derived key and initialization vector.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));

        byte[] decryptedTextBytes = null;
        try {
            decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return decryptedTextBytes;//new String(decryptedTextBytes);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        String s = "abcgihz+1423456";
        byte[] bS = AESUtil.parseHexStr2Byte(s);
        String ss = AESUtil.parseByte2HexStr(bS);
        System.out.println(bS);
        System.out.println(ss);
    }

}
