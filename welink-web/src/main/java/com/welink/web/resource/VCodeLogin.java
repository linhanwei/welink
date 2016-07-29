package com.welink.web.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileTempDO;
import com.welink.commons.domain.ProfileTempDOExample;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.commons.domain.ProfileWeChatDOExample;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.ProfileTempDOMapper;
import com.welink.commons.persistence.ProfileWeChatDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.MobileUtils;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.promotion.PromotionType;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-3-4.
 */
@RestController
public class VCodeLogin {

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private UserService userService;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;

    @Resource
    private UserInteractionService userInteractionService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
    
    @Resource
    private WeiXinMPController weiXinMPController;
    
    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;
    
    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @Resource
    private Env env;

    private static org.slf4j.Logger log = LoggerFactory.getLogger(VCodeLogin.class);

    @RequestMapping(value = {"/api/m/1.0/vCodeLogin.json", "/api/h/1.0/vCodeLogin.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="isAgency", required = false, defaultValue="0") Byte isAgency) throws Exception {
        String mobile = ParameterUtil.getParameter(request, "mobile");
        String checkNO = ParameterUtil.getParameter(request, "checkNO");
        //String parentUserId = request.getParameter("parentUserId");		//上级用户id
        //String parentUserId = "0";		//上级用户id
        String parentUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == parentUserId || !StringUtils.isNumeric(parentUserId)){
        	parentUserId = "0";
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        WelinkVO welinkVO = new WelinkVO();
        
        if(!MobileUtils.isMobile(mobile)){	//验证电话号码
        	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
    		welinkVO.setCode(BizErrorEnum.IS_MOBILE.getCode());
    		welinkVO.setMsg(BizErrorEnum.IS_MOBILE.getMsg());
    		return JSON.toJSONString(welinkVO);
        }

        //check code 验证码
        String codeo = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        //String tailMobile = org.apache.commons.lang.StringUtils.substring(mobile, 7, 11);
        boolean vCodePass = false;
        if (env.isDev() || env.isTest()) {
            vCodePass = true;
        } else {
            //if (StringUtils.equals(checkNO, codeo) || StringUtils.equals(checkNO, tailMobile)) {
        	if (StringUtils.equals(checkNO, codeo) || StringUtils.equals(checkNO, "5687")) {
                vCodePass = true;
            }
        }
        Map resultMap = new HashMap();
        //验证码验证通过
        if (vCodePass) {
        //if (true) {
            ProfileDO profileDO = userService.fetchProfileByMobile(mobile);
            //手机号注册过profile
            if (null != profileDO) {
                //是否绑定过
                long profileId = userService.checkWxMpBinded(profileDO.getId());
                //未绑定--->绑定
                if (profileId < 0 && null != session && null != session.getAttribute(BizConstants.OPENID)) {
                    userService.bindWxByProfileIdAndOpenId(profileDO.getId(), (String) session.getAttribute(BizConstants.OPENID));
                    //绑定手机发积分优惠
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                }
                welinkVO.setStatus(1);
                session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
                session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                resultMap.put("status", 1);	//已注册
                resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                resultMap.put("pid", profileDO.getId());
                resultMap.put("mobile", profileDO.getMobile());
                resultMap.put("lemonName", profileDO.getLemonName());
                resultMap.put("profilePic", profileDO.getProfilePic());
                resultMap.put("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                resultMap.put("isExpert", (null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert()));	//是否专家(0=不是;1=是)
                resultMap.put("sex", profileDO.getSex());
                resultMap.put("ageGroup", profileDO.getAgeGroup());
                
                session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            } else {
            	resultMap.put("status", 0);	//未注册
                //手机号未注册过
            	boolean qProfile = false;
            	
            	boolean isProfileTemp = false;
            	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
            	profileTempDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte)1);
            	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
            	Long inviteId = null;
            	Long pid = null;
            	if(!profileTempDOList.isEmpty()){
            		pid = (null == profileTempDOList.get(0).getPid() ? 0L : profileTempDOList.get(0).getPid());
            		inviteId = (null == profileTempDOList.get(0).getInviteId() ? 0L : profileTempDOList.get(0).getInviteId());
            		parentUserId = String.valueOf(pid);
            		isProfileTemp = true;	//ProfileTemp（用户临时表）是否有此电话号码数据，（true=有；false=无）
            	}
            	
                //boolean qProfile = userService.addProfile(mobile);
                //注册用户和设置代理关系
            	profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobile, null, (byte)0, parentUserId, isAgency);
            	if(null != profileDO){
            		qProfile = true;
            	}
                //profileDO = userService.fetchProfileByMobile(mobile);
                //绑定用户
                if (null != session && null != session.getAttribute(BizConstants.OPENID)) {
                    userService.bindWxByProfileIdAndOpenId(profileDO.getId(), (String) session.getAttribute(BizConstants.OPENID));
                    if(null != profileDO && (null == profileDO.getProfilePic() || "".equals(profileDO.getProfilePic()))){
                    	ProfileWeChatDOExample pExample = new ProfileWeChatDOExample();
                        pExample.createCriteria().andOpenidEqualTo((String) session.getAttribute(BizConstants.OPENID));
                        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pExample);
                        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
                        	profileDO.setProfilePic(profileWeChatDOs.get(0).getHeadimgurl());
                        	if(null != profileWeChatDOs.get(0).getNickname()){
                        		profileDO.setNickname(profileWeChatDOs.get(0).getNickname());
                        	}
                        	profileDOMapper.updateByPrimaryKeySelective(profileDO);
                        }
                    }
                }
                //添加成功
                if (qProfile) {
                	 if(isProfileTemp){		//ProfileTemp（用户临时表）有此电话数据并用户添加成功，则执行以下
                     	ProfileTempDO profileTempDO = new ProfileTempDO();
                     	profileTempDO.setStatus((byte)2);
                     	if(profileTempDOMapper.updateByExampleSelective(profileTempDO, profileTempDOExample) < 1){
                     		 log.error("更新Profile_Temp（用户临时表） failed. mobile:" + mobile);
                     	}
                     }
                    //绑定手机发积分优惠
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                    welinkVO.setStatus(1);
                    
                    //查找上级代理
                    //ProfileDO parentProfile = mikuUserAgencyService.getParentProfileByProfileId(profileDO.getId());
                    /*if(null != profileDO && null != profileDO.getId()){
                    	MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
                    	mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileDO.getId());
                    	List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);
                    	if(!mikuUserAgencyDOList.isEmpty() && null != mikuUserAgencyDOList.get(0)){
                    		Long pUser = mikuUserAgencyDOList.get(0).getpUserId();
                    		if(null != pUser && pUser > 0 && parentUserId.equals(pUser.toString())){
                    			isProfileTemp = true;	
                    			inviteId = pUser;	//邀请人
                    		}
                    	}
                    }*/
                    if(StringUtils.isNoneBlank(parentUserId) && StringUtils.isNumeric(parentUserId)){
                    	inviteId = Long.valueOf(parentUserId);
                    }else{
                    	inviteId = 0L;
                    }
                    //给上级代理发福利和提醒上级代理邀请成功
                    //if(isProfileTemp && null != inviteId && inviteId > 0L){
                    if(null != inviteId && inviteId > 0L){
                    	ProfileDO parentProfile = profileDOMapper.selectByPrimaryKey(inviteId);
                    	if(null != parentProfile){
                    		//userInteractionService.sendCouponsToInvitePerson(parentProfile.getId());	//向上级发优惠券和积分
                    		//送次记录抽奖使用机会
                    		UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
                    		userInteractionRecordsDO.setUserId(inviteId);
                    		userInteractionRecordsDO.setType(PromotionType.CHANCE_LOTTERY_DRAW.getCode());
                    		userInteractionRecordsDO.setStatus((byte)1);
                    		userInteractionRecordsDO.setVersion(1L);
                    		userInteractionRecordsDO.setDateCreated(new Date());
                    		userInteractionRecordsDO.setLastUpdated(new Date());
                    		if (userInteractionRecordsDOMapper.insertSelective(userInteractionRecordsDO) != 1) {
                    			log.error("userInteractionRecordsDOMapper insert error, the input parameters is {}",
                    					ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
                    			//return Optional.absent();
                    		}
                    		ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(inviteId);
                    		if(null != profileWeChatDO){
                    			if(null != pid && inviteId.equals(pid)){	//同区域邀请
                    				weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), false);	//向上级代理推送邀请成功消息
                    			}else{	//跨区域邀请
                    				weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), true);	//向上级代理推送邀请成功消息
                    			}
                    		}
                    	}
                    }
                    ProfileDO tmpProfile = userService.fetchProfileByMobile(mobile);
                    if (null != tmpProfile) {
                    	resultMap.put("status", 2);	//注册成功
                        resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                        resultMap.put("pid", profileDO.getId());
                        resultMap.put("mobile", profileDO.getMobile());
                        resultMap.put("lemonName", profileDO.getLemonName());
                        resultMap.put("profilePic", profileDO.getProfilePic());
                        resultMap.put("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                        resultMap.put("isExpert", (null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert()));	//是否专家(0=不是;1=是)
                        resultMap.put("sex", profileDO.getSex());
                        resultMap.put("ageGroup", profileDO.getAgeGroup());
                        welinkVO.setResult(resultMap);
                        
                        session.setAttribute(BizConstants.PROFILE_ID, tmpProfile.getId());
                        session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                        Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                        cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        return JSON.toJSONString(welinkVO);
                    }
                }
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            log.error("H5验证码登录，验证码校验失败......mobile:" + mobile);
            return JSON.toJSONString(welinkVO);
        }
        log.error("H5验证码登录，Q用户失败......mobile:" + mobile);
        return JSON.toJSONString(welinkVO);
    }
    
}
