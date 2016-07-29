package com.welink.web.resource.customized;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easemob.server.example.api.IMUserAPI;
import com.easemob.server.example.comm.ClientContext;
import com.easemob.server.example.comm.EasemobRestAPIFactory;
import com.easemob.server.example.comm.body.IMUserBody;
import com.easemob.server.example.comm.wrapper.BodyWrapper;
import com.easemob.server.example.comm.wrapper.ResponseWrapper;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.Md5;
import com.welink.biz.service.UserService;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.domain.MikuCsadEvaluateDO;
import com.welink.commons.domain.MikuCsadServiceLogDO;
import com.welink.commons.domain.MikuCsadServiceLogDOExample;
import com.welink.commons.domain.MikuMineRecentlycontactLogDO;
import com.welink.commons.domain.MikuMineRecentlycontactLogDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.MikuCsadEvaluateDOMapper;
import com.welink.commons.persistence.MikuCsadServiceLogDOMapper;
import com.welink.commons.persistence.MikuMineRecentlycontactLogDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.vo.ImUserVO;
import com.welink.commons.vo.MikuCsadEvaluateVO;
import com.welink.commons.vo.MikuMineRecentlycontactLogVO;
import com.welink.web.common.constants.ResponseStatusEnum;

@RestController
public class EmchatAction {
	
	@Resource
	private UserService userService;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
	private MikuCsadEvaluateDOMapper mikuCsadEvaluateDOMapper;
	
	@Resource
	private MikuCsadServiceLogDOMapper mikuCsadServiceLogDOMapper;
	
	@Resource
	private MikuMineRecentlycontactLogDOMapper mikuMineRecentlycontactLogDOMapper;

