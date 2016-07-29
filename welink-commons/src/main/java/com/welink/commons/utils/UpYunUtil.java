/**
 * Project Name:welink-web
 * File Name:UpYunUtil.java
 * Package Name:com.welink.web.common.util
 * Date:2015年12月25日下午4:14:00
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName:UpYunUtil <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月25日 下午4:14:00 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class UpYunUtil {
	// 运行前先设置好以下三个参数
	private static final String BUCKET_NAME = "mikumine";        
	private static final String OPERATOR_NAME = "unesmall";      
	private static final String OPERATOR_PWD = "unesmall123456"; 
	
	public static final String UPYUN_URL = "http://mikumine.b0.upaiyun.com";
	
	public static final String[] imgExts = {".gif", ".jpg", ".jpeg",".bmp", ".png"};
	
	/** 绑定的域名 */
	private static final String URL = "http://" + BUCKET_NAME
			+ ".b0.upaiyun.com";
	
	/** 不知道模块的根目录 */
	public static final String UNKNOWN_DIR_ROOT = "/module/unKnown/";					//1

	/** 微信二维码根目录 */
	public static final String WX_DIR_ROOT = "/module/qrCode/wx/";						//2
	
	/** 颜值兑换根目录 */
	public static final String FACESCORE_DIR_ROOT = "/module/faceScore/";				//3
	
	/** 评论根目录 */
	public static final String COMMENTS_DIR_ROOT = "/module/comments/";					//4
	
	/** 退货根目录 */
	public static final String RETURNGOODS_DIR_ROOT = "/module/returnGoods/";			//5
	
	/** 私人定制报告根目录 */
	public static final String DZDETECTREPORT_DIR_ROOT = "/module/dzDetectReport/";		//6

	/** 上传到upyun的图片名 */
	private static final String PIC_NAME = "sample.jpeg";

	/** 本地待上传的测试文件 */
	private static final String SAMPLE_PIC_FILE = System
			.getProperty("user.dir") + "/sample.jpeg";
	
	
	/**
	 * 
	 * writePicByMultipartFile:(根据MultipartFile上传图片). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param file
	 * @param dir
	 * @param PIC_NAME
	 */
	public static Boolean writePicByMultipartFile(MultipartFile file, String dir, String PIC_NAME,
			Map<String, String> params){
		try {
			/** 上传到upyun的图片名 */
			//String PIC_NAME = "youlishiTest2.jpeg";
			// 要传到upyun后的文件路径
			String filePath = dir + "" +PIC_NAME;
			if(null != file && file.getBytes().length > 0){
				UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
				upyun.setTimeout(120);
				// 设置待上传文件的 Content-MD5 值
				// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
				//upyun.setContentMD5(UpYun.md5(file));
				
				// 设置待上传文件的"访问密钥"
				// 注意：
				// 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
				// 举例：
				// 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
				// 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
				//upyun.setFileSecret("bac");
				
				// 上传文件，并自动创建父级目录（最多10级）
				boolean result = upyun.writeFile(filePath, file.getBytes(), true, params);
				return result;
				/*// 图片宽度
				String width = upyun.getPicWidth();
				// 图片高度
				String height = upyun.getPicHeight();
				// 图片帧数
				String frames = upyun.getPicFrames();
				// 图片类型
				String type = upyun.getPicType();*/
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	/**
	 * 
	 * writePicByBytes:(根据字节上传图片). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param bytes
	 * @param dir
	 * @param PIC_NAME
	 */
	public static Boolean writePicByBytes(byte[] bytes, String dir, String PIC_NAME, Map<String, String> params){
		try {
			/** 上传到upyun的图片名 */
			//String PIC_NAME = "youlishiTest2.jpeg";
			// 要传到upyun后的文件路径
			String filePath = dir + "" +PIC_NAME;
			if(null != bytes && bytes.length > 0){
				UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
				upyun.setTimeout(120);
				// 设置待上传文件的 Content-MD5 值
				// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
				//upyun.setContentMD5(UpYun.md5(file));
				
				// 设置待上传文件的"访问密钥"
				// 注意：
				// 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
				// 举例：
				// 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
				// 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
				//upyun.setFileSecret("bac");
				
				// 上传文件，并自动创建父级目录（最多10级）
				boolean result = upyun.writeFile(filePath, bytes, true, params);
				return result;
				/*// 图片宽度
				String width = upyun.getPicWidth();
				// 图片高度
				String height = upyun.getPicHeight();
				// 图片帧数
				String frames = upyun.getPicFrames();
				// 图片类型
				String type = upyun.getPicType();*/
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	/**
	 * 
	 * writePicByFile:(根据file上传图片). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param bytes
	 * @param dir
	 * @param PIC_NAME
	 */
	public static Boolean writePicByFile(File file, String dir, String PIC_NAME, Map<String, String> params){
		try {
			/** 上传到upyun的图片名 */
			//String PIC_NAME = "youlishiTest2.jpeg";
			// 要传到upyun后的文件路径
			String filePath = dir + "" +PIC_NAME;
			if(null != file){
				UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
				upyun.setTimeout(120);
				// 设置待上传文件的 Content-MD5 值
				// 如果又拍云服务端收到的文件MD5值与用户设置的不一致，将回报 406 NotAcceptable 错误
				//upyun.setContentMD5(UpYun.md5(file));
				
				// 设置待上传文件的"访问密钥"
				// 注意：
				// 仅支持图片空！，设置密钥后，无法根据原文件URL直接访问，需带URL后面加上（缩略图间隔标志符+密钥）进行访问
				// 举例：
				// 如果缩略图间隔标志符为"!"，密钥为"bac"，上传文件路径为"/folder/test.jpg"，
				// 那么该图片的对外访问地址为：http://空间域名 /folder/test.jpg!bac
				//upyun.setFileSecret("bac");
				
				// 上传文件，并自动创建父级目录（最多10级）
				boolean result = upyun.writeFile(filePath, file, true, params);
				return result;
				/*// 图片宽度
				String width = upyun.getPicWidth();
				// 图片高度
				String height = upyun.getPicHeight();
				// 图片帧数
				String frames = upyun.getPicFrames();
				// 图片类型
				String type = upyun.getPicType();*/
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	/**
	 * 删除文件
	 */
	public static Boolean deleteFile(String filePath) {
		UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
		// upyun空间下存在的文件的路径
		//String filePath = DIR_ROOT + filName;
		// 删除文件
		return upyun.deleteFile(filePath);
	}
	
	/**
	 * 
	 * isImage:(通过文件后缀判断是否为图片). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param file
	 */
	public static  boolean isImage(MultipartFile file)  
    {  
    	/*String[] imgExts = {".gif", ".jpg", ".jpeg",".bmp", ".png"};
    	List<String> imgExtsList = Arrays.asList(imgExts);*/
        boolean flag = false;   
        try{  
        	if(null != imgExts && imgExts.length > 0){	//判断图片后缀是否图片后缀
        		for(int i=0; i<imgExts.length; i++){
        			if(file.getOriginalFilename().toLowerCase().endsWith(imgExts[i])){
        				flag = true;
        			}
        		}
        	}
        	if(null != file){
        		ImageInputStream is = ImageIO.createImageInputStream(file.getInputStream());
        		if(null == is){  
        			return false;  
        		}  
        		is.close();  
        		//flag = true;  
        	}
        } catch (Exception e) {  
        	flag = false;   
        }  
        return flag;  
    }
	
	/** 
     * 根据地址获得数据的字节流 
     * @param strUrl 网络连接地址 
     * @return 
     */  
    public static byte[] getImageFromNetByUrl(String strUrl){  
        try {  
            URL url = new URL(strUrl);  
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
            conn.setRequestMethod("GET");  
            conn.setConnectTimeout(5 * 1000);  
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据  
            if(null != inStream){
            	ImageInputStream is = ImageIO.createImageInputStream(inStream);
            	if(null == is){ 
            		return null;
        		}  
        		is.close(); 
	    		byte[] btImg = readInputStream(inStream);//得到图片的二进制数据  
	            if(null != conn){
	            	conn.disconnect();
	            }
	            return btImg;
        		
            }
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
    
    /** 
     * 从输入流中获取数据 
     * @param inStream 输入流 
     * @return 
     * @throws Exception 
     */  
    public static byte[] readInputStream(InputStream inStream) throws Exception{  
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        while( (len=inStream.read(buffer)) != -1 ){  
            outStream.write(buffer, 0, len);  
        }  
        inStream.close();  
        return outStream.toByteArray();  
    } 
    
	
	public static void main(String[] args) {
		UpYun upyun = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
		if(upyun.deleteFile("/test/youlishiTest2.jpeg")){
			System.out.println("1111111111111111111111111111111111111111111");
		}else{
			System.out.println("22222222222222222222222222222222222222222222222");
		}
	}
	
}

