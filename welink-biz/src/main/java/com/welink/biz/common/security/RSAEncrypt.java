package com.welink.biz.common.security;

import com.welink.biz.common.constants.ResourcesConstants;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAEncrypt {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(RSAEncrypt.class);

    public static final String DEFAULT_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCTevDTNKW0N9GO4UhGhFLjCV/9" + "\r" +
                    "JCWKgvkQSYs1BrR3Ak/z+Hvo3jIx7uZw8hTB4pnungvKju5ix9IGMf0M6J53tpiZ" + "\r" +
                    "1rGZh6HEPBdsUebuAeKGlgSkf2wqbtrxZ6Git9CybvmBAM34qzFCrajRKWBcAKHq" + "\r" +
                    "1bHkLQ/GRT1EDemt1wIDAQAB" + "\r";

    public static final String DEFAULT_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJN68NM0pbQ30Y7h" + "\r" +
                    "SEaEUuMJX/0kJYqC+RBJizUGtHcCT/P4e+jeMjHu5nDyFMHime6eC8qO7mLH0gYx" + "\r" +
                    "/Qzonne2mJnWsZmHocQ8F2xR5u4B4oaWBKR/bCpu2vFnoaK30LJu+YEAzfirMUKt" + "\r" +
                    "qNEpYFwAoerVseQtD8ZFPUQN6a3XAgMBAAECgYB6pK5ItWtnZ1PZofbczYWwUEeG" + "\r" +
                    "19xwR2Kf7MjbG+xPW5jppiZFDSJo6+au3mHsqmigSsY7eIk9lDZP+Jobdgbw+7bJ" + "\r" +
                    "YQnJqZ56jnYVCnhcsgPc0kKhGFI0IeziI4ZyKLeue3KSP+akWCFWmBH2Rso0bqkA" + "\r" +
                    "MbWHgs1kbYs1+MnHQQJBAMNgGGZbowRblMAwTb897YXN26RW/H4XFN75VZRWkaoV" + "\r" +
                    "/evIUwGTWQh8R8vJ3zhjLDlwe3kMFQcJ0EDi4321NRkCQQDBPjsrNwrYYzuBKSVy" + "\r" +
                    "cwGZ0jKg0CfoI74BNjJJIpGElG+Ky/SVwx/bf4gEwmmRPPD7Mb2zAzjpAt1Lsk/c" + "\r" +
                    "IehvAkB516j/1LAyXIbE4Jrr5EwHwRz0PUdTv0NF1wr26pIHF5X2gES728+Piiog" + "\r" +
                    "PjIWcUq4O5zVHaLTc9xLSvQChoqpAkEAvaVSMiYRma1BT4/O9VQSpSAZmaLPF05E" + "\r" +
                    "gTHsBcRMJV37Xa6tuXBGSocNOQaGzgeNxCfbsNwftOWfwlMbdsE8NwJAUhpAEkPq" + "\r" +
                    "BLU8laLddzH7F7xXea1dpsFy5+BNHI+xtR3NQ22P+vDdcq2j6s8VDey+UbWzD0iR" + "\r" +
                    "+sUzHRCUza4jhA==" + "\r";

    /**
     *  * 私钥
     *  
     */
    private static RSAPrivateKey privateKey;

    private static RSAPrivateKey Alipay_privateKey;

    private static boolean privateKeyLoaded = false;

    private static boolean publicKeyLoaded = false;

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     *  * 公钥
     *  
     */
    public static RSAPublicKey publicKey;
    /**
     *  * 字节数据转字符串专用集合
     *  
     */
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     *  * 获取私钥
     *  * @return 当前的私钥对象
     *  
     */
    public static RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public static RSAPublicKey getPublicKeys() {
        return publicKey;
    }

    /**
     *  * 获取公钥
     *  * @return 当前的公钥对象
     *  
     */
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     *  * 随机生成密钥对
     *  
     */
    @SuppressWarnings("static-access")
    public void genKeyPair() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    /**
     *  * 从文件中输入流中加载公钥
     *  * @param in 公钥输入流
     *  * @throws Exception 加载公钥时产生的异常
     *  
     */
    public void loadPublicKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPublicKey(sb.toString());
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }

    public void loadPublicKeyFromFile(String fileName) {
        File file = new File(fileName);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            log.error("public key file not find. exp:" + e1.getMessage());
        }
        byte[] decodedData = null;
        try {
            RSAEncrypt rsaEncrypt = new RSAEncrypt();
            if (null != in) {
                rsaEncrypt.loadPublicKey(in);
            }
        } catch (Exception e) {
            log.error("decode data failed. exp:" + e.getMessage());
        }
    }

    /**
     *  * 从字符串中加载公钥
     *  * @param publicKeyStr 公钥数据字符串
     *  * @throws Exception 加载公钥时产生的异常
     *  
     */
    public void loadPublicKey(String publicKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (IOException e) {
            throw new Exception("公钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /*
    *load private key from file in case
    */
    public static void loadPriKeyForAlipay() {

        String prifileName = ResourcesConstants.PRIVATE_KEY_PATH;
        //String prifileName = ServletActionContext.getServletContext().getRealPath("/")+"WEB-INF/classes/"+ResourcesConstants.PRIVATE_KEY_PATH;
        String path = PasswordParser.class.getResource("/").getPath();
        File tmp = new File(path);
        String tmpPath = tmp.getParentFile().getParentFile().getPath();
        tmpPath = tmp.getParentFile().getParentFile().getParent();
        prifileName = tmpPath + "/welink-web/target/classes/" + ResourcesConstants.ALIPAY_PRIVATE_KEY_PATH;
        File file = new File(prifileName);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            log.error("private key file not find. exp:" + e1.getMessage());
        }
        if (null == RSAEncrypt.Alipay_privateKey) {
            RSAEncrypt rsaEncrypt = new RSAEncrypt();
            if (null != in) {
                try {
                    rsaEncrypt.loadPrivateKeyForAlipay(in);
                } catch (Exception e) {
                    log.error("load private key failed . exp:" + e.getMessage());
                }
            }
        }
    }


    /**
     *  * 从文件中加载私钥
     *  * @param keyFileName 私钥文件名
     *  * @return 是否成功
     *  * @throws Exception
     *  
     */
    public void loadPrivateKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPrivateKey(sb.toString());
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }

    /**
     *  * 从文件中加载私钥
     *  * @param keyFileName 私钥文件名
     *  * @return 是否成功
     *  * @throws Exception
     *  
     */
    public void loadPrivateKeyForAlipay(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPrivateKeyForAlipay(sb.toString());
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }


    public void loadPrivateKeyFromFile(String filePath) {
        File file = new File(filePath);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            log.error("private key file not find. exp:" + e1.getMessage());
        }
        byte[] decodedData = null;
        try {
            RSAEncrypt rsaEncrypt = new RSAEncrypt();
            if (null != in) {
                rsaEncrypt.loadPrivateKey(in);
            }
        } catch (Exception e) {
            log.error("decode data failed. exp:" + e.getMessage());
        }
    }

    @SuppressWarnings("static-access")
    public void loadPrivateKeyForAlipay(String privateKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.Alipay_privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            log.error("无此算法");
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (IOException e) {
            throw new Exception("私钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    @SuppressWarnings("static-access")
    public void loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            log.error("无此算法");
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (IOException e) {
            throw new Exception("私钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     *  * 加密过程
     *  * @param publicKey 公钥
     *  * @param plainTextData 明文数据
     *  * @return
     *  * @throws Exception 加密过程中的异常信息
     *  
     */
    public byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData) throws Exception {
        if (publicKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            //   cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     *  * 解密过程
     *  * @param privateKey 私钥
     *  * @param cipherData 密文数据
     *  * @return 明文
     *  * @throws Exception 解密过程中的异常信息
     *  
     */
    public static byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception {
        if (privateKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            //   cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());//RSA/ECB/NoPadding
            //cipher= Cipher.getInstance("RSA/ECB/NoPadding");
            cipher = Cipher.getInstance("RSA/ECB/NoPadding");//Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(cipherData);
            //   byte[] output = cipher.doFinal(enBuff);
            return output;
        } catch (NoSuchAlgorithmException e) {
            log.error("password decrypt error. 无此解密算法,请检查");
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            log.error("password decrypt error. 解密私钥非法,请检查");
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            log.error("password decrypt error. 密文长度非法");
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            log.error("password decrypt error. 密文数据已损坏");
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     *  * 字节数据转十六进制字符串
     *  * @param data 输入数据
     *  * @return 十六进制内容
     *  
     */
    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            //取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
            //取出字节的低四位 作为索引得到相应的十六进制标识符
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        String sss = "7084300c255d211b75ad37adf189ff6ba2ee822a768781dc8b98ffbc5a959ee5db68ae417982c3c24dbc75057028d2e4d31a08d1ed985a6a891b4e44d310f3215d4cad160157f4fe8eee03497bd8d2acb82d35c5a13b40fef3b1685d1211040332036abd06e01c7bde4a753f94c59ea06a3b1b1bb33333cd82a07cca14986093";
        byte[] js = hexStringToBytes(sss);
        System.out.print("========" + js.length);
        System.out.println("++++++" + RSAEncrypt.byteArrayToString(js));
        System.out.print("==========" + sss.length());
        RSAEncrypt rsaEncrypt = new RSAEncrypt();
        //rsaEncrypt.genKeyPair();
        String defaultCharsetName = Charset.defaultCharset().displayName();
        System.out.println("defaultCharsetName:" + defaultCharsetName);
        //加载公钥
        try {
            //rsaEncrypt.loadPublicKey(RSAEncrypt.DEFAULT_PUBLIC_KEY);
            //rsaEncrypt.loadPublicKeyFromFile(("/users/daniel/work/key/public.key"));
        	rsaEncrypt.loadPublicKeyFromFile(("D:\\workspace\\guogegeWS\\welink\\welink-web\\src\\main\\resources/public.key"));
        	
            System.out.println("加载公钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载公钥失败");
        }
        //加载私钥
        try {
            //rsaEncrypt.loadPrivateKey(RSAEncrypt.DEFAULT_PRIVATE_KEY);
            //rsaEncrypt.loadPrivateKeyFromFile("/users/daniel/work/key/private.key");
            rsaEncrypt.loadPrivateKeyFromFile("D:\\workspace\\guogegeWS\\welink\\welink-web\\src\\main\\resources/private.key");
            System.out.println("加载私钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载私钥失败");
        }
        //测试字符串
        String toEncryptStr = "3132d号33";
        //toEncryptStr = AESUtil.parseByte2HexStr(toEncryptStr.getBytes());
        try {
            //加密
            //byte[] toEn_content = new byte[128];
            //System.arraycopy(toEncryptStr.getBytes(),0 , toEn_content, 0, toEncryptStr.getBytes().length);
            byte[] toEncryptBytes = toEncryptStr.getBytes("UTF-8");
            System.out.println("hex:" + AESUtil.parseByte2HexStr(toEncryptBytes));
            System.out.println("toEncryptString bytes length:" + toEncryptBytes.length);
            byte[] cipher = rsaEncrypt.encrypt(rsaEncrypt.getPublicKey(), toEncryptBytes);//rsaEncrypt.encrypt(rsaEncrypt.getPublicKey(), toEncryptBytes);
            System.out.println("---:" + AESUtil.parseByte2HexStr(cipher));
            System.out.println("cipher len:" + cipher.length);
            String encoded = new String(cipher, "UTF-8");
            System.out.println("加密后数据：" + new String(cipher, "UTF-8"));
            System.out.println("加密后数据 encoded：" + AESUtil.parseByte2HexStr(cipher));

            System.out.println("加密前数据:" + toEncryptStr);
            //解密
            String psd = "3914EFBFBDEFBFBDEFBFBD53EFBFBD5EEFBFBDEFBFBD2528EFBFBD1B62187F475BEFBFBD47EFBFBDEFBFBDEFBFBD1EEFBFBD507C0F0624EFBFBDEFBFBD6EEFBFBDEFBFBDEFBFBD0FEFBFBDEFBFBD3429EFBFBD2FEFBFBDEFBFBD14EFBFBDEFBFBDEFBFBDEFBFBD46EFBFBD08EFBFBD33357C3FEFBFBD6BEFBFBD60EFBFBD21EFBFBD5FEFBFBDD98AEFBFBD6FEFBFBDEFBFBDEFBFBD2BEFBFBD4B3D462E46EFBFBDEFBFBD3D1D4AEFBFBDCE9EEFBFBD19EFBFBD486FEFBFBDEFBFBD73EFBFBDE69A9C6D26EFBFBD301303EFBFBD4CEFBFBD2EEFBFBDEFBFBDC39C024167EFBFBD50EFBFBDEFBFBD73C8AE0C";

            byte[] plainText = rsaEncrypt.decrypt(rsaEncrypt.getPrivateKey(), cipher);

            System.out.println("密文长度:" + js.length);
            System.out.println(RSAEncrypt.byteArrayToString(cipher));
            System.out.println("明文长度:" + plainText.length);
            String result = new String(plainText, "UTF-8");
            System.out.println("----result:" + result.replaceAll("[\u0000-\u001f]", ""));
            System.out.println("解密后数据：" + new String(plainText, "UTF-8"));
            //System.out.println("解密后数据截取:"+StringUtils.substring(sss, 0,8));


            String mypswd = "0e23709ef1973a874bbd38df259409320ab6f1397b1fdb508d667caefc84dfd2a2c611e1049a567b8348f707e1dc54c0a00041c27f5a0120fede5cf6aec9cf6041107b749e8c0bb4c89092909ca7b4f0b6c2a77fa82e83c659a4ac6744ffefba468e22982321d9e838c31006430a8673a9decc1b622584a6b517d1122c98f9e9";
            String tpswd = PasswordParser.parserPlanPswd(mypswd, null, false);

            String toStorePassword = BCrypt.hashpw(tpswd, BCrypt.gensalt());
            System.out.println("+++++++++" + tpswd);
            System.out.println("+++++++++00" + toStorePassword);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static RSAPrivateKey getAlipay_privateKey() {
        return Alipay_privateKey;
    }

    public static void setAlipay_privateKey(RSAPrivateKey alipay_privateKey) {
        Alipay_privateKey = alipay_privateKey;
    }

    public static boolean isPrivateKeyLoaded() {
        return privateKeyLoaded;
    }

    public static boolean isPublicKeyLoaded() {
        if (null != publicKey) {
            return true;
        }
        return false;
    }

    /**
     * js不支持byte，转换
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static void setPrivateKeyLoaded(boolean privateKeyLoaded) {
        RSAEncrypt.privateKeyLoaded = privateKeyLoaded;
    }
}
