/**
 * Project Name:welink-web
 * File Name:MikuSalesRecord.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月2日下午7:07:42
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import sun.misc.BASE64Decoder;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.MikuFaceScoreExchangeService;
import com.welink.commons.domain.MikuFaceScoreExchangeDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuFaceScoreExchangeDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.utils.UpYunUtil;

/**
 * ClassName:MikuSalesRecord <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午7:07:42 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class MikuFaceScoreExchangeAction {
	
	@Resource
	private MikuFaceScoreExchangeDOMapper mikuFaceScoreExchangeDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuFaceScoreExchangeService mikuFaceScoreExchangeService;
	
	@Resource
    private MemcachedClient memcachedClient;
	
	/**
	 * 
	 * faceScore:(颜值分计算). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param file
	 * @return
	 * @throws Exception
	 * @since JDK 1.6
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/faceScore.json", "/api/h/1.0/faceScore.json"}, produces = "application/json;charset=utf-8")
	public String faceScore(HttpServletRequest request, HttpServletResponse response,
			String imgStr) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		// 判断文件是否为空
		if (null != profileDO && null != imgStr) {
			
		}
		MikuFaceScoreExchangeDO mikuFaceScoreExchangeDO = new MikuFaceScoreExchangeDO();
		//mikuFaceScoreExchangeDO.setId(1L);
		//mikuFaceScoreExchangeDO.setPicUrl("http://wx.qlogo.cn/mmopen/0w44rZsqE6YDZYZfPq2JxH3oLkYFFQZRRKJxYuRdgb2LS9p6gjIjJ4Y1DBP9icXy7LkBAZ5GuUPdmTITuIzadSa7zzDhlXFlO/0");
		mikuFaceScoreExchangeDO.setAge(20);
		mikuFaceScoreExchangeDO.setSex((byte)0);
		mikuFaceScoreExchangeDO.setFaceScore(95);
		mikuFaceScoreExchangeDO.setPiont(95);
		//mikuFaceScoreExchangeDO.setDateCreated(new Date());
		//mikuFaceScoreExchangeDO.setUserId(profileId);
		if(null != memcachedClient){
			memcachedClient.set(mikuFaceScoreExchangeService.FACE_SCORE_EXCHANGE+profileId, TimeConstants.REDIS_EXPIRE_SECONDS_5, mikuFaceScoreExchangeDO);
		}
		resultMap.put("do", mikuFaceScoreExchangeDO);
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * faceScoreExchange:(颜值兑换). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param imgStr	图片base64值
	 * @return
	 * @throws Exception
	 * @since JDK 1.6
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/faceScoreExchange.json", "/api/h/1.0/faceScoreExchange.json"}, produces = "application/json;charset=utf-8")
	public String faceScoreExchange(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="imgStr", required = false) String imgStr) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		//ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);

		//颜值兑换
		welinkVO = mikuFaceScoreExchangeService.faceScoreExchange(profileId, imgStr);	
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * oneBuyList:(颜值兑换排行榜). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param isGetpays
	 * @param pg
	 * @param sz
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = {"/api/m/1.0/faceScoreExchangeList.json", "/api/h/1.0/faceScoreExchangeList.json"}, produces = "application/json;charset=utf-8")
	public String faceScoreExchangeList(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		
		/*MikuFaceScoreExchangeDOExample mikuFaceScoreExchangeDOExample = new MikuFaceScoreExchangeDOExample();
		mikuFaceScoreExchangeDOExample.setOffset(startRow);
		mikuFaceScoreExchangeDOExample.setLimit(size);
		mikuFaceScoreExchangeDOExample.setOrderByClause("date_created desc");
		List<MikuFaceScoreExchangeDO> mikuFaceScoreExchangeDOs = mikuFaceScoreExchangeDOMapper.selectByExample(mikuFaceScoreExchangeDOExample);*/
		
		List<MikuFaceScoreExchangeDO> mikuFaceScoreExchangeDOs = new ArrayList<MikuFaceScoreExchangeDO>();
		MikuFaceScoreExchangeDO mikuFaceScoreExchangeDO = new MikuFaceScoreExchangeDO();
		mikuFaceScoreExchangeDO.setId(1L);
		mikuFaceScoreExchangeDO.setPicUrl("http://wx.qlogo.cn/mmopen/0w44rZsqE6YDZYZfPq2JxH3oLkYFFQZRRKJxYuRdgb2LS9p6gjIjJ4Y1DBP9icXy7LkBAZ5GuUPdmTITuIzadSa7zzDhlXFlO/0");
		mikuFaceScoreExchangeDO.setAge(20);
		mikuFaceScoreExchangeDO.setSex((byte)0);
		mikuFaceScoreExchangeDO.setFaceScore(95);
		mikuFaceScoreExchangeDO.setPiont(95);
		mikuFaceScoreExchangeDO.setUserName("优理氏");
		mikuFaceScoreExchangeDO.setMobile("12345678911");
		mikuFaceScoreExchangeDO.setDateCreated(new Date());
		
		MikuFaceScoreExchangeDO mikuFaceScoreExchangeDO2 = new MikuFaceScoreExchangeDO();
		mikuFaceScoreExchangeDO2.setId(2L);
		mikuFaceScoreExchangeDO2.setPicUrl("http://wx.qlogo.cn/mmopen/ibulS1Kxia35JbUO3hHiboBzGEqBpF5eDsRibmXv79kIxjbCiaG40NGCM4tG3VJ8ibe5qr5rv30ibXWCDsQtYqRorTfI2n6yRoePrpf/0");
		mikuFaceScoreExchangeDO2.setAge(22);
		mikuFaceScoreExchangeDO2.setSex((byte)0);
		mikuFaceScoreExchangeDO2.setFaceScore(94);
		mikuFaceScoreExchangeDO2.setPiont(94);
		
		mikuFaceScoreExchangeDO.setUserName("优理氏2");
		mikuFaceScoreExchangeDO.setMobile("12345678922");
		mikuFaceScoreExchangeDO.setDateCreated(new Date());
		
		mikuFaceScoreExchangeDOs.add(mikuFaceScoreExchangeDO);
		resultMap.put("list", mikuFaceScoreExchangeDOs);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
}

