package com.welink.web.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.cache.CheckNOGenerator;
import com.welink.biz.common.cache.CheckNOValidator;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.constants.ProfileStatusEnum;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.BCrypt;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.ProfileExtService;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuUserAgencyDO;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.ProfileExtDO;
import com.welink.commons.domain.ProfileExtDOExample;
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
import com.welink.commons.tacker.EventTracker;
import com.welink.commons.utils.MobileUtils;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.promotion.PromotionType;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 14-9-9.
 */
@RestController
public class Register {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(Register.class);

    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;

    @Resource
    private ProfileExtService profileExtService;

    @Resource
    private UserInteractionService userInteractionService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private CheckNOValidator checkNOValidator;
    
    @Resource
    private WeiXinMPController weiXinMPController;
    
    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
    
    @Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;

    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param isAgency	(0=不是代理；1=是代理)
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @RequestMapping(value = {"/api/m/1.0/register2.json", "/api/h/1.0/register2.json"}, produces = "application/json;charset=utf-8")
    public String register2(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="isAgency", required = false, defaultValue="0") Byte isAgency) throws Exception {
        //get params
        String mobileNum = request.getParameter("mobile");
        String pswd = request.getParameter("pswd");
        String hpswd = request.getParameter("hp");
        String checkNO = request.getParameter("checkNum");
        String ip = request.getParameter("ip");
        String deviceId = request.getParameter("deviceId");
        //String parentUserId = request.getParameter("parentUserId");		//上级用户id
        //String parentUserId = "0";		//上级用户id
        String parentUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == parentUserId || !StringUtils.isNumeric(parentUserId)){
        	parentUserId = "0";
        }
        WelinkAgent welinkAgent = new WelinkAgent();
        ResponseResult result = new ResponseResult();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        //获取用户设备信息
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        try {
            EventTracker.track(mobileNum, "register", "register-action", "regiser-pre", 1L);
        } catch (Exception e) {
            log.error("register mobile error mobile:" + mobileNum);
        }
        //根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobileNum).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.REGISTED_YET.getCode());
            welinkVO.setMsg(BizErrorEnum.REGISTED_YET.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        String tailMobile = org.apache.commons.lang.StringUtils.substring(mobileNum, 7, 11);
        //if (checkNOValidator.checkNOisValid(checkNO, mobileNum)||StringUtils.equals(checkNO,tailMobile)) {//注册特殊验证码
        if (null != checkNO && checkNOValidator.checkNOisValid(checkNO, mobileNum)) {//注册特殊验证码
            //checkNOOK = true;
        } else {
            //checkNOOK = false;
            log.error("注册放开验证码 验证码校验失败... mobile:"+mobileNum+",checkNUM:"+checkNO);
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            log.warn("用户注册过程 验证码校验失败. mobile " + mobileNum);
            return JSON.toJSONString(welinkVO);
        }
        //密码操作 -- h5
        boolean isH5 = false;
        if (StringUtils.isBlank(pswd)) {
            if (StringUtils.isNotBlank(hpswd)) {
                pswd = hpswd;
                byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
                pswd = new String(pswdArray);
                isH5 = true;
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                log.error("password param is blank...  mobile:" + mobileNum);
                return JSON.toJSONString(welinkVO);
            }
        }
        String dePswd = PasswordParser.parserPlanPswd(pswd, null, isH5);
        String toStorePassword = BCrypt.hashpw(dePswd, BCrypt.gensalt());

        com.welink.web.common.filter.Profiler.enter("register action");
        ProfileDO profileDO = null;
        byte deplom = 0;
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
            if (null != welinkAgent) {
                if (StringUtils.equals(BizConstants.PRE_CLIENT_FLAG, welinkAgent.getAppbundle())) {
                    deplom = 1;
                }
            }
        } catch (Exception e) {
            log.error("parse welinkAgent failed. mobile:" + mobileNum);
        }

        try {
        	boolean isProfileTemp = false;
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobileNum).andStatusEqualTo((byte)1);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		Long pid = (null == profileTempDOList.get(0).getPid() ? 0L : profileTempDOList.get(0).getPid());
        		parentUserId = String.valueOf(pid);
        		isProfileTemp = true;
        	}
        	
            //profileDO = addProfile(-1, mobileNum, toStorePassword, deplom);
        	//注册用户和设置代理关系
        	profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobileNum, toStorePassword, deplom, parentUserId, isAgency);
            if (null != profileDO) {
                resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                if(isProfileTemp){		//更新ProfileTemp（用户临时表）的状态，有此电话数据并用户添加成功，则执行以下
                	ProfileTempDO profileTempDO = new ProfileTempDO();
                	profileTempDO.setStatus((byte)2);
                	if(profileTempDOMapper.updateByExampleSelective(profileTempDO, profileTempDOExample) < 1){
                		 log.error("更新Profile_Temp（用户临时表） failed. mobile:" + mobileNum);
                	}
                }
                
                doLogin(mobileNum, toStorePassword, profileDO.getId(), response);
                //给新用户发积分和优惠券
                userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                if (null != welinkAgent) {
                    recordProfileExtInfo(welinkAgent.getBuildVersion(), welinkAgent.getVersion(), welinkAgent.getScale(), welinkAgent.getMode(), welinkAgent.getPlatform(), ip, welinkAgent.getSystemVersion(), deviceId, profileDO.getId());
                }
                
                Long inviteId = 0L;
                if(StringUtils.isNotBlank(parentUserId) && StringUtils.isNumeric(parentUserId)){
                	inviteId = Long.valueOf(parentUserId);
                }else{
                	inviteId = 0L;
                }
                
              //查找上级代理
                //ProfileDO parentProfile = mikuUserAgencyService.getParentProfileByProfileId(profileDO.getId());
                //给上级代理发福利和提醒上级代理邀请成功
                //if(null != parentProfile && String.valueOf(parentProfile.getId()).equals(parentUserId)){
                if(null != inviteId && inviteId > 0L){
                	ProfileDO parentProfile = profileDOMapper.selectByPrimaryKey(inviteId);
                	if(null != parentProfile){
                		//userInteractionService.sendCouponsToInvitePerson(parentProfile.getId());	//向上级发优惠券和积分
                		//送次记录抽奖使用机会
                		UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
                		userInteractionRecordsDO.setUserId(parentProfile.getId());
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
                		ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(parentProfile.getId());
                		if(null != profileWeChatDO){
                			weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), true);	//向上级代理推送邀请成功消息
                		}
                	}
                }
                /*else if(isProfileTemp && StringUtils.isNumeric(parentUserId) && Long.valueOf(parentUserId) > 0
                		&& null != parentProfile && !String.valueOf(parentProfile.getId()).equals(parentUserId)){	//跨区邀请发福利
            		//送次记录抽奖使用机会
            		UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
            		userInteractionRecordsDO.setUserId(Long.valueOf(parentUserId));
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
            		ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(Long.valueOf(parentUserId));
            		if(null != profileWeChatDO){
            			weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), true);	//向上级代理推送邀请成功消息
            		}
                }*/
                
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        } catch (Exception e) {
            log.error("register failed. mobile:" + mobileNum + "," + e.getMessage());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //role
        EventTracker.track(profileDO.getMobile(), "register", "register-action", "register-success", 1L);
        result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        resultMap.put("profile_pic", profileDO.getProfilePic());
        resultMap.put("lemon_name", profileDO.getNickname());
        resultMap.put("isExpert", (null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert()));	//是否专家(0=不是;1=是)
        resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
        resultMap.put("pid", PhenixUserHander.encodeUserId(profileDO.getId()));
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 
     * register2:(这里用一句话描述这个方法的作用). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param isAgency	(0=不是代理；1=是代理)
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @RequestMapping(value = {"/api/m/1.0/register.json", "/api/h/1.0/register.json"}, produces = "application/json;charset=utf-8")
    public String register(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="isAgency", required = false, defaultValue="0") Byte isAgency) throws Exception {
        //get params
        String mobileNum = request.getParameter("mobile");
        String pswd = request.getParameter("pswd");
        String hpswd = request.getParameter("hp");
        String checkNO = request.getParameter("checkNum");
        String ip = request.getParameter("ip");
        String deviceId = request.getParameter("deviceId");
        //String parentUserId = request.getParameter("parentUserId");		//上级用户id
        String parentUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == parentUserId || !StringUtils.isNumeric(parentUserId)){
        	parentUserId = "0";
        }
        WelinkAgent welinkAgent = new WelinkAgent();
        ResponseResult result = new ResponseResult();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        if(!MobileUtils.isMobile(mobileNum)){	//验证电话号码
        	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
    		welinkVO.setCode(BizErrorEnum.IS_MOBILE.getCode());
    		welinkVO.setMsg(BizErrorEnum.IS_MOBILE.getMsg());
    		return JSON.toJSONString(welinkVO);
        }
        //获取用户设备信息
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        try {
            EventTracker.track(mobileNum, "register", "register-action", "regiser-pre", 1L);
        } catch (Exception e) {
            log.error("register mobile error mobile:" + mobileNum);
        }
        //根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobileNum).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
    		welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
    		welinkVO.setCode(BizErrorEnum.REGISTED_YET.getCode());
    		welinkVO.setMsg(BizErrorEnum.REGISTED_YET.getMsg());
    		return JSON.toJSONString(welinkVO);
        }
        //String tailMobile = org.apache.commons.lang.StringUtils.substring(mobileNum, 7, 11);
        /*if (checkNOValidator.checkNOisValid(checkNO, mobileNum)||StringUtils.equals(checkNO,tailMobile)) {//注册特殊验证码
            checkNOOK = true;
        } else {
            checkNOOK = false;
            log.error("注册放开验证码 验证码校验失败... mobile:"+mobileNum+",checkNUM:"+checkNO);
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            result.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            log.warn("用户注册过程 验证码校验失败. mobile " + mobileNum);
            return SUCCESS;
        }*/
        //密码操作 -- h5
        boolean isH5 = false;
        if (StringUtils.isBlank(pswd)) {
            if (StringUtils.isNotBlank(hpswd)) {
                pswd = hpswd;
                byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
                pswd = new String(pswdArray);
                isH5 = true;
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                log.error("password param is blank...  mobile:" + mobileNum);
                return JSON.toJSONString(welinkVO);
            }
        }
        String dePswd = PasswordParser.parserPlanPswd(pswd, null, isH5);
        String toStorePassword = BCrypt.hashpw(dePswd, BCrypt.gensalt());

        com.welink.web.common.filter.Profiler.enter("register action");
        ProfileDO profileDO = null;
        byte deplom = 0;
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
            if (null != welinkAgent) {
                if (StringUtils.equals(BizConstants.PRE_CLIENT_FLAG, welinkAgent.getAppbundle())) {
                    deplom = 1;
                }
            }
        } catch (Exception e) {
            log.error("parse welinkAgent failed. mobile:" + mobileNum);
        }

        try {
        	boolean isProfileTemp = false;
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobileNum).andStatusEqualTo((byte)1);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	Long inviteId = null;
        	Long pid = null;
        	if(!profileTempDOList.isEmpty()){
        		pid = (null == profileTempDOList.get(0).getPid() ? 0L : profileTempDOList.get(0).getPid());
        		inviteId = (null == profileTempDOList.get(0).getInviteId() ? 0L : profileTempDOList.get(0).getInviteId());
        		parentUserId = String.valueOf(pid);
        		isProfileTemp = true;	//ProfileTemp（用户临时表）是否有此电话号码数据，（true=有；false=无）
        	}
        	
            //profileDO = addProfile(-1, mobileNum, toStorePassword, deplom);
        	//注册用户和设置代理关系
        	profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobileNum, toStorePassword, deplom, parentUserId, isAgency);
            if (null != profileDO && profileDO.getId() > 0) {
                resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                if(isProfileTemp){		//ProfileTemp（用户临时表）有此电话数据并用户添加成功，则执行以下
                	ProfileTempDO profileTempDO = new ProfileTempDO();
                	profileTempDO.setStatus((byte)2);
                	if(profileTempDOMapper.updateByExampleSelective(profileTempDO, profileTempDOExample) < 1){
                		 log.error("更新Profile_Temp（用户临时表） failed. mobile:" + mobileNum);
                	}
                }
                doLogin(mobileNum, toStorePassword, profileDO.getId(), response);
                //给新用户发积分和优惠券
                userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                if (null != welinkAgent) {
                    recordProfileExtInfo(welinkAgent.getBuildVersion(), welinkAgent.getVersion(), welinkAgent.getScale(), welinkAgent.getMode(), welinkAgent.getPlatform(), ip, welinkAgent.getSystemVersion(), deviceId, profileDO.getId());
                }
                
                //查找上级代理
                //ProfileDO parentProfile = mikuUserAgencyService.getParentProfileByProfileId(profileDO.getId());
                if(null != profileDO && null != profileDO.getId()){
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
                }
                //给上级代理发福利和提醒上级代理邀请成功
                if(isProfileTemp && null != inviteId && inviteId > 0L){
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
                		//weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), false);	//向上级代理推送邀请成功消息
                		if(null != pid && inviteId.equals(pid)){	//同区域邀请
                			weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), false);	//向上级代理推送邀请成功消息
                		}else{	//跨区域邀请
                			weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), true);	//向上级代理推送邀请成功消息
                		}
                	}
                }
            } else {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        } catch (Exception e) {
            log.error("register failed. mobile:" + mobileNum + "," + e.getMessage());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //role
        EventTracker.track(profileDO.getMobile(), "register", "register-action", "register-success", 1L);
        result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        resultMap.put("profile_pic", profileDO.getProfilePic());
        resultMap.put("lemon_name", profileDO.getNickname());
        resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
        resultMap.put("pid", profileDO.getId());
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
	private void recordProfileExtInfo(String buildVersion, String version, String scale, String os, String plateform, String ip, String osVersion, String deviceId, long profileId) {
        ProfileExtDO profileExtDO = new ProfileExtDO();
        ProfileExtDOExample profileExtDOExample = new ProfileExtDOExample();
        profileExtDOExample.createCriteria().andProfileIdEqualTo(profileId);
        if (org.apache.commons.lang.StringUtils.isNotBlank(plateform)) {
            profileExtDO.setPlateform(plateform);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(os)) {
            profileExtDO.setOs(os);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(scale)) {
            profileExtDO.setScale(scale);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(osVersion)) {
            profileExtDO.setOsVersion(osVersion);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(ip)) {
            profileExtDO.setIp(ip);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(deviceId)) {
            profileExtDO.setDeviceId(deviceId);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(version)) {
            profileExtDO.setDeviceId(version);
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(buildVersion)) {
            profileExtDO.setBuildVersion(buildVersion);
        }

        profileExtDO.setProfileId(profileId);
        profileExtDO.setLoginTime(new Date());
        profileExtDO.setLastUpdated(new Date());
        profileExtService.updateProfileExt(profileExtDO, profileExtDOExample);
    }

    /**
     * 执行登录
     *
     * @param mobileNum
     * @param toStorePassword
     * @param profileId
     */
    private void doLogin(String mobileNum, String toStorePassword, long profileId, HttpServletResponse response) {
        //注册成功后做登陆 --- 以mobile为name登陆
        UsernamePasswordToken token = new UsernamePasswordToken(mobileNum, toStorePassword);
        token.setRememberMe(true);
        //2. 获取当前Subject：
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);//millis
        session.setAttribute("profileId", profileId);
        session.setAttribute("mobile", mobileNum);
        //session.setAttribute("isAgency", "1");	//(0=非代理；1=代理)
        Cookie cookieU = new Cookie("JSESSIONID", session.getId().toString());
        cookieU.setMaxAge(60 * 60 * 24 * 15);
        cookieU.setPath("/");
        response.addCookie(cookieU);
    }

    /**
     * insert profile
     *
     * @param mobileNum
     * @param storedPassword
     */
    private ProfileDO addProfile(long buildingId, String mobileNum, String storedPassword, byte deplom) {
        ProfileDO profileDO = new ProfileDO();
        profileDO.setDateCreated(new Date());
        profileDO.setLastUpdated(new Date());
        profileDO.setMobile(mobileNum);
        profileDO.setStatus(ProfileStatusEnum.valid.getCode());
        profileDO.setInstalledApp((byte) 1);
        profileDO.setLastCommunity(buildingId);
        profileDO.setPassword(storedPassword);
        profileDO.setDiploma(deplom);
        String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
		if(null != mobileNum && mobileNum.length() > 4){
			profileDO.setNickname(mobileNum.substring(mobileNum.length()-4, mobileNum.length())+checkCode);
		}else{
			profileDO.setNickname(CheckNOGenerator.getFixLenthString(6));
		}
        int updateprofile = profileDOMapper.insertSelective(profileDO);
        if (updateprofile <= 0) {
            log.error("注册过程更改数据失败. update profile failed. mobile:" + mobileNum);
            com.welink.web.common.filter.Profiler.release();
            return null;
        }
        com.welink.web.common.filter.Profiler.release();
        return profileDO;
    }
    
    @RequestMapping(value = {"/api/m/1.0/checkIsRegister.json", "/api/h/1.0/checkIsRegister.json"}, produces = "application/json;charset=utf-8")
    public String checkIsRegister(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam String mobile) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
    	//根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
        	ProfileDO profileDO = thisNOprofiles.get(0);
        	if(null == profileDO.getPassword() || "".equals(profileDO.getPassword().trim())){
        		resultMap.put("isRegister", 2);		//已注册未设密码
        	}else{
        		resultMap.put("isRegister", 1);		//已注册
        	}
        }else{
        	resultMap.put("isRegister", 0);		//未注册
        	/*ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobile);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		resultMap.put("isRegister", 4);		//未注册,已有temp记录
        	}else{
        		resultMap.put("isRegister", 3);		//未注册
        	}*/
        }
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    public void setProfileExtService(ProfileExtService profileExtService) {
        this.profileExtService = profileExtService;
    }

    public void setProfileDOMapper(ProfileDOMapper profileDOMapper) {
        this.profileDOMapper = profileDOMapper;
    }
    
    public static void main(String[] args) {
		Long ss = 12L;
		System.out.println(ss.toString());
	}
    
}
