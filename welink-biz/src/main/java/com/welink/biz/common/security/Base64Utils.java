package com.welink.biz.common.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sun.misc.BASE64Encoder;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * @author yonder
 */
public class Base64Utils {

    private static final int CACHE_SIZE = 1024;

    /**
     * base64解码
     *
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode(String base64) throws Exception {
        return Base64.decode(base64.getBytes());
    }


    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes));
    }

    public static String encodeFile(String filePath) throws Exception {
        byte[] bytes = fileToByte(filePath);
        return encode(bytes);
    }

    public static void decodeToFile(String filePath, String base64) throws Exception {
        byte[] bytes = decode(base64);
        byteArrayToFile(bytes, filePath);
    }

    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] cache = new byte[CACHE_SIZE];
            int nRead = 0;
            while ((nRead = in.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            out.close();
            in.close();
            data = out.toByteArray();
        }
        return data;
    }

    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);
        File destFile = new File(filePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        destFile.createNewFile();
        OutputStream out = new FileOutputStream(destFile);
        byte[] cache = new byte[CACHE_SIZE];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {
            out.write(cache, 0, nRead);
            out.flush();
        }
        out.close();
        in.close();
    }
    
    /*--------------------------------------------------------------------------------*/
    
    // 
    /**
     * 
     * getImageStr:(图片转化成base64字符串). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param imgFile
     */
 	public static String getImageStr(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
 		//String imgFile = "d://lgctemp/2.png";// 待处理的图片
 		InputStream in = null;
 		byte[] data = null;
 		// 读取图片字节数组
 		try {
 			in = new FileInputStream(imgFile);
 			data = new byte[in.available()];
 			in.read(data);
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// 对字节数组Base64编码
 		BASE64Encoder encoder = new BASE64Encoder();
 		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
 	}
    
}
