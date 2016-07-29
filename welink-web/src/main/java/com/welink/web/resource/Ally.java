/**
 * Project Name:welink-web
 * File Name:Ally.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月3日下午8:00:32
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.utils.BigDecimalUtils;
import com.welink.commons.vo.MikuAgencyShareAccountVO;

/**
 * ClassName:Ally <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月3日 下午8:00:32 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class Ally {
	
	static Logger log = LoggerFactory.getLogger(Ally.class);
	
	@Resource
    private ProfileDOMapper profileDOMapper;
	
	@Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
	
	@Resource
    private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	/**
     * 
     * inviteAlly:(邀请盟友). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/inviteAlly.json", "/api/h/1.0/inviteAlly.json"}, produces = "application/json;charset=utf-8")
    public String inviteAlly(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //para
        long profileId = -1l;
        long point = 0;
        long couponCount = 0;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        Map resultMap = new HashMap();
     
        return null;
	}
	
	/**
     * 
     * myAlly:(我的盟友). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/myAlly.json", "/api/h/1.0/myAlly.json"}, produces = "application/json;charset=utf-8")
    public String myAlly(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //para
        long profileId = -1l;
        Integer isAgency = 0;
        long point = 0;
        long couponCount = 0;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        try {
        	if(profileId > 0){
        		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        		if(null != profileDO){
        			isAgency = (null == profileDO.getIsAgency() ? 0 :  Integer.valueOf(profileDO.getIsAgency()));
        		}
        	}
        } catch (Exception e) {
        	isAgency = 0;
            log.info("user not isAgency");
        }
        
        if(isAgency != 1){
        	welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_AGENCY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_AGENCY.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        Map resultMap = new HashMap();
        
        int zjAllyCount = mikuAgencyShareAccountDOMapper.selectAllyByParamCount(1, profileId);	//统计直接盟友人数
        int jjAllyCount = mikuAgencyShareAccountDOMapper.selectAllyByParamCount(2, profileId);	//统计间接盟友人数
        resultMap.put("zjAllyCount", zjAllyCount);		//统计直接盟友人数
        resultMap.put("jjAllyCount", jjAllyCount);		//统计间接盟友人数
        
        List<MikuAgencyShareAccountVO> mikuAgencyShareAccountVOList = 
        		mikuAgencyShareAccountDOMapper.selectAllyByParam(1, profileId, null,null,"pro.date_created DESC",10,0);
        
        for(int i=0; i<mikuAgencyShareAccountVOList.size(); i++){
        	setFee(mikuAgencyShareAccountVOList.get(i));	//设置金额分为元
        }
        resultMap.put("list", mikuAgencyShareAccountVOList);
        
        /*resultMap.put("zjAllyCount", 1000);		//统计直接盟友人数
        resultMap.put("jjAllyCount", 2000);		//统计间接盟友人数
        List<MikuAgencyShareAccountVO> mikuAgencyShareAccountModelList = new ArrayList<MikuAgencyShareAccountVO>();
        mikuAgencyShareAccountModel(mikuAgencyShareAccountModelList);
        welinkVO.setStatus(1);
        resultMap.put("list", mikuAgencyShareAccountModelList);*/
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * myInOrDirectAlly:(我的直接或间接盟友). <br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param type	查询类型(1=直接盟友，2=间接盟友)
	 * @param pg
	 * @param sz
	 * @return
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/myInOrDirectAlly.json", "/api/h/1.0/myInOrDirectAlly.json"}, produces = "application/json;charset=utf-8")
    public String myInOrDirectAlly(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="type", required = true) Integer type,
    		@RequestParam(value="nickName", required = false) String nickName,
    		@RequestParam(value="mobile", required = false) String mobile,
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
        //para
        long profileId = -1l;
        Integer isAgency = 0;
        long point = 0;
        long couponCount = 0;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        try {
        	if(profileId > 0){
        		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        		if(null != profileDO){
        			isAgency = (null == profileDO.getIsAgency() ? 0 :  Integer.valueOf(profileDO.getIsAgency()));
        		}
        	}
        } catch (Exception e) {
        	isAgency = 0;
            log.info("user not isAgency");
        }
        
        if(isAgency != 1){
        	welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_AGENCY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_AGENCY.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        Map resultMap = new HashMap();
        int allyCount = mikuAgencyShareAccountDOMapper.selectAllyByParamCount(type, profileId);	//统计盟友人数
        List<MikuAgencyShareAccountVO> mikuAgencyShareAccountVOList = null;
        resultMap.put("allyCount", allyCount);		//统计盟友人数
        boolean hasNext = true;
        if(allyCount > 0){
        	mikuAgencyShareAccountVOList 
        		= mikuAgencyShareAccountDOMapper.selectAllyByParam(type, profileId, nickName, mobile, "  contactsLevel asc,pro.date_created DESC  ",size,startRow);
        	if (null != mikuAgencyShareAccountVOList && mikuAgencyShareAccountVOList.size() < size) {
        		hasNext = false;
        	} else {
        		hasNext = true;
        	}
        	for(int i=0; i<mikuAgencyShareAccountVOList.size(); i++){
        		setFee(mikuAgencyShareAccountVOList.get(i));	//设置金额分为元
        	}
        }else{
        	hasNext = false;
        }
        
        welinkVO.setStatus(1);
        resultMap.put("list", mikuAgencyShareAccountVOList);
        resultMap.put("hasNext", hasNext);
        
        /*List<MikuAgencyShareAccountVO> mikuAgencyShareAccountModelList = new ArrayList<MikuAgencyShareAccountVO>();
        mikuAgencyShareAccountModel(mikuAgencyShareAccountModelList);
        welinkVO.setStatus(1);
        
        
        resultMap.put("list", mikuAgencyShareAccountModelList);
        resultMap.put("hasNext", false);*/
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	//设置金额分为元
	private void setFee(MikuAgencyShareAccountVO mikuAgencyShareAccountVO){
		if(null != mikuAgencyShareAccountVO){
			if(null != mikuAgencyShareAccountVO.getContactsLevel() 
					&& mikuAgencyShareAccountVO.getContactsLevel().equals(1)){
				
			}else{
				String mobile = mikuAgencyShareAccountVO.getMobile();
				if(null != mobile && mobile.length() == 11){
					String mobilePre = mobile.substring(0, 3);
					//String mobileMid = mobile.substring(3, 7);
					String mobileEnd = mobile.substring(7, 11);
					mikuAgencyShareAccountVO.setMobile(mobilePre+"****"+mobileEnd);
				}
			}
			Long getpayingFee = Long.valueOf(null == mikuAgencyShareAccountVO.getGetpayingFee() ? "0" : mikuAgencyShareAccountVO.getGetpayingFee());
			Long noGetpayFee = Long.valueOf(null == mikuAgencyShareAccountVO.getNoGetpayFee() ? "0" : mikuAgencyShareAccountVO.getNoGetpayFee());
			String cangetFee = String.valueOf(noGetpayFee-getpayingFee);
			mikuAgencyShareAccountVO.setCanGetpayFee(BigDecimalUtils.divFee100(cangetFee));
			mikuAgencyShareAccountVO.setDirectSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getDirectSalesFee()));
			mikuAgencyShareAccountVO.setDirectShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getDirectShareFee()));
			mikuAgencyShareAccountVO.setIndirectSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getIndirectSalesFee()));
			mikuAgencyShareAccountVO.setIndirectShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getIndirectShareFee()));
			mikuAgencyShareAccountVO.setGetpayingFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getGetpayingFee()));
			mikuAgencyShareAccountVO.setNoGetpayFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getNoGetpayFee()));
			mikuAgencyShareAccountVO.setTotalGotpayFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getTotalGotpayFee()));
			mikuAgencyShareAccountVO.setpSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getpSalesFee()));
			mikuAgencyShareAccountVO.setTotalShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getTotalShareFee()));
			mikuAgencyShareAccountVO.setpOfferFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getpOfferFee()));
		}
	}
	
	/*public void mikuAgencyShareAccountModel(List<MikuAgencyShareAccountVO> list){
		for(int i=0; i<5; i++){
			MikuAgencyShareAccountVO model = new MikuAgencyShareAccountVO();
			model.setId(1L);
			model.setNickName("代理"+i);
			model.setAgencyId(1L);
			model.setDirectSalesFee(100L);
			model.setDirectShareFee(100L);
			model.setGetpayingFee(100L);
			model.setIndirectSalesFee(100L);
			model.setIndirectShareFee(100L);
			model.setNoGetpayFee(100L);
			model.setpOfferFee(100L);
			model.setpSalesFee(100L);
			model.setTotalGotpayFee(100L);
			model.setTotalShareFee(100L);
			list.add(model);
		}
	}*/
	
}

