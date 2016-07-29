package com.welink.web.resource;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.commons.Env;
import com.welink.commons.utils.UpYun;
import com.welink.commons.utils.UpYunUtil;

/**
 * Created by daniel on 15-4-24.
 */
@Controller
public class Hello {
	
	@Resource
    private Env env;

    @RequestMapping({"/h/hello.htm"})//上传文件
    public String execute(HttpServletRequest request, HttpServletResponse response,
                          ModelMap model, @RequestParam("file") MultipartFile file) throws Exception {
        // 判断文件是否为空
        if (!file.isEmpty()) {
            try {
                // 文件保存路径
                String filePath = request.getSession().getServletContext().getRealPath("/")
                        + file.getOriginalFilename();
                // 转存文件
                file.transferTo(new File(filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 重定向
        return "hello";
    }
    
    @RequestMapping({"/h/hello2.htm"})//上传文件
    public String execute2(HttpServletRequest request, HttpServletResponse response,
                          ModelMap model, @RequestParam("file") MultipartFile file) throws Exception {
        // 判断文件是否为空
        if (!file.isEmpty()) {
    		System.out.println("111111111111111111111111");
    		try {
    			if(!UpYunUtil.isImage(file)){
					//判断是否是图片
					System.out.println("3333333333333333333333333333failed");
				}else{
					System.out.println("44444444444444444444444444444success");
				}
    			// 文件保存路径
    			String filePath = request.getSession().getServletContext().getRealPath("/")
    					+ file.getOriginalFilename();
    			byte[] bytes = file.getBytes();
    			toUpyun(file);
    			// 转存文件
    			//file.transferTo(new File(filePath));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}else{
    		System.out.println("22222222222222222222222222222");
    	}
        // 重定向
        return "hello";
    }
    
    public void toUpyun(MultipartFile file) throws IOException{
    	String BUCKET_NAME = "mikumine";
    	String OPERATOR_NAME = "unesmall";
    	String OPERATOR_PWD = "unesmall123456";
    	/** 根目录 */
    	String DIR_ROOT = "/test/";
    	/** 上传到upyun的图片名 */
    	//private static final String PIC_NAME = "sample.jpeg";
    	String PIC_NAME = "youlishiTest2.jpeg";
    	// 要传到upyun后的文件路径
    	String filePath = DIR_ROOT + PIC_NAME;
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
    		upyun.setFileSecret("bac");

    		// 上传文件，并自动创建父级目录（最多10级）
    		boolean result = upyun.writeFile(filePath, file.getBytes(), true);
    		//assertTrue(result);

    		// 图片宽度
    		String width = upyun.getPicWidth();
    		// 图片高度
    		String height = upyun.getPicHeight();
    		// 图片帧数
    		String frames = upyun.getPicFrames();
    		// 图片类型
    		String type = upyun.getPicType();

    		/*assertTrue(width != null && !"".equals(width));
    		assertTrue(height != null && !"".equals(height));
    		assertTrue(frames != null && !"".equals(frames));
    		assertTrue(type != null && !"".equals(type));*/
    	}
    }
    
    @RequestMapping(value = {"/api/m/1.0/hello2.json", "/api/h/1.0/hello2.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	if (env.isProd()) {
    		System.out.println("1111111111111111111111111111111111111111111111111111111111111");
    		System.out.println("notify_url........................."+AlipayConfig.Notify_URL);
        } else {
            System.out.println("2222222222222222222222222222222222222222222222222222222222222222222222222");
    		System.out.println("notify_url........................."+AlipayConfig.APP_DAILY_Notify_URL);
        }
    	WelinkVO welinkVO = new WelinkVO();
    	welinkVO.setStatus(0);
        welinkVO.setCode(10);
        welinkVO.setMsg("lgc........");
    	return JSON.toJSONString(welinkVO);
    }
    
}
