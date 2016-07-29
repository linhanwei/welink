package org.welink.biz;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.welink.biz.common.fileUtil.ReadFromFile;
import com.welink.biz.common.security.AESUtil;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.common.security.RSAUtils;

public class RSATester {
    static String publicKey;
    static String privateKey;
    static HashMap<String, HashMap<String, String>> pvkey = new HashMap<String, HashMap<String, String>>();

    static String PASSWORD = "12345678";

    public static String content = "1234567890 zz abcdefghijklmnopqrstuvwxyz 啊伯吃的额火哥 好吧，我写不下去了，太多了好不好，还是不能用有木有！";

    static {
        try {
            Map keyMap = RSAUtils.genKeyPair();
            publicKey = RSAUtils.getPublicKey(keyMap);
            privateKey = RSAUtils.getPrivateKey(keyMap);
            Map<String, String> pv = new HashMap<String, String>();
            pv.put(publicKey, privateKey);
            pvkey.put("1", (HashMap<String, String>) pv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // testAES();
        testRSAandAES1();
        //test();
        //testSign();
    }

    static void testAES() throws NoSuchProviderException {
        String str = content;
        String password = "12345678";

        //AES加密
        System.out.println("========================================");
        System.out.println("| AES 开始加密");
        System.out.println("========================================");
        System.out.println("---> 加密前：" + content);
        byte[] encryptResult = null;
        encryptResult = AESUtil.encrypt(content, password);
        String encryptResultStr = AESUtil.parseByte2HexStr(encryptResult);
        System.out.println("---> 加密后：" + encryptResultStr);
        //AES解密
        byte[] decryptFrom = AESUtil.parseHexStr2Byte(encryptResultStr);
        byte[] decryptResult = null;
        decryptResult = AESUtil.decrypt(decryptFrom, password);
        System.out.println("---> 解密后：" + new String(decryptResult));
    }

    static void testRSAandAES() throws Exception {
        Map<String, String> mp = pvkey.get("1");
        for (String s : mp.keySet()) {

            System.out.println("public key:" + s);
            System.out.println("=====================================================================");
        }
        for (String s : mp.values()) {

            System.out.println("private key:" + s);
            System.out.println("=====================================================================");
        }
        String source = "你好，AES编码加密呵呵哈哈阿里郎了dlfjladal你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当d 叮叮当当";
        content = source;
        byte[] data = content.getBytes("UTF-8");
        System.out.println("========================================");
        System.out.println("| RSA 开始加密");
        System.out.println("========================================");
        System.out.println("---> RSA加密前数据：\r\n" + content);
        //RSA 加密：
        byte[] _RSAencodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        System.out.println("---> RSA加密后数据：\r\n" + new String(_RSAencodedData));
        System.out.println("---> AES加密前长度：" + _RSAencodedData.length);
        
         /*        byte[] de = null;
         try {
     		System.out.println("--->AES加密前数据："+new String(_RSAencodedData));
     		//AES 加密
 			byte[] en = AESUtil.encrypt(_RSAencodedData);
 			System.out.println("--->加密后数据："+ en);
 			// AES 解密
 			de = AESUtil.decrypt(en);
 			System.out.println("--->解密后数据："+de);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeySpecException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidParameterSpecException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
*/
        //RSA再解密
        //byte[] decodedData = RSAUtils.decryptByPrivateKey(de, privateKey);// AES解密需
        byte[] decodedData = RSAUtils.decryptByPrivateKey(_RSAencodedData, privateKey);
        String target = new String(decodedData, "UTF-8");
        System.out.println("---> RSA再解密后数据: \r\n" + target);
    }

    static void testRSAandAES1() throws Exception {
        //测试使用格式正确的key 此方法是可以的
        //publicKey = RSAEncrypt.DEFAULT_PUBLIC_KEY;
        String fileName = "D:/file/public.key";//public 3是通过struts2下载的文件，解密成功
        String prifileName = "D:/file/private.key";
        publicKey = null;
        publicKey = ReadFromFile.readKey(fileName, 1, 6);
        // privateKey = RSAEncrypt.DEFAULT_PRIVATE_KEY;
        privateKey = null;
        privateKey = ReadFromFile.readKey(prifileName, 1, 16);

        String source = "你好，AES编码加密呵呵哈哈阿里郎了dlfjladal你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当你好，AES编码加密呵呵哈哈阿里郎了dlfjladald 叮叮当当d 叮叮当当";
        source = "pswd";
        content = source;

        byte[] data = content.getBytes("UTF-8");
        System.out.println("========================================");
        System.out.println("| RSA 开始加密");
        System.out.println("========================================");
        System.out.println("---> RSA加密前数据：\r\n" + content);
        //RSA 加密：
        byte[] _RSAencodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        System.out.println("---> RSA加密后数据：\r\n" + new String(_RSAencodedData));
        System.out.println("---> AES加密前长度：" + _RSAencodedData.length);
        String _RSAencodeDataStr = AESUtil.parseByte2HexStr(_RSAencodedData);//加密后产生的byte数组转成string时要在各byte之间加个标识符，我加了个空格，

        System.out.println("加密后的十六进制:" + _RSAencodeDataStr);// 然后再根据空格分隔转换回byte数组。如果不加标识符，由于byte值可能是一位到三位，无法知道某一个byte是在哪里结束。当然也可以在转成string时补0。或者转成16进制固定为两位长。
         /*        byte[] de = null;
         try {
     		System.out.println("--->AES加密前数据："+new String(_RSAencodedData));
     		//AES 加密
 			byte[] en = AESUtil.encrypt(_RSAencodedData);
 			System.out.println("--->加密后数据："+ en);
 			// AES 解密
 			de = AESUtil.decrypt(en);
 			System.out.println("--->解密后数据："+de);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeySpecException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidParameterSpecException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
*/
        //RSA再解密
        //byte[] decodedData = RSAUtils.decryptByPrivateKey(de, privateKey);// AES解密需 _RSAencodeDataStr
        // byte[] decodedData = RSAUtils.decryptByPrivateKey(_RSAencodedData, privateKey);
        byte[] decodedData = RSAUtils.decryptByPrivateKey(AESUtil.parseHexStr2Byte(_RSAencodeDataStr), privateKey);
        String target = new String(decodedData, "UTF-8");
        System.out.println("---> RSA再解密后数据: \r\n" + target);
    }

    static void testSign() throws Exception {
        byte[] data = content.getBytes();
        byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);
        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);
        String target = new String(decodedData);
        String sign = RSAUtils.sign(encodedData, privateKey);
        boolean status = RSAUtils.verify(encodedData, publicKey, sign);
    }
}
