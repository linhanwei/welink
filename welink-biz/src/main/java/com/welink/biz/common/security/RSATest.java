package com.welink.biz.common.security;

import com.welink.biz.common.constants.ResourcesConstants;

import java.nio.charset.Charset;

/**
 * Created by daniel on 14-9-12.
 */
public class RSATest {
    public static void main(String[] args) {
        String s = "123";
        String sHex = new String(AESUtil.parseByte2HexStr(s.getBytes()));
        RSAEncrypt rsaEncrypt = new RSAEncrypt();
        //rsaEncrypt.genKeyPair();
        String defaultCharsetName = Charset.defaultCharset().displayName();
        System.out.println("defaultCharsetName:" + defaultCharsetName);
        //加载公钥
        try {
            //rsaEncrypt.loadPublicKey(RSAEncrypt.DEFAULT_PUBLIC_KEY);
            rsaEncrypt.loadPublicKeyFromFile((ResourcesConstants.TEST_PUBLIC_KEY_PATH));
            System.out.println("加载公钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载公钥失败");
        }
        //加载私钥
        try {
            //rsaEncrypt.loadPrivateKey(RSAEncrypt.DEFAULT_PRIVATE_KEY);
            rsaEncrypt.loadPrivateKeyFromFile(ResourcesConstants.TEST_PRIVATE_KEY_PATH);
            System.out.println("加载私钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载私钥失败");
        }
        //测试字符串
        String toEncryptStr = "电脑 ，好12345678";
        try {
            //加密
            //byte[] toEn_content = new byte[128];
            //System.arraycopy(toEncryptStr.getBytes(),0 , toEn_content, 0, toEncryptStr.getBytes().length);
            byte[] toEncryptBytes = sHex.getBytes();// toEncryptStr.getBytes();//原始字符串
            String toEncryptStr16 = AESUtil.parseByte2HexStr(toEncryptStr.getBytes());
            //原始字符串的16进制字符串
            String originHexStr = AESUtil.parseByte2HexStr(toEncryptBytes);//原始字符串转hex string
            System.out.println("原始字符串的16进制字符串：" + originHexStr);
            System.out.println("原始字符串的16进制字符串 len：" + originHexStr.length());


            byte[] rbytes = AESUtil.parseHexStr2Byte(originHexStr);
            String ss = new String(rbytes);
            System.out.println("原始字符串16进制转回：" + ss);
            System.out.println("toEncryptString bytes length:" + toEncryptBytes.length);
            //加密
            byte[] cipher = rsaEncrypt.encrypt(RSAEncrypt.publicKey, originHexStr.getBytes());//rsaEncrypt.encrypt(rsaEncrypt.getPublicKey(), toEncryptBytes);
            String encryptedHexString = AESUtil.parseByte2HexStr(cipher);//传输前做16进制字符串处理
            System.out.println("传输前，中的16进制：" + encryptedHexString);
            System.out.println("传输前，中的16进制len：" + encryptedHexString.length());
            System.out.println("加密后16进制字符串:");
            System.out.println(encryptedHexString);
            System.out.println("加密后数据：" + new String(cipher, "UTF-8"));
            System.out.println("加密前数据:" + toEncryptStr);
            //解密
            byte[] hexedCipher = AESUtil.parseHexStr2Byte(encryptedHexString);
            byte[] plainText = rsaEncrypt.decrypt(rsaEncrypt.getPrivateKey(), hexedCipher);

            String decodeStr16 = new String(plainText);
            //byte[] decodeStr16Bytes = AESUtil.parseHexStr2Byte(decodeStr16);
            //String ends = new String(decodeStr16Bytes,"UTF-8");
            //System.out.println("最终结果："+ends);
            //解密后是原始密码的16进制字符串
            //16进制解密
            String ends = new String(plainText, "UTF-8");
            System.out.println("最终解密结果：" + new String(plainText));
            System.out.println("最终解密结果len：" + new String(plainText).length());
//
//            String s = "你好12";
//            byte[] bytes = s.getBytes("UTF-8");
//            String hex = AESUtil.parseByte2HexStr(bytes);
//            System.out.println("十六进制数组:"+hex);
//            byte[] rbytes = AESUtil.parseHexStr2Byte(hex);
//            String ss = new String(rbytes,"UTF-8");
//            System.out.println("转回后："+ss);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
