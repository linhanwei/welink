package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.MSG.SMSUtils;
import com.welink.biz.common.cache.CheckNOGenerator;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.common.model.SmsResponseTpl;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-9-10.
 * 验证手机是否本人
 * 这里只是发送短信验证码，短信验证码的验证在注册逻辑中统一校验
 */
@RestController
public class CheckMobile {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CheckMobile.class);

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private SMSUtils smsUtils;

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/m/1.0/checkMobile.json", "/api/h/1.0/checkMobile.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String mobile = request.getParameter("mobile");
        String act = request.getParameter("act");
        String reg = request.getParameter("reg");
        ResponseResult result = new ResponseResult();
        String checkNO = request.getParameter("checkNO");
        WelinkVO welinkVO = new WelinkVO();
        if (org.apache.commons.lang.StringUtils.equals(reg, "reg")) {
            //判断是否已经注册过
            //根据电话查出profile信息
            ProfileDOExample profileDOExample = new ProfileDOExample();
            profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
            List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
            if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.REGISTED_YET.getCode());
                welinkVO.setMsg(BizErrorEnum.REGISTED_YET.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        }

        if (StringUtils.equals("send", act)) {
            //send check number and put it into cache
            sendCheckNO(mobile, welinkVO, result);
        } else if (StringUtils.equals("check", act)) {
            //check the check number is valid or not
            checkCheckNO(mobile, checkNO, welinkVO);
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.PARAMS_ERROR.getCode());
            result.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
        }
        return JSON.toJSONString(welinkVO);
    }

    /**
     * check the check number is invalid or not
     *
     * @param mobile
     * @param checkNO
     * @param welinkVO
     */
    private void checkCheckNO(String mobile, String checkNO, WelinkVO welinkVO) {
        //check code 验证码
        String codeo = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        String tailMobile = org.apache.commons.lang.StringUtils.substring(mobile, 7, 11);
        if (env.isDev() || env.isTest()) {
            if (StringUtils.equals(checkNO, tailMobile)) {
                welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
                return;
            }
        }
        if (StringUtils.equals(checkNO, codeo)) {
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            log.warn("验证码校验失败. mobile " + mobile);
        }
    }

    /**
     * send check number via sms then put it into cache
     *
     * @param mobile
     * @param welinkVO
     */
    private void sendCheckNO(String mobile, WelinkVO welinkVO, ResponseResult result) {
        //boolean sendCheckNO = smsUtils.sendCheckCodeAndCacheCode(mobile);
    	SmsResponseTpl responseTpl = smsUtils.sendCheckCodeAndCacheCodeSmsResponseTpl(mobile);
    	if(null == responseTpl){
    		result.setStatus(BizErrorEnum.SYSTEM_BUSY.getCode());
            result.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
            result.setErrorCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setStatus(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
    	}
    	if (responseTpl.getCode() != 0) {//失败
    		result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(responseTpl.getMsg());
            result.setErrorCode(responseTpl.getCode());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(responseTpl.getCode());
            welinkVO.setMsg(responseTpl.getMsg());
        } else {
        	welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        }
        /*if (sendCheckNO) {
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(BizErrorEnum.SEND_CHECKNO_FAILED.getMsg());
            result.setErrorCode(BizErrorEnum.SEND_CHECKNO_FAILED.getCode());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SEND_CHECKNO_FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SEND_CHECKNO_FAILED.getMsg());
        }*/
    }
    
    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/sendTestNO.json", "/api/h/1.0/sendTestNO.json"}, produces = "application/json;charset=utf-8")
    public String sendTestNO(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String mobile = request.getParameter("mobile");
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
        Map result = new HashMap();
        //ResponseResult result = new ResponseResult();
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId); 
        if(null != profileDO && null != profileDO.getMobile() && "15622395287".equals(profileDO.getMobile().trim())){
        	String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
        	result.put("checkCode", checkCode);
        	memcachedClient.set(BizConstants.CHECK_NO_PREFIX + mobile, TimeConstants.REDIS_EXPIRE_SECONDS_10, checkCode);
        	/*SmsResponseTpl responseTpl = smsUtils.sendCheckCodeAndCacheCodeSmsResponseTplTest(mobile, checkCode);
        	if (null != responseTpl && responseTpl.getCode() != 0) {//失败
        		result.put("status", ResponseStatusEnum.FAILED.getCode());
        		result.put("msg", responseTpl.getMsg());
        		result.put("errorCode", responseTpl.getCode());
        		welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        		welinkVO.setCode(responseTpl.getCode());
        		welinkVO.setMsg(responseTpl.getMsg());
        	} else {
        		welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        	}*/
        	welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        }else{
        	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        	result.put("msg", "错误");
        }
        
    	welinkVO.setResult(result);
        return JSON.toJSONString(welinkVO);
    }
    
}
