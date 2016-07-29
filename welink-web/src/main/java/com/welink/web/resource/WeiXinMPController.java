/**
 * Project Name:welink-web
 * File Name:WeiXinMPController.java
 * Package Name:com.welink.web.resource
 * Date:2015年12月15日下午6:24:12
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.WxMpTemplateData;
import com.daniel.weixin.mp.bean.WxMpTemplateMessage;
import com.ibm.icu.text.SimpleDateFormat;
//import com.sun.prism.paint.Color;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;

/**
 * ClassName:WeiXinMPController <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月15日 下午6:24:12 <br/>
 * @author   LuoGuangChun
 */
@RestController
public class WeiXinMPController {
	@Resource
    private WxMpService wxMpService;
	
	/**
	 * 
	 * sendWXTemplateMessage:(发送微信模板信息). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param profileDO
	 * @param openId
	 * @param isOverArea	是否跨区域(true=是；false=不是)
	 * @return
	 * @since JDK 1.6
	 */
	public Boolean sendWXTemplateMessage(ProfileDO profileDO, String openId, boolean isOverArea) {
		try {
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			
			String nickName = "";
			String mobile = "";
			if(null != profileDO){
				nickName =(null == profileDO.getNickname() ? "" : profileDO.getNickname());
				mobile =(null == profileDO.getMobile() ? "" : profileDO.getMobile().trim());
				if(null != mobile && mobile.length() == 11){
					String mobilePre = mobile.substring(0, 3);
					//String mobileMid = mobile.substring(3, 7);
					String mobileEnd = mobile.substring(7, 11);
					mobile = mobilePre+"****"+mobileEnd;
				}
			}
			
			WxMpTemplateMessage templateMessage = new WxMpTemplateMessage();
			templateMessage.setToUser(openId);
			//templateMessage.setTemplateId("_e5cXZPeat8_z80ectjId2EKOqvXUtTGYyiySfejf3w");	//加盟通知
			//templateMessage.setTemplateId("gT08FnNGzSw7jZl6XUVghz-Lm4eDyoPWKihgQWqrqNg");	//丽元堂 邀请好友注册成功通知
			templateMessage.setTemplateId("8-ca2f8RCVnG1ncbyi-NF9w0jOr3L4hmU7RuNbwi6sI");	//米酷mine 邀请好友注册成功通知
			//templateMessage.setUrl(BizConstants.ONLINE_DOMAIN+"/api/h/1.0/myAlly.htm");
			//templateMessage.setUrl(BizConstants.ONLINE_DOMAIN+"/api/h/1.0/hActive.htm?page=lottery");
			templateMessage.setTopColor("#000000");
			/*if(isOverArea){	//跨区域邀请
				templateMessage.getDatas().add(new WxMpTemplateData("first", "邀请盟友失败，不能跨区域邀请，^o^ \n", "#000000"));
				templateMessage.getDatas().add(new WxMpTemplateData("remark", "\n\""+nickName+"\"已经接受你的邀请，因你们不属于同一个区域所以不能成为盟友，但恭喜获得一次抽取大奖的机会，点击马上抽奖", "#000000"));
			}else{	//不是跨区域邀请
				templateMessage.getDatas().add(new WxMpTemplateData("first", "又成功邀请一枚新盟友啦，么么哒^o^ \n", "#000000"));
				templateMessage.getDatas().add(new WxMpTemplateData("remark", "\n\""+nickName+"\"已经接受你的邀请，恭喜获得一次抽取大奖的机会，点击马上抽奖", "#000000"));
			}*/
			templateMessage.getDatas().add(new WxMpTemplateData("first", "又成功邀请一枚好友啦，么么哒^o^ \n", "#000000"));
			//templateMessage.getDatas().add(new WxMpTemplateData("remark", "\n\""+nickName+"\"已经接受你的邀请，恭喜获得一次抽取大奖的机会，点击马上抽奖", "#000000"));
			templateMessage.getDatas().add(new WxMpTemplateData("remark", "\n\""+nickName+"\"已经接受你的邀请", "#000000"));
			
			templateMessage.getDatas().add(new WxMpTemplateData("keyword1", nickName+"("+mobile+")", "#000000"));
			templateMessage.getDatas().add(new WxMpTemplateData("keyword2", sdf.format(now), "#000000"));
			//templateMessage.getDatas().add(new WxMpTemplateData(name2, value2, color2));
			
			wxMpService.templateSend(templateMessage);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	public static void main(String[] args) {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		System.out.println(""+sdf.format(now));
	}
}