	/**
	 * 注册单个环信用户1111
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    @RequestMapping(value = {"/api/m/1.0/createNewIMUserSingle.json", "/api/h/1.0/createNewIMUserSingle.json"}, produces = "application/json;charset=utf-8")
    public String createNewIMUserSingle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			/*welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);*/
        	resultMap.put("status", -1);		//status(-1=未登录;0=注册失败；1=注册成功; 2=已注册)
			welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
	        welinkVO.setResult(resultMap);
	        return JSON.toJSONString(welinkVO);
		}
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		if (null != profileDO && null != profileDO.getId()) {
			try {
				if(!StringUtils.isBlank(profileDO.getEmUserName())){
					ImUserVO imUserVO = new ImUserVO();
					imUserVO.setUserName(profileDO.getEmUserName());
					imUserVO.setPassword(profileDO.getEmUserPw());
					resultMap.put("imUser", imUserVO);	//用户环信信息
					resultMap.put("status", 2);		//status(-1=未登录;0=注册失败；1=注册成功; 2=已注册)
					welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
			        welinkVO.setResult(resultMap);
			        return JSON.toJSONString(welinkVO);
				}
				
				String emchatPw = Md5.MD5Encode(BizConstants.EMCHAT_PW+profileDO.getId());	//密码
				EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();
				IMUserAPI user = (IMUserAPI)factory.newInstance(EasemobRestAPIFactory.USER_CLASS);
				BodyWrapper userBody = new IMUserBody(BizConstants.EMCHAT_CUSTOMER_PRE+profileDO.getId(),
						emchatPw, profileDO.getMobile());
				ResponseWrapper responseWrapper = (ResponseWrapper) user.createNewIMUserSingle(userBody);
				if(null != responseWrapper && null != responseWrapper.getResponseStatus() && responseWrapper.getResponseStatus().equals(200)){
					//注册成功
					IMUserBody userBody2 = (IMUserBody)userBody;
					ImUserVO imUserVO = new ImUserVO();
					imUserVO.setUserName(userBody2.getUserName());
					imUserVO.setNickName(userBody2.getNickName());
					imUserVO.setPassword(userBody2.getPassword());
					resultMap.put("imUser", imUserVO);	//用户环信信息
					resultMap.put("status", 1);		//status(-1=未登录;0=注册失败；1=注册成功; 2=已注册)
					profileDO.setEmUserName(BizConstants.EMCHAT_CUSTOMER_PRE+profileDO.getId());
					profileDO.setEmUserPw(emchatPw);
					profileDOMapper.updateByPrimaryKeySelective(profileDO);
				}else{
					/*welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
		            welinkVO.setMsg("啊哦~环信注册失败~");
		            return JSON.toJSONString(welinkVO);*/
					resultMap.put("status", 0);		//status(-1=未登录;0=注册失败；1=注册成功; 2=已注册)
					welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
			        welinkVO.setResult(resultMap);
			        return JSON.toJSONString(welinkVO);
				}
				//数据库保存用户信息
			} catch (Exception e) {
				/*welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
	            welinkVO.setMsg("啊哦~环信注册失败~");
	            return JSON.toJSONString(welinkVO);*/
				resultMap.put("status", 0);		//status(-1=未登录;0=注册失败；1=注册成功; 2=已注册)
				welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
		        welinkVO.setResult(resultMap);
		        return JSON.toJSONString(welinkVO);
			}
		}else{
			/*welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);*/
			resultMap.put("status", -1);		//status(-1=未登录;0=注册失败；1=注册成功; 2=已注册)
			welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
	        welinkVO.setResult(resultMap);
	        return JSON.toJSONString(welinkVO);
		}
        
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 是否注册环信
     * @param request
     * @param response
     * @param type 聊天用户(0=客户；1=专家)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/hasRegistEm.json", "/api/h/1.0/hasRegistEm.json"}, produces = "application/json;charset=utf-8")
    public String hasRegistEm(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        ProfileDO profileDO = userService.fetchProfileById(profileId);
		if (null != profileDO && null != profileDO.getId()) {
			if(StringUtils.isBlank(profileDO.getEmUserName())){
				resultMap.put("status", 1);		//status(0=未注册；1=已注册)
				welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
				welinkVO.setResult(resultMap);
				return JSON.toJSONString(welinkVO);
			}else{
				resultMap.put("status", 0);		//status(0=未注册；1=已注册)
				welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
				welinkVO.setResult(resultMap);
				return JSON.toJSONString(welinkVO);
			}
			/*if(null != type && type == 0){
				if(StringUtils.isBlank(profileDO.getEmUserName())){
					resultMap.put("status", 1);		//status(0=未注册；1=已注册)
					welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
					welinkVO.setResult(resultMap);
					return JSON.toJSONString(welinkVO);
				}else{
					resultMap.put("status", 0);		//status(0=未注册；1=已注册)
					welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
					welinkVO.setResult(resultMap);
					return JSON.toJSONString(welinkVO);
				}
			}else{
				MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
	        	mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileId);
	        	List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
	        	if(!mikuCsadDOList.isEmpty()){
	        		MikuCsadDO mikuCsadDO = mikuCsadDOList.get(0);
	        		if(null != mikuCsadDO && !StringUtils.isBlank(mikuCsadDO.getEmUserName())){
	        			resultMap.put("status", 1);		//status(0=未注册；1=已注册)
						welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
						welinkVO.setResult(resultMap);
						return JSON.toJSONString(welinkVO);
					}else{
	        			resultMap.put("status", 0);		//status(0=未注册；1=已注册)
						welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
						welinkVO.setResult(resultMap);
						return JSON.toJSONString(welinkVO);
	        		}
	        	}else{
	        		resultMap.put("status", 0);		//status(0=未注册；1=已注册)
					welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
					welinkVO.setResult(resultMap);
					return JSON.toJSONString(welinkVO);
	        	}
			}*/
		}else{
			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
		}
    }
    
    /**
     * 判断环信用户是否在线
     * @param request
     * @param response
     * @param emUserName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/isEmOnline.json", "/api/h/1.0/isEmOnline.json"}, produces = "application/json;charset=utf-8")
    public String isEmOnline(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="emUserName", required = true) String emUserName) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map<String, Object> resultMap = new HashMap<String, Object> ();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        
        EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();
		IMUserAPI user = (IMUserAPI)factory.newInstance(EasemobRestAPIFactory.USER_CLASS);
		ResponseWrapper imUserStatus = (ResponseWrapper)user.getIMUserStatus(emUserName);
		if(null != imUserStatus && imUserStatus.getResponseStatus() == 200){
			JSONObject parseObject = JSON.parseObject(String.valueOf(imUserStatus.getResponseBody()));
			Object objData = parseObject.get("data");
			if(null != objData){
				String data = String.valueOf(objData);
				if(data.indexOf("online") > -1){
					resultMap.put("status", 1);	//在线
				}else if(data.indexOf("offline") > -1){
					resultMap.put("status", 0);	//离线
				}else{
					resultMap.put("status", 0);	//离线
				}
			}else{
				resultMap.put("status", 0);	//离线
			}
		}else{
			resultMap.put("status", 0);	//离线
		}
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 是否专家
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/checkIsExper.json", "/api/h/1.0/checkIsExper.json"}, produces = "application/json;charset=utf-8")
    public String checkIsExper(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        MikuCsadDO mikuCsadDO = null;
        MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
    	mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileId);
    	int countCsad = mikuCsadDOMapper.countByExample(mikuCsadDOExample);
    	if(countCsad > 0){
    		resultMap.put("isExpert", (byte)1);	//是否专家(0=不是; 1=是)
    	}else{
    		resultMap.put("isExpert", (byte)0);	//是否专家(0=不是; 1=是)
    	}
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    /**
     *我的专家信息
     * @param request
     * @param response
     * @param type 聊天用户(0=客户；1=专家)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/myCsad.json", "/api/h/1.0/myCsad.json"}, produces = "application/json;charset=utf-8")
    public String myCsad(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        MikuCsadDO mikuCsadDO = null;
        MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
    	mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileId);
    	List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
    	if(!mikuCsadDOList.isEmpty()){
    		mikuCsadDO = mikuCsadDOList.get(0);
    		if(null != mikuCsadDO){
    			ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
    			resultMap.put("emUserName", profileDO.getEmUserName());
    			resultMap.put("emUserPw", profileDO.getEmUserPw());
    			/*if(null != profileDO){
    				mikuCsadDO.setEmUserName(profileDO.getEmUserName());
    				mikuCsadDO.setEmUserPw(profileDO.getEmUserPw());
    			}else{
    				mikuCsadDO.setEmUserName(null);
    				mikuCsadDO.setEmUserPw(null);
    			}*/
    		}
    	}
    	/*if(null != mikuCsadDO){
			MikuMineRecentlycontactLogDOExample mikuMineRecentlycontactLogDOExample = new MikuMineRecentlycontactLogDOExample();
			mikuMineRecentlycontactLogDOExample.createCriteria()
				.andCsadUserIdEqualTo(mikuCsadDO.getUserId());
			int contactTimes = mikuMineRecentlycontactLogDOMapper.countByExample(mikuMineRecentlycontactLogDOExample);
			resultMap.put("contactTimes", contactTimes);
		}else{
			resultMap.put("contactTimes", 0);
		}*/
    	resultMap.put("vo", mikuCsadDO);
    	welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
    
    /**
     *专家信息
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/getCsadInfo.json", "/api/h/1.0/getCsadInfo.json"}, produces = "application/json;charset=utf-8")
    public String getCsadInfo(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="emUserName", required = true) String emUserName) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        MikuCsadDO mikuCsadDO = null;
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andEmUserNameEqualTo(emUserName);
        List<ProfileDO> profileDOlist = profileDOMapper.selectByExample(profileDOExample);
        if(!profileDOlist.isEmpty()){
        	ProfileDO profileDO = profileDOlist.get(0);
        	if(null != profileDO){
        		MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
        		mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileDO.getId());
        		List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
        		if(!mikuCsadDOList.isEmpty()){
        			mikuCsadDO = mikuCsadDOList.get(0);
        			if(null != mikuCsadDO){
        				resultMap.put("emUserName", profileDO.getEmUserName());
        			}
        		}
    		}
        }
        /*if(null != mikuCsadDO){
			MikuMineRecentlycontactLogDOExample mikuMineRecentlycontactLogDOExample = new MikuMineRecentlycontactLogDOExample();
			mikuMineRecentlycontactLogDOExample.createCriteria()
				.andCsadUserIdEqualTo(mikuCsadDO.getUserId());
			int contactTimes = mikuMineRecentlycontactLogDOMapper.countByExample(mikuMineRecentlycontactLogDOExample);
			resultMap.put("contactTimes", contactTimes);
		}else{
			resultMap.put("contactTimes", 0);
		}*/
        resultMap.put("vo", mikuCsadDO);
    	welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
        
    /**
     * 开始聊天
     * @param request
     * @param response
     * @param type 聊天用户(0=客户；1=专家)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/beginTalk.json", "/api/h/1.0/beginTalk.json"}, produces = "application/json;charset=utf-8")
    public String beginTalk(HttpServletRequest request, HttpServletResponse response, 
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type,
    		@RequestParam(value="toUserId", required = true) Long toUserId) throws Exception {
        long profileId = -1, userId = -1L, csadId = -1, csadUser = -1;
    	//long profileId = -1, csadId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();	//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        
        MikuCsadDO mikuCsadDO = null;
        
        if(null != type && type == 0){	//客户发起聊天
        	userId = profileId;
        	csadUser = toUserId;
        }else{	//专家发起聊天
        	userId = toUserId;
        	csadUser = profileId;
        }
        MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
        mikuCsadDOExample.createCriteria().andUserIdEqualTo(csadUser);
        List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
        if(!mikuCsadDOList.isEmpty()){
        	mikuCsadDO = mikuCsadDOList.get(0);
        }
        
        if(null == mikuCsadDO){
        	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        	welinkVO.setMsg("阿哦~没有此聊天对象");
        	return JSON.toJSONString(welinkVO);
        }
        
        //ProfileDO profileDO = userService.fetchProfileById(userId);
        ProfileDO profileDO = userService.fetchProfileById(userId);
        ProfileDO csadProfileDO = userService.fetchProfileById(csadUser);
		if (null != profileDO && null != csadProfileDO) {
			Date nowDate = new Date();
			//更新对话列表
			/*MikuCsadServiceLogDOExample mikuCsadServiceLogDOExample = new MikuCsadServiceLogDOExample();
			mikuCsadServiceLogDOExample.createCriteria().andCsadIdEqualTo(csadId).andUserIdEqualTo(profileDO.getId());
			if(mikuCsadServiceLogDOMapper.countByExample(mikuCsadServiceLogDOExample) > 0){
				MikuCsadServiceLogDO mikuCsadServiceLogDO = new MikuCsadServiceLogDO();
				mikuCsadServiceLogDO.setLastUpdated(nowDate);
				mikuCsadServiceLogDOMapper.updateByExampleSelective(mikuCsadServiceLogDO, mikuCsadServiceLogDOExample);
			}else{
				MikuCsadServiceLogDO mikuCsadServiceLogDO = new MikuCsadServiceLogDO();
				mikuCsadServiceLogDO.setUserId(profileDO.getId());
				mikuCsadServiceLogDO.setCsadId(csadId);
				mikuCsadServiceLogDO.setCsadName(mikuCsadDO.getCsadName());
				mikuCsadServiceLogDO.setCsadUserId(mikuCsadDO.getUserId());
				mikuCsadServiceLogDO.setDateCreated(nowDate);
				mikuCsadServiceLogDO.setLastUpdated(nowDate);
				mikuCsadServiceLogDOMapper.insertSelective(mikuCsadServiceLogDO);
			}*/
			//更新对话列表
			/*MikuMineRecentlycontactLogDOExample mikuMineRecentlycontactLogDOExample = new MikuMineRecentlycontactLogDOExample();
			mikuMineRecentlycontactLogDOExample.createCriteria().andCsadIdEqualTo(csadId)
				.andUserIdEqualTo(profileDO.getId());
			if(mikuMineRecentlycontactLogDOMapper.countByExample(mikuMineRecentlycontactLogDOExample) > 0){
				MikuMineRecentlycontactLogDO mikuMineRecentlycontactLogDO = new MikuMineRecentlycontactLogDO();
				mikuMineRecentlycontactLogDO.setLastUpdated(nowDate);
				mikuMineRecentlycontactLogDOMapper.updateByExampleSelective(mikuMineRecentlycontactLogDO, mikuMineRecentlycontactLogDOExample);
			}else{
				MikuMineRecentlycontactLogDO mikuMineRecentlycontactLogDO = new MikuMineRecentlycontactLogDO();
				mikuMineRecentlycontactLogDO.setUserId(userId);
				mikuMineRecentlycontactLogDO.setCsadId(csadId);
				mikuMineRecentlycontactLogDO.setCsadName(mikuCsadDO.getCsadName());
				mikuMineRecentlycontactLogDO.setCsadUserId(csadUser);
				mikuMineRecentlycontactLogDO.setDateCreated(nowDate);
				mikuMineRecentlycontactLogDO.setLastUpdated(nowDate);
				mikuMineRecentlycontactLogDOMapper.insertSelective(mikuMineRecentlycontactLogDO);
			}*/
		}else{
			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
		}
		
		ImUserVO imUser = new ImUserVO(profileDO.getEmUserName(),
				profileDO.getEmUserPw(), null);
		ImUserVO csadImUser = new ImUserVO(csadProfileDO.getEmUserName(),
				csadProfileDO.getEmUserPw(), null);
		
		if(null != type && type == 0){	//客户发起聊天
			resultMap.put("myImUser", imUser);
			resultMap.put("toImUser", csadImUser);
        }else{	//专家发起聊天
        	resultMap.put("myImUser", csadImUser);
			resultMap.put("toImUser", imUser);
        }
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 结束聊天
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/stopTalk.json", "/api/h/1.0/stopTalk.json"}, produces = "application/json;charset=utf-8")
    public String stopTalk(HttpServletRequest request, HttpServletResponse response, 
    		//@RequestParam(value="csadId", required = true) Long csadId,
    		@RequestParam(value="emUserName", required = true) String emUserName,
    		@RequestParam(value="evaluateLevel", required = false, defaultValue="10") Integer evaluateLevel,
    		@RequestParam(value="evaluateNote", required = false) String evaluateNote
    		) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        if(StringUtils.isBlank(emUserName)){
        	welinkVO.setStatus(0);
			welinkVO.setMsg("请~用户名不能为空!");
			return JSON.toJSONString(welinkVO);
        }
        MikuCsadDO mikuCsadDO = null;
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andEmUserNameEqualTo(emUserName);
        List<ProfileDO> profileDOlist = profileDOMapper.selectByExample(profileDOExample);
        if(!profileDOlist.isEmpty()){
        	ProfileDO profileDO = profileDOlist.get(0);
        	if(null != profileDO){
        		MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
        		mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileDO.getId());
        		List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
        		if(!mikuCsadDOList.isEmpty()){
        			mikuCsadDO = mikuCsadDOList.get(0);
        		}
    		}
        }
        //MikuCsadDO mikuCsadDO = mikuCsadDOMapper.selectByPrimaryKey(csadId);
        if(null == mikuCsadDO){
        	welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        	welinkVO.setMsg("阿哦~没有此聊天对象");
        	return JSON.toJSONString(welinkVO);
        }
        
        ProfileDO profileDO = userService.fetchProfileById(profileId);
		if (null != profileDO) {
			Date nowDate = new Date();
			if(evaluateLevel > 10 || evaluateLevel < 0){
				evaluateLevel = 10;
			}
			//插入评论
			MikuCsadEvaluateDO mikuCsadEvaluateDO = new MikuCsadEvaluateDO();
			mikuCsadEvaluateDO.setUserId(profileId);
			mikuCsadEvaluateDO.setCsadId(mikuCsadDO.getId());
			mikuCsadEvaluateDO.setCsadUserId(mikuCsadDO.getUserId());
			mikuCsadEvaluateDO.setCsadName(mikuCsadDO.getCsadName());
			mikuCsadEvaluateDO.setEvaluateLevel(evaluateLevel);
			mikuCsadEvaluateDO.setEvaluateNote(evaluateNote);
			mikuCsadEvaluateDO.setDateCreated(nowDate);
			mikuCsadEvaluateDO.setLastUpdated(nowDate);
			mikuCsadEvaluateDOMapper.insertSelective(mikuCsadEvaluateDO);
			
			//更新专家等级
			Integer commentCount = (null == mikuCsadDO.getCommentCount() ? 0 : mikuCsadDO.getCommentCount());
			Integer csadLevel = (null == mikuCsadDO.getCsadLevel() ? 0 : mikuCsadDO.getCsadLevel());
			csadLevel = ((commentCount * csadLevel) +  evaluateLevel) / (commentCount + 1);
			mikuCsadDO.setCommentCount(commentCount+1);
			if(evaluateLevel > 10 || evaluateLevel < 0){
				csadLevel = 10;
			}
			mikuCsadDO.setCsadLevel(csadLevel);
			mikuCsadDO.setAdviceCount((null == mikuCsadDO.getAdviceCount() ? 0 : mikuCsadDO.getAdviceCount())+1);
			mikuCsadDOMapper.updateByPrimaryKeySelective(mikuCsadDO);
			
			//更新对话列表
			/*MikuMineRecentlycontactLogDOExample mikuMineRecentlycontactLogDOExample = new MikuMineRecentlycontactLogDOExample();
			mikuMineRecentlycontactLogDOExample.createCriteria().andCsadIdEqualTo(csadId)
				.andUserIdEqualTo(profileDO.getId());
			if(mikuMineRecentlycontactLogDOMapper.countByExample(mikuMineRecentlycontactLogDOExample) > 0){
				MikuMineRecentlycontactLogDO mikuMineRecentlycontactLogDO = new MikuMineRecentlycontactLogDO();
				mikuMineRecentlycontactLogDO.setLastUpdated(nowDate);
				mikuMineRecentlycontactLogDOMapper.updateByExampleSelective(mikuMineRecentlycontactLogDO, mikuMineRecentlycontactLogDOExample);
			}else{
				MikuMineRecentlycontactLogDO mikuMineRecentlycontactLogDO = new MikuMineRecentlycontactLogDO();
				mikuMineRecentlycontactLogDO.setUserId(profileDO.getId());
				mikuMineRecentlycontactLogDO.setCsadId(csadId);
				mikuMineRecentlycontactLogDO.setCsadName(mikuCsadDO.getCsadName());
				mikuMineRecentlycontactLogDO.setCsadUserId(mikuCsadDO.getUserId());
				mikuMineRecentlycontactLogDO.setDateCreated(nowDate);
				mikuMineRecentlycontactLogDO.setLastUpdated(nowDate);
				mikuMineRecentlycontactLogDOMapper.insertSelective(mikuMineRecentlycontactLogDO);
			}*/
		}else{
			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
		}
		resultMap.put("status", 1);
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 聊天列表
     * @param request
     * @param response
     * @param type
     * @param pg
     * @param sz
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/getTalkList.json", "/api/h/1.0/getTalkList.json"}, produces = "application/json;charset=utf-8")
    public String getTalkList(HttpServletRequest request, HttpServletResponse response, 
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type,
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
        
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        //BeanUtils.copyProperties(mineRecentlycontactLogDO, mikuCrowdfundDetailVO);
        List<MikuMineRecentlycontactLogVO> mikuMineRecentlycontactLogVOList = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if(null != type && type == 0){
        	paramMap.put("userId", profileId);
        }else{
        	paramMap.put("csadUserId", profileId);
        }
        paramMap.put("orderByClause", "rl.last_updated DESC");
        paramMap.put("limit", size);
        paramMap.put("offset", startRow);
        mikuMineRecentlycontactLogVOList = mikuCsadDOMapper.getMineRecentlycontactLogVOList(paramMap);
        boolean hasNext = true;
        if (null != mikuMineRecentlycontactLogVOList && mikuMineRecentlycontactLogVOList.size() < size) {
            hasNext = false;
        }else if(null == mikuMineRecentlycontactLogVOList){
            hasNext = false;
        }else{
        	hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuMineRecentlycontactLogVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 获取评论列表
     * @param request
     * @param response
     * @param type
     * @param pg
     * @param sz
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/api/m/1.0/getCsadEvaluateVOList.json", "/api/h/1.0/getCsadEvaluateVOList.json"}, produces = "application/json;charset=utf-8")
    public String getCsadEvaluateVOList(HttpServletRequest request, HttpServletResponse response, 
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type,
    		@RequestParam(value="csadId", required = false) Long csadId,
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
        
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        //BeanUtils.copyProperties(mineRecentlycontactLogDO, mikuCrowdfundDetailVO);
        List<MikuCsadEvaluateVO> mikuMineRecentlycontactLogVOList = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if(null != type && type == 0){
        	paramMap.put("userId", profileId);
        	paramMap.put("csadId", csadId);
        }else{
        	paramMap.put("csadUserId", profileId);
        }
        paramMap.put("orderByClause", "rl.last_updated DESC");
        paramMap.put("limit", size);
        paramMap.put("offset", startRow);
        mikuMineRecentlycontactLogVOList = mikuCsadDOMapper.getCsadEvaluateVOList(paramMap);
        boolean hasNext = true;
        if (null != mikuMineRecentlycontactLogVOList && mikuMineRecentlycontactLogVOList.size() < size) {
            hasNext = false;
        }else if(null == mikuMineRecentlycontactLogVOList){
            hasNext = false;
        }else{
        	hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuMineRecentlycontactLogVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
    
    public static void main(String[] args) {
    	//String emchatPw = Md5.MD5Encode(BizConstants.EMCHAT_PW+70468);
    	int evaluateLevel = 10;
    	Integer commentCount = 2;
		Integer csadLevel = 5;
		csadLevel = ((commentCount * csadLevel) +  evaluateLevel) / (commentCount + 1);
		
    	System.out.println("------------------------------------");
    	System.out.println("........: "+csadLevel);
	}
}
