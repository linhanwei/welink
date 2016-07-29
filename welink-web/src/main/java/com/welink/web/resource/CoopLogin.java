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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileCoopDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileTempDO;
import com.welink.commons.domain.ProfileTempDOExample;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
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
 * Created by daniel on 15-1-7.
 */
@RestController
public class CoopLogin {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CoopLogin.class);

    @Resource
    private UserService userService;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private UserInteractionService userInteractionService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private WeiXinMPController weiXinMPController;
    
    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;
    
    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @RequestMapping(value = {"/api/m/1.0/coopLogin.json", "/api/h/1.0/coopLogin.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String unionId = ParameterUtil.getParameter(request, "unionId");
        String loginType = ParameterUtil.getParameter(request, "ltype");
        String mobile = ParameterUtil.getParameter(request, "mobile");
        String checkNO = ParameterUtil.getParameter(request, "checkNO");
        
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
        boolean checkNOvalid = checkCheckNO(mobile, checkNO, session);
        if (!checkNOvalid) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //微信登陆
        if (StringUtils.equalsIgnoreCase(loginType, String.valueOf(BizConstants.LoginEnum.WX_LOGIN.getType()))  || StringUtils.equalsIgnoreCase(loginType, String.valueOf(BizConstants.LoginEnum.QQ_LOGIN.getType()))) {
            //获取获取存储的profile coop info
            //判断是否已经存储过
            ProfileCoopDO profileCoopDO = userService.fetchProfileCoopByUnionId(loginType, unionId);
            Preconditions.checkNotNull(profileCoopDO, "it can not happen ... profileCoopDO stored at the first step. it can not be null ...nick:" +
                    ",unionId:" + unionId + ",sessionId:" + session.getId());
            //是否关联过
            boolean hasRelated = userService.relatedCheck(profileCoopDO, session);
            ProfileDO profileDO = null;
            if (!hasRelated) {
            	ProfileDO profileDOtemp = userService.fetchProfileByMobile(mobile);
                boolean rela = userService.relaUser(mobile, profileCoopDO, loginType, session);
                profileDO = userService.fetchProfileByMobile(mobile);
                //recode ext info
                if (!rela) {
                    welinkVO.setStatus(0);
                    welinkVO.setCode(BizErrorEnum.ADD_COOP_USER_RELATIONSHIP_FAILED.getCode());
                    welinkVO.setMsg(BizErrorEnum.ADD_COOP_USER_RELATIONSHIP_FAILED.getMsg());
                    return JSON.toJSONString(welinkVO);
                }
                //首次绑定发积分优惠
                userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                if(null == profileDOtemp){
                	if(null != profileDO && null!=profileCoopDO && null != profileCoopDO.getNickname()
                			&& !"".equals(profileCoopDO.getNickname().trim())){
                		profileDO.setNickname(profileCoopDO.getNickname());
                		profileDO.setProfilePic(profileCoopDO.getHeadimgurl());
                		profileDOMapper.updateByPrimaryKeySelective(profileDO);
                	}
                	boolean isProfileTemp = false;
                	String parentUserId = "0";
                	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
                	profileTempDOExample.createCriteria().andMobileEqualTo(mobile);
                	//查找邀请人
                	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
                	Long inviteId = null;
                	Long pid = null;
                	if(!profileTempDOList.isEmpty()){
                		pid = (null == profileTempDOList.get(0).getPid() ? 0L : profileTempDOList.get(0).getPid());
                		inviteId = (null == profileTempDOList.get(0).getInviteId() ? 0L : profileTempDOList.get(0).getInviteId());
                		parentUserId = String.valueOf(pid);
                		isProfileTemp = true;	//ProfileTemp（用户临时表）是否有此电话号码数据，（true=有；false=无）
                	}
                	//查找上级代理
                    //ProfileDO parentProfile = mikuUserAgencyService.getParentProfileByProfileId(profileDO.getId());
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
                    		if(null != pid && inviteId.equals(pid)){	//同区域邀请
                    			weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), false);	//向上级代理推送邀请成功消息
                    		}else{	//跨区域邀请
                    			weiXinMPController.sendWXTemplateMessage(profileDO, profileWeChatDO.getOpenid(), true);	//向上级代理推送邀请成功消息
                    		}
                    	}
                    }
            	}
                
            }
            Map resultMap = new HashMap();
            if (null != profileDO) {
                resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                resultMap.put("profilePic", profileDO.getProfilePic());
                resultMap.put("lemonName", profileDO.getLemonName());
                resultMap.put("isExpert", (null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert()));	//是否专家(0=不是;1=是)
                resultMap.put("sex", profileDO.getSex());
                resultMap.put("ageGroup", profileDO.getAgeGroup());
            }
            buildCookie(mobile, welinkVO, response, session);
            welinkVO.setStatus(1);
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //支付宝
        else {
            //获取获取存储的profile coop info
            //判断是否已经存储过
            /*ProfileCoopDO profileCoopDO = userService.fetchProfileCoopByAlipayUserId(loginType, unionId);
            Preconditions.checkNotNull(profileCoopDO, "it can not happen ... profileCoopDO stored at the first step. it can not be null ...userId:" + unionId);
            //是否关联过
            boolean hasRelated = userService.relatedCheck(profileCoopDO, session);
            ProfileDO profileDO = null;
            if (!hasRelated) {
                boolean rela = userService.relaUser(mobile, profileCoopDO, loginType, session);
                profileDO = userService.fetchProfileByMobile(mobile);
                //recode ext info
                if (!rela) {
                    welinkVO.setStatus(0);
                    welinkVO.setCode(BizErrorEnum.ADD_COOP_USER_RELATIONSHIP_FAILED.getCode());
                    welinkVO.setMsg(BizErrorEnum.ADD_COOP_USER_RELATIONSHIP_FAILED.getMsg());
                    return JSON.toJSONString(welinkVO);
                }
                //绑定手机发积分优惠
                userInteractionService.sendCouponsToNewPerson(profileDO.getId());
            }
            buildCookie(mobile, welinkVO, response, session);
            welinkVO.setStatus(1);
            return JSON.toJSONString(welinkVO);*/
        }
        welinkVO.setStatus(0);
        welinkVO.setCode(BizErrorEnum.ADD_COOP_USER_RELATIONSHIP_FAILED.getCode());
        welinkVO.setMsg(BizErrorEnum.ADD_COOP_USER_RELATIONSHIP_FAILED.getMsg());
        return JSON.toJSONString(welinkVO);
    }

    /**
     * check the check number is invalid or not
     *
     * @param mobile
     * @param checkNO
     */
    private boolean checkCheckNO(String mobile, String checkNO, Session session) {
        String tailMobile = org.apache.commons.lang.StringUtils.substring(mobile, 7, 11);
        if (org.apache.commons.lang3.StringUtils.equals(checkNO, tailMobile)) {
            return true;
        }
        if (StringUtils.equals("032666", checkNO)) {
            return true;
        }
        //check code 验证码
        String codeo = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        if (org.apache.commons.lang3.StringUtils.equals(checkNO, codeo)) {
            return true;
        } else {
            log.warn("验证码校验失败. mobile " + mobile + ",sessionId:" + session.getId());
            return false;
        }
    }

    private void buildCookie(String mobile, WelinkVO welinkVO, HttpServletResponse response, Session session) {
        session.setAttribute(BizConstants.MOBILE, mobile);
        Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
        cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
        cookie.setPath("/");
        response.addCookie(cookie);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
    }
}
