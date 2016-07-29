/**
 * Project Name:welink-web
 * File Name:MikuWalletAction.java
 * Package Name:com.welink.web.resource
 * Date:2016年2月27日下午3:43:32
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.domain.MikuWalletDO;
import com.welink.commons.domain.MikuWalletDOExample;
import com.welink.commons.domain.MikuWalletOriginDO;
import com.welink.commons.domain.MikuWalletOriginDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuWalletDOMapper;
import com.welink.commons.persistence.MikuWalletOriginDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;

/**
 * ClassName:MikuWalletAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年2月27日 下午3:43:32 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class MikuWalletAction {
	
	@Resource
	private MikuWalletDOMapper mikuWalletDOMapper;
	
	@Resource
	private MikuWalletOriginDOMapper mikuWalletOriginDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	/**
	 * 
	 * getMyWallet:(我的钱包). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getMyWallet.json", "/api/h/1.0/getMyWallet.json"}, produces = "application/json;charset=utf-8")
	public String getMyWallet(HttpServletRequest request, HttpServletResponse response,
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
		welinkVO.setStatus(1);
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		
		MikuWalletDOExample mikuWalletDOExample = new MikuWalletDOExample();
		mikuWalletDOExample.createCriteria().andUserIdEqualTo(profileId);
		List<MikuWalletDO> mikuWalletDOList = mikuWalletDOMapper.selectByExample(mikuWalletDOExample);
		if(null != mikuWalletDOList && !mikuWalletDOList.isEmpty()){
			resultMap.put("balanceFee", mikuWalletDOList.get(0).getBalanceFee());	//余额
			MikuWalletOriginDOExample mikuWalletOriginDOExample = new MikuWalletOriginDOExample();
			mikuWalletOriginDOExample.createCriteria().andUserIdEqualTo(profileId);
			mikuWalletOriginDOExample.setOffset(startRow);
			mikuWalletOriginDOExample.setLimit(size);
			mikuWalletOriginDOExample.setOrderByClause(" date_created DESC ");
			List<MikuWalletOriginDO> mikuWalletOriginDOList = mikuWalletOriginDOMapper.selectByExample(mikuWalletOriginDOExample);
			
			/*resultMap.put("balanceFee", 10L);	//余额
			List<MikuWalletOriginDO> mikuWalletOriginDOList = new ArrayList<MikuWalletOriginDO>();
			MikuWalletOriginDO mikuWalletOriginDO = new MikuWalletOriginDO();
			mikuWalletOriginDO.setGetpayStatus((byte)-1);
			mikuWalletOriginDO.setDateCreated(new Date());
			mikuWalletOriginDO.setId(1L);
			mikuWalletOriginDO.setLastUpdated(new Date());
			mikuWalletOriginDO.setOriginId(112233L);
			mikuWalletOriginDO.setTotalFee(10L);
			mikuWalletOriginDO.setType((byte)1);
			mikuWalletOriginDO.setUserId(11L);
			mikuWalletOriginDO.setVersion(0L);
			mikuWalletOriginDOList.add(mikuWalletOriginDO);*/
			
			boolean hasNext = true;
			if (null != mikuWalletOriginDOList && mikuWalletOriginDOList.size() < size) {
				hasNext = false;
			}else if(null == mikuWalletOriginDOList){
				hasNext = false;
			}else{
				hasNext = true;
			}
			welinkVO.setStatus(1);
			resultMap.put("walletOrigins", mikuWalletOriginDOList);
			resultMap.put("hasNext", hasNext);
			welinkVO.setResult(resultMap);
			//return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
			return JSON.toJSONString(welinkVO);
		}else{
			ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
			if(null != profileDO){
				MikuWalletDO mikuWalletDO = setMikuWalletDO(profileDO);
				int insertSelective = mikuWalletDOMapper.insertSelective(mikuWalletDO);		//插入我的钱包
			}
		}
		
        return JSON.toJSONString(welinkVO);
	}
	
	//初始化钱包用于注册时
	public MikuWalletDO setMikuWalletDO(ProfileDO profileDO) {
		Date nowDate = new Date();
		MikuWalletDO mikuWalletDO = new MikuWalletDO();
		mikuWalletDO.setBalanceFee(0L);
		mikuWalletDO.setGetpayedFee(0L);
		mikuWalletDO.setGetpayingFee(0L);
		mikuWalletDO.setMobile(profileDO.getMobile());
		mikuWalletDO.setUserId(profileDO.getId());
		mikuWalletDO.setVersion(0L);
		mikuWalletDO.setDateCreated(nowDate);
		mikuWalletDO.setLastUpdated(nowDate);
		return mikuWalletDO;
	}
	
}

