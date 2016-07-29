/**
 * Project Name:welink-web
 * File Name:QrPage.java
 * Package Name:com.welink.web.resource.hpage
 * Date:2015年11月11日下午6:21:05
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource.hpage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swetake.util.Qrcode;
import com.welink.biz.service.UserService;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;

/**
 * ClassName:QrPage <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月11日 下午6:21:05 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Controller
public class QrPage {
	
	@Resource
    private Env env;
	
	@Resource
    private UserService userService;
	
	@RequestMapping(value = {"/api/h/1.0/qrPage.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        /*String encodeddata = "http://miku.unesmall.com/api/h/1.0/indexPage.htm";
        qrCodeEncodeResponse(encodeddata, response);*/
        return "qrPage";
	}
	
	/**
     * 编码字符串内容到目标httpResponse对象中
     * @param encodeddata
     * @param destFile
     * @throws IOException
     */
    public void qrCodeEncodeResponse(String encodeddata,HttpServletResponse response) throws IOException{
        Qrcode qrcode = new Qrcode();
        //错误修正容量   
        //L水平   7%的字码可被修正  
        //M水平   15%的字码可被修正  
        //Q水平   25%的字码可被修正  
        //H水平   30%的字码可被修正  
        //QR码有容错能力，QR码图形如果有破损，仍然可以被机器读取内容，最高可以到7%~30%面积破损仍可被读取。  
        //相对而言，容错率愈高，QR码图形面积愈大。所以一般折衷使用15%容错能力。
        qrcode.setQrcodeErrorCorrect('M');
        qrcode.setQrcodeEncodeMode('B');
        qrcode.setQrcodeVersion(7);
        byte[] d = encodeddata.getBytes("GBK");
        BufferedImage bi = new BufferedImage(139, 139, BufferedImage.TYPE_INT_RGB);
        // createGraphics
        Graphics2D g = bi.createGraphics();
        // set background
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, 139, 139);
        //设置二维码图片颜色
        g.setColor(Color.BLACK);
 
        if (d.length > 0 && d.length < 123) {
            boolean[][] b = qrcode.calQrcode(d);
            for (int i = 0; i < b.length; i++) {
                for (int j = 0; j < b.length; j++) {
                    if (b[j][i]) {
                        g.fillRect(j * 3 + 2, i * 3 + 2, 3, 3);
                    }
                }
            }
        }
 
        g.dispose();
        bi.flush();
 
        ImageIO.write(bi, "png", response.getOutputStream());
        System.out.println("Input Encoded data is："+encodeddata);
    }
    
    public static void main(String[] args) {
    	a :
		for(int i=0; i<3; i++){
			for(int y=0; y<10; y++){
				if(y==5){
					continue a;
				}
				System.out.print(i+":"+y + "   ");
			}
		}
    	
    	/*for(int y=0; y<10; y++){
			if(y == 5) continue;
			System.out.print("   "+y);
		}*/
    	
	}
	
}

