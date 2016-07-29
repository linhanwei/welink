/**
 * Project Name:welink-web
 * File Name:Base64Test.java
 * Package Name:com.welink.web.qrCode
 * Date:2016年1月18日下午3:13:11
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
 */

package com.welink.web.qrCode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.welink.biz.common.security.Base64Utils;

/**
 * ClassName:Base64Test <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年1月18日 下午3:13:11 <br/>
 * 
 * @author LuoGuangChun
 * @version
 * @since JDK 1.6
 * @see
 */
public class Base64Test {

	@Test
	public void test1() {
		String imgStr = GetImageStr();
		System.out.println(imgStr);
		GenerateImage(imgStr);

		/*BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			UpYunUtil.writePicByBytes(b, "/test/", "222.jpg");
		} catch (Exception e) {
		}*/
	}
	
	public void test2() throws Exception{
		String encodeFile = Base64Utils.encodeFile("d://lgctemp/2.png");
		System.out.println(encodeFile);
		Base64Utils.decodeToFile("d://lgctemp/3.png", encodeFile);
	}

	// 图片转化成base64字符串
	public static String GetImageStr() {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		String imgFile = "d://lgctemp/2.png";// 待处理的图片
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

	// base64字符串转化成图片
	public static boolean GenerateImage(String imgStr) { // 对字节数组字符串进行Base64解码并生成图片
		if (imgStr == null) // 图像数据为空
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			// 生成jpeg图片
			String imgFilePath = "d://lgctemp/223.png";// 新生成的图片
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
