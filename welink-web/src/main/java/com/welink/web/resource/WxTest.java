/**
 * Project Name:welink-web
 * File Name:WxTest.java
 * Package Name:com.welink.web.resource
 * Date:2015年12月15日上午10:09:18
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.awt.Color;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sun.misc.BASE64Decoder;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.WxMpCustomMessage;
import com.daniel.weixin.mp.bean.WxMpCustomMessage.WxArticle;
import com.daniel.weixin.mp.bean.WxMpTemplateData;
import com.daniel.weixin.mp.bean.WxMpTemplateMessage;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.util.TimeUtils;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.utils.UpYunUtil;

/**
 * ClassName:WxTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月15日 上午10:09:18 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class WxTest {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(WxTest.class);
	
	@Resource
    private WxMpService wxMpService;
	
	@Resource
    private UserInteractionService userInteractionService;
	
	@Resource
    private WeiXinMPController weiXinMPController;
	
	@RequestMapping(value = {"/api/m/1.0/sendWXMessage.json", "/api/h/1.0/sendWXMessage.json"}, produces = "application/json;charset=utf-8")
	public String sendWXMessage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="openId", required = false) String openId) throws Exception {
		if(null != openId && !"".equals(openId.trim())){
			WxMpCustomMessage message = new WxMpCustomMessage();
			
			// 设置消息的内容等信息
			wxMpService.customMessageSend(WxMpCustomMessage
					  .TEXT()
					  .toUser(openId)
					  .content("请点击：www.baidu.com")
					  .build());
			return "openId success";
		}
		/*WxMpCustomMessage message = new WxMpCustomMessage();
		message
		  .TEXT()
		  .toUser("oKY0xs4gxiJXAyWZPjB8nmyJK7i8")
		  .content("hello chun ge...............")
		  .build();
		// 设置消息的内容等信息
		wxMpService.customMessageSend(message);*/
		
		/*WxMpCustomMessage message2 = new WxMpCustomMessage();
		message2
		  .IMAGE()
		  .toUser("oKY0xs4gxiJXAyWZPjB8nmyJK7i8")
		  //.mediaId("http://wx.qlogo.cn/mmopen/rQRHolqGLykiaiaIiaMup7NmW6umj3jwN5Dia6gje8IX6vfewgM1uz698T7nk5pbRUY8HfEhBiajRB4Vly3RcreaGqXV4cwUCIwHa/0")
		  //.content("hello bing ge...............")
		  .build();
		// 设置消息的内容等信息
		wxMpService.customMessageSend(message2);*/
		
		WxArticle wxArticle = new WxArticle();
		wxArticle.setTitle("lgcTest");
		wxArticle.setPicUrl("http://wx.qlogo.cn/mmopen/rQRHolqGLykiaiaIiaMup7NmW6umj3jwN5Dia6gje8IX6vfewgM1uz698T7nk5pbRUY8HfEhBiajRB4Vly3RcreaGqXV4cwUCIwHa/0");
		
		wxMpService.customMessageSend(WxMpCustomMessage
				  .NEWS()
				  .toUser("oKY0xs4gxiJXAyWZPjB8nmyJK7i8")
				  .addArticle(wxArticle)
				  .build());
		
		/*wxMpService.customMessageSend(WxMpCustomMessage
				  .IMAGE()
				  .toUser("oKY0xs4gxiJXAyWZPjB8nmyJK7i8")
				  .mediaId("http://wx.qlogo.cn/mmopen/rQRHolqGLykiaiaIiaMup7NmW6umj3jwN5Dia6gje8IX6vfewgM1uz698T7nk5pbRUY8HfEhBiajRB4Vly3RcreaGqXV4cwUCIwHa/0")
				  .build());*/
		
		
		return "success";
	}
	
	@RequestMapping(value = {"/api/m/1.0/sendWXTemplateMessage.json", "/api/h/1.0/sendWXTemplateMessage.json"}, produces = "application/json;charset=utf-8")
	public String sendWXTemplateMessage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="openId", required = false) String openId) throws Exception {
		WxMpTemplateMessage templateMessage = new WxMpTemplateMessage();
		//templateMessage.setToUser("oKY0xs4gxiJXAyWZPjB8nmyJK7i8");
		templateMessage.setToUser("oKY0xs-eTAPPcJYZzxsEoV5UGONg");
		//templateMessage.setTemplateId("_e5cXZPeat8_z80ectjId2EKOqvXUtTGYyiySfejf3w");	//加盟通知
		templateMessage.setTemplateId("gT08FnNGzSw7jZl6XUVghz-Lm4eDyoPWKihgQWqrqNg");	//邀请好友注册成功通知
		//templateMessage.setUrl("http://miku.unesmall.com/api/h/1.0/indexPage.htm");
		templateMessage.setUrl("");
		templateMessage.setTopColor(Color.RED.toString());
		templateMessage.getDatas().add(new WxMpTemplateData("first", "00", Color.RED.toString()));
		templateMessage.getDatas().add(new WxMpTemplateData("keyword1", "01", Color.RED.toString()));
		templateMessage.getDatas().add(new WxMpTemplateData("keyword2", "02", Color.RED.toString()));
		templateMessage.getDatas().add(new WxMpTemplateData("keyword3", "03", Color.RED.toString()));
		templateMessage.getDatas().add(new WxMpTemplateData("keyword4", "04", Color.RED.toString()));
		//templateMessage.getDatas().add(new WxMpTemplateData("remark", "非常高兴您来处理这条信息", Color.RED.toString()));
		templateMessage.getDatas().add(new WxMpTemplateData("remark", "非常高兴您来处理这条信息", Color.RED.toString()));
		//templateMessage.getDatas().add(new WxMpTemplateData(name2, value2, color2));

		wxMpService.templateSend(templateMessage);
		
		return "success";
	}
	
	@RequestMapping(value = {"/api/m/1.0/sendCouponsToInvitePerson.json", "/api/h/1.0/sendCouponsToInvitePerson.json"}, produces = "application/json;charset=utf-8")
	public String sendCouponsToInvitePerson(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="userId", required = false) Long userId) throws Exception {
		userInteractionService.sendCouponsToInvitePerson(userId);
		return "success";
	}
	
	@RequestMapping(value = {"/api/m/1.0/sendWXTemplateMessage2.json", "/api/h/1.0/sendWXTemplateMessage2.json"}, produces = "application/json;charset=utf-8")
	public String sendWXTemplateMessage2(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="openId", required = false) String openId) throws Exception {
		ProfileDO profileDO = new ProfileDO();
		profileDO.setId(69539L);
		profileDO.setNickname("lgc");
		profileDO.setMobile("15622395287");
		//userInteractionService.sendCouponsToInvitePerson(userId);
		weiXinMPController.sendWXTemplateMessage(profileDO, openId, false);
		//发送图文消息
		WxArticle wxArticle = new WxArticle();
		wxArticle.setTitle("和小伙伴们一起玩耍吧");
		wxArticle.setDescription("感谢Myron带你开启米酷之旅，长按二维码与他一起玩耍，探索米酷更多好玩的地方吧");
		//wxArticle.setPicUrl("http://wx");
		//wxArticle.setPicUrl("http://wx.qlogo.cn/mmopen/rQRHolqGLykiaiaIiaMup7NmW6umj3jwN5Dia6gje8IX6vfewgM1uz698T7nk5pbRUY8HfEhBiajRB4Vly3RcreaGqXV4cwUCIwHa/0");
		wxArticle.setPicUrl("http://pic.baike.soso.com/p/20131211/20131211091752-393669037.jpg");
		/*wxMpService.customMessageSend(WxMpCustomMessage
				  .NEWS()
				  .toUser("oKY0xs4gxiJXAyWZPjB8nmyJK7i8")
				  //.toUser("oKY0xs-eTAPPcJYZzxsEoV5UGONg")
				  .addArticle(wxArticle)
				  .build());*/
		return "success";
	}
	
	@RequestMapping(value = {"/api/m/1.0/setSessionTest.json", "/api/h/1.0/setSessionTest.json"}, produces = "application/json;charset=utf-8")
	public String setSessionTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setAttribute("lgcmobile", "110");
        Cookie cookieU = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
        cookieU.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
        cookieU.setPath("/");
        response.addCookie(cookieU);
		return "success";
	}
	
	@RequestMapping(value = {"/api/m/1.0/getSessionTest01.json", "/api/h/1.0/getSessionTest01.json"}, produces = "application/json;charset=utf-8")
	public String getSessionTest01(HttpServletRequest request, HttpServletResponse response) throws Exception {
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        String lgcmobile = (String)session.getAttribute("lgcmobile");
		return "success:"+lgcmobile;
	}
	
	@RequestMapping(value = {"/api/m/1.0/getSessionTest02.json", "/api/h/1.0/getSessionTest02.json"}, produces = "application/json;charset=utf-8")
	public String getSessionTest02(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "redirect:/http://120.26.217.212:8080/banner/addBannerHtml";
	}
	
	@RequestMapping(value = {"/api/h/1.0/redirectTest.htm"}, produces = "text/html;charset=utf-8")
    public String redirectTest(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		System.out.println("11111111111111111111111111111111111111111111111111112222");
		response.sendRedirect("http://develop.unesmall.com:8070/");
		return null;
	}
	
	@RequestMapping(value = {"/api/m/1.0/uploadBase64.json", "/api/h/1.0/uploadBase64.json"}, produces = "application/json;charset=utf-8")
	public String uploadBase64(HttpServletRequest request, HttpServletResponse response,
			String imgBase) throws Exception {
		BASE64Decoder decoder = new BASE64Decoder();  
        try   
        {  
            //Base64解码  
            byte[] b = decoder.decodeBuffer(imgBase);  
            for(int i=0;i<b.length;++i)  
            {  
                if(b[i]<0)  
                {//调整异常数据  
                    b[i]+=256;  
                }  
            }  
            UpYunUtil.writePicByBytes(b, "/test/", "test-"+System.currentTimeMillis()+".jpg", null);
        }   
        catch (Exception e){  
        }  
		return imgBase;
	}
	
}

