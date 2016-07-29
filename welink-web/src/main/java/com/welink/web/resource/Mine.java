package com.welink.web.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.CouponService;
import com.welink.biz.service.PointService;
import com.welink.biz.service.UserService;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.domain.MikuUserAgencyDO;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.utils.UpYunUtil;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 14-10-8.
 */
@RestController
public class Mine {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(Mine.class);

	@Resource
	private UserService userService;

	@Resource
	private TradeMapper tradeMapper;

	@Resource
	private PointService pointService;

	@Resource
	private CouponService couponService;

	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
	private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;

	@NeedProfile
	@RequestMapping(value = { "/api/m/1.0/mine.json", "/api/h/1.0/mine.json" }, produces = "application/json;charset=utf-8")
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// para
		long profileId = -1l;
		long point = 0;
		long couponCount = 0;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		log.info("user mine ... sesionid:" + session.getId()
				+ ".......profileId:" + profileId);
		//Map resultMap = new HashMap();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("isAgentcy", "0");
		ProfileDO profileDO = userService.fetchProfileById(profileId);
		if (null != profileDO) {
			MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
			mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
			List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);
			if(!mikuUserAgencyDOList.isEmpty()){
				Long levelId = mikuUserAgencyDOList.get(0).getLevelId();
				if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO1.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO1.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO1.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO2.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO2.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO2.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO3.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO3.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO3.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO4.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO4.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO4.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO5.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO5.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO5.getName());
				}else{
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.USER.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.USER.getName());
				}
			}else{
				resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.USER.getId());
				resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.USER.getName());
			}
			resultMap.put("nickName", profileDO.getNickname());
			//resultMap.put("city", BizConstants.USER_CITY);// TODO:操，又是写死，客户端就不能做个定位吗
			resultMap.put("mobile", profileDO.getMobile());
			resultMap.put("headPic", profileDO.getProfilePic());
			resultMap.put("isAgentcy", null == profileDO.getIsAgency() ? "0"
					: profileDO.getIsAgency());
			resultMap.put("isExpert", null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert());
			resultMap.put("province", profileDO.getProvince());
			resultMap.put("city", profileDO.getCity());
			resultMap.put("sex", profileDO.getSex());
			resultMap.put("ageGroup", profileDO.getAgeGroup());
		}
		/** 我的订单 **/
		/*TradeExample tExample = new TradeExample();
		tExample.createCriteria()
				.andBuyerIdEqualTo(profileId)
				.andStatusEqualTo(
						Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
		int forPay = tradeMapper.countByExample(tExample);
		resultMap.put("forPay", forPay);

		TradeExample tExample1 = new TradeExample();
		tExample1
				.createCriteria()
				.andBuyerIdEqualTo(profileId)
				.andStatusEqualTo(
						Constants.TradeStatus.WAIT_SELLER_SEND_GOODS
								.getTradeStatusId());
		int forSend = tradeMapper.countByExample(tExample1);
		resultMap.put("forSend", forSend);

		TradeExample tExample2 = new TradeExample();
		tExample2
				.createCriteria()
				.andBuyerIdEqualTo(profileId)
				.andStatusEqualTo(
						Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS
								.getTradeStatusId());
		int forReceive = tradeMapper.countByExample(tExample2);
		resultMap.put("forReceive", forReceive);*/

		/** 积分&优惠券 */
		point = pointService.findAvailablePointByUserId(profileId);
		couponCount = couponService.findUserCouponCountByUserId(profileId);
		resultMap.put("point", point);
		resultMap.put("couponCount", couponCount);
		resultMap.put("uid", profileId);
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}

	@RequestMapping(value = { "/api/m/1.0/mineInfo.json",
			"/api/h/1.0/mineInfo.json" }, produces = "application/json;charset=utf-8")
	public String mineInfo(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// para
		long profileId = -1l;
		long point = 0;
		long couponCount = 0;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		log.info("user mineInfo ... sesionid:" + session.getId()
				+ ".......profileId:" + profileId);
		//Map resultMap = new HashMap();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		ProfileDO profileDO = userService.fetchProfileById(profileId);
		
		
		if (null != profileDO) {
			MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
			mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
			List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);
			if(!mikuUserAgencyDOList.isEmpty()){
				Long levelId = mikuUserAgencyDOList.get(0).getLevelId();
				if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO1.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO1.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO1.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO2.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO2.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO2.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO3.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO3.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO3.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO4.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO4.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO4.getName());
				}else if(null != levelId && levelId.equals(BizConstants.MikuAgencyLevel.CEO5.getId())){
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.CEO5.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.CEO5.getName());
				}else{
					resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.USER.getId());
					resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.USER.getName());
				}
			}else{
				resultMap.put("agencyLevelId", BizConstants.MikuAgencyLevel.USER.getId());
				resultMap.put("agencyLevelName", BizConstants.MikuAgencyLevel.USER.getName());
			}
			resultMap.put("profileId", profileId);
			resultMap.put("isAgency", profileDO.getIsAgency());
			resultMap.put("nickName", profileDO.getNickname());
			resultMap.put("emUserName", profileDO.getEmUserName());
			resultMap.put("isExpert", null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert());
			//resultMap.put("emUserPw", profileDO.getEmUserPw());
			//resultMap.put("city", BizConstants.USER_CITY);// TODO:操，又是写死，客户端就不能做个定位吗
			resultMap.put("mobile", profileDO.getMobile());
			resultMap.put("headPic", profileDO.getProfilePic());
			resultMap.put("province", profileDO.getProvince());
			resultMap.put("city", profileDO.getCity());
			resultMap.put("sex", profileDO.getSex());
			resultMap.put("ageGroup", profileDO.getAgeGroup());
		} else {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
			welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}

	private boolean checkSession(Session session) {
		if (null == session) {
			return true;
		}
		if (null != session.getAttribute("profileId")) {
			return false;
		} else {
			return true;
		}
	}

	@NeedProfile
	@RequestMapping(value = { "/api/m/1.0/joinAgency.json",
			"/api/h/1.0/joinAgency.json" }, produces = "application/json;charset=utf-8")
	public String joinAgency(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
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

		Map paramsMap = new HashMap();
		paramsMap.put("profileId", profileId);
		paramsMap.put("status",
				Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId()); // 一完成的订单
		Map<String, Object> sumFeeMap = tradeMapper.sumByBuyer(paramsMap);
		if (null != sumFeeMap) {
			Long sumPrice = (null == sumFeeMap.get("price") ? 0L : Long
					.valueOf(sumFeeMap.get("price").toString()));
			if (sumPrice >= BizConstants.CAN_JOIN_AGENCY_FEE) { // 如果消费满99元可以成为代理
				ProfileDO profileDO = new ProfileDO();
				profileDO.setId(profileId);
				profileDO.setIsAgency((byte) 1);
				//profileDOMapper.updateByPrimaryKeySelective(profileDO); // 成为代理
				welinkVO.setStatus(1);
				return JSON.toJSONString(welinkVO);
			} else {
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦，您消费未满" + BizConstants.CAN_JOIN_AGENCY_FEE
						+ "元不能申请成为代理");
				return JSON.toJSONString(welinkVO);
			}
		} else {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
			welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
			return JSON.toJSONString(welinkVO);
		}
	}
	
	/**
	 * 
	 * myParentUser:(查询是否有上级). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = { "/api/m/1.0/myParentUser.json",
			"/api/h/1.0/myParentUser.json" }, produces = "application/json;charset=utf-8")
	public String myParentUser(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		Long pUserId = -1L;
		String pUserIdStr = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserIdStr || !StringUtils.isNumeric(pUserIdStr) || StringUtils.isBlank(pUserIdStr)){
        	pUserIdStr = "0";
        }
        pUserId = Long.valueOf(pUserIdStr);
        if(pUserId < 1L){
        	resultMap.put("hasUp", 5);	//没传上级
			welinkVO.setStatus(1);
			welinkVO.setResult(resultMap);
			return JSON.toJSONString(welinkVO);
        }
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		
		if(null != session.getAttribute("profileId")){
			profileId = (long) session.getAttribute("profileId");
		}
		if (profileId < 0) {
			resultMap.put("hasUp", -1);	//未登录
			welinkVO.setStatus(1);
			welinkVO.setResult(resultMap);
			return JSON.toJSONString(welinkVO);
		}
		
		ProfileDO profile = null;
		profile = profileDOMapper.selectByPrimaryKey(profileId);
		if(null != profile){
			resultMap.put("isAgency", null == profile.getIsAgency() ? (byte)0 : profile.getIsAgency());	//是否代理
		}else{
			resultMap.put("hasUp", -1);	//未登录
			welinkVO.setStatus(1);
			welinkVO.setResult(resultMap);
			return JSON.toJSONString(welinkVO);
		}
		
		MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
		mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
		List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);
		ProfileDO upProfile = null;
		upProfile = profileDOMapper.selectByPrimaryKey(pUserId);
		if(null == upProfile || (null != upProfile && null == upProfile.getIsAgency()) ||
				(null != upProfile && upProfile.getIsAgency().equals((byte)1))){
			resultMap.put("hasUp", 4);	//上级不是代理
			welinkVO.setStatus(1);
			welinkVO.setResult(resultMap);
			return JSON.toJSONString(welinkVO);
		}
		if(null != mikuUserAgencyDOList && !mikuUserAgencyDOList.isEmpty()){
			MikuUserAgencyDO mikuUserAgencyDO = mikuUserAgencyDOList.get(0);
			if(null != mikuUserAgencyDO && null!= mikuUserAgencyDO.getpUserId() && mikuUserAgencyDO.getpUserId() > 0L){
				if(null != upProfile){
					resultMap.put("mobile", upProfile.getMobile());
					resultMap.put("hasUp", 1);	//有上级
					if(pUserId.equals(mikuUserAgencyDO.getpUserId()) || pUserId.equals(mikuUserAgencyDO.getP2UserId())
							|| pUserId.equals(mikuUserAgencyDO.getP3UserId())  || pUserId.equals(mikuUserAgencyDO.getP4UserId())
							 || pUserId.equals(mikuUserAgencyDO.getP5UserId()) || pUserId.equals(mikuUserAgencyDO.getP6UserId())
							 || pUserId.equals(mikuUserAgencyDO.getP7UserId()) || pUserId.equals(mikuUserAgencyDO.getP8UserId())){
						/*if(upProfile.getIsAgency().equals((byte)1) && pUserId.equals(mikuUserAgencyDO.getP8UserId())){
							resultMap.put("hasUp", 2);	//有代理关系
						}else{
							resultMap.put("hasUp", 3);	//同线上下级关系
						}*/
						resultMap.put("hasUp", 3);	//同线上下级关系
					}else{
						resultMap.put("hasUp", 1);	//有上级
					}
				}else{
					resultMap.put("hasUp", 1);	//有上级
				}
			}else{
				resultMap.put("hasUp", 2);	//有代理关系
			}
		}else{
			resultMap.put("hasUp", 0);	//无代理关系；
		}
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	@NeedProfile
	@RequestMapping(value = { "/api/m/1.0/myParentUsers.json",
			"/api/h/1.0/myParentUsers.json" }, produces = "application/json;charset=utf-8")
	public String myParentUsers(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
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
		MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
		mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
		List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);
		List<Long> ids = new ArrayList<Long>();
		if(null != mikuUserAgencyDOList && !mikuUserAgencyDOList.isEmpty()){
			MikuUserAgencyDO mikuUserAgencyDO = mikuUserAgencyDOList.get(0);
			if(null != mikuUserAgencyDO){
				ProfileDO upProfile = profileDOMapper.selectByPrimaryKey(mikuUserAgencyDO.getUserId());
				resultMap.put("upUser", upProfile);
				//resultMap.put("upUserMobile", mikuUserAgencyDO.get)
				if(null != mikuUserAgencyDO.getpUserId() && mikuUserAgencyDO.getpUserId() > 0){
					ids.add(mikuUserAgencyDO.getpUserId());
				}
				if(null != mikuUserAgencyDO.getP2UserId() && mikuUserAgencyDO.getP2UserId() > 0){
					ids.add(mikuUserAgencyDO.getP2UserId());
				}
				if(null != mikuUserAgencyDO.getP3UserId() && mikuUserAgencyDO.getP3UserId() > 0){
					ids.add(mikuUserAgencyDO.getP3UserId());
				}
				if(null != mikuUserAgencyDO.getP4UserId() && mikuUserAgencyDO.getP4UserId() > 0){
					ids.add(mikuUserAgencyDO.getP4UserId());
				}
				if(null != mikuUserAgencyDO.getP4UserId() && mikuUserAgencyDO.getP4UserId() > 0){
					ids.add(mikuUserAgencyDO.getP4UserId());
				}
				if(null != mikuUserAgencyDO.getP4UserId() && mikuUserAgencyDO.getP4UserId() > 0){
					ids.add(mikuUserAgencyDO.getP4UserId());
				}
				if(null != mikuUserAgencyDO.getP5UserId() && mikuUserAgencyDO.getP5UserId() > 0){
					ids.add(mikuUserAgencyDO.getP5UserId());
				}
				if(null != mikuUserAgencyDO.getP6UserId() && mikuUserAgencyDO.getP6UserId() > 0){
					ids.add(mikuUserAgencyDO.getP6UserId());
				}
				if(null != mikuUserAgencyDO.getP7UserId() && mikuUserAgencyDO.getP7UserId() > 0){
					ids.add(mikuUserAgencyDO.getP7UserId());
				}
				if(null != mikuUserAgencyDO.getP8UserId() && mikuUserAgencyDO.getP8UserId() > 0){
					ids.add(mikuUserAgencyDO.getP8UserId());
				}
			}
		}
		if(!ids.isEmpty()){
			ProfileDOExample profileDOExample = new ProfileDOExample();
			profileDOExample.createCriteria().andIdIn(ids);
			List<ProfileDO> profileDOList = profileDOMapper.selectByExample(profileDOExample);
			if(!profileDOList.isEmpty()){
				resultMap.put("upUserList", profileDOList);
			}
		}
		
		//mikuUserAgencyDOMapper.
		welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
		welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * uploadUserQrCode:(上传用户个人二维码). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param file	
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
	@RequestMapping(value = { "/api/m/1.0/uploadUserQrCode.json",
			"/api/h/1.0/uploadUserQrCode.json" }, produces = "application/json;charset=utf-8")
	public String uploadUserQrCode(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile file) throws Exception {
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		// 判断文件是否为空
		if (null != profileDO && !file.isEmpty()) {
			try {
				if(!UpYunUtil.isImage(file)){
					//判断是否是图片
					welinkVO.setStatus(0);
					welinkVO.setMsg("啊哦~请选择正确的图片~");
					return JSON.toJSONString(welinkVO);
				}
				
				String preWxQrcodeUrl = profileDO.getWxQrcodeUrl();		//之前微信二维码
				String mobile = profileDO.getMobile();
				//String wxQrcodeUrl = profileDO.getMobile().substring(mobile.length()-4,mobile.length())+""+System.currentTimeMillis()+".jpg";
				String wxQrcodeUrl = "miku"+System.currentTimeMillis()+ UUID.randomUUID() +".jpg";
				String dir = UpYunUtil.WX_DIR_ROOT;	//上传目录
				// 文件保存路径
				/*String filePath = request.getSession().getServletContext()
						.getRealPath("/")
						+ file.getOriginalFilename();*/
				byte[] bytes = file.getBytes();
				if(UpYunUtil.writePicByMultipartFile(file, dir, wxQrcodeUrl, null)){	//上传微信二维码
					profileDO.setWxQrcodeUrl(UpYunUtil.UPYUN_URL+dir+wxQrcodeUrl);
					profileDOMapper.updateByPrimaryKeySelective(profileDO);
					welinkVO.setStatus(1);	//上传成功
					resultMap.put("wxQrcodeUrl", UpYunUtil.UPYUN_URL+dir+wxQrcodeUrl);
					if(null != preWxQrcodeUrl && !"".equals(preWxQrcodeUrl.trim())){
						preWxQrcodeUrl = preWxQrcodeUrl.substring(UpYunUtil.UPYUN_URL.length());
						try {
							UpYunUtil.deleteFile(preWxQrcodeUrl);	//删除wx二维码
						} catch (Exception e) {
							log.info("mobile:"+profileDO.getMobile()+"删除微信二维码失败!");
						}
					}
					welinkVO.setResult(resultMap);
					return JSON.toJSONString(welinkVO);
				}
				// 转存文件
				//file.transferTo(new File(filePath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		welinkVO.setStatus(0);
		welinkVO.setMsg("上次失败");
		return JSON.toJSONString(welinkVO);
	}
	
	@RequestMapping(value = { "/api/m/1.0/getUserInfo.json",
		"/api/h/1.0/getUserInfo.json" }, produces = "application/json;charset=utf-8")
	public String getUserInfo(HttpServletRequest request,
		HttpServletResponse response,
		@RequestParam(value="emUserName", required = true) String emUserName) throws Exception {
		// para
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		ProfileDOExample profileDOExample = new ProfileDOExample();
		profileDOExample.createCriteria().andEmUserNameEqualTo(emUserName);
		
		List<ProfileDO> profileDOList = profileDOMapper.selectByExample(profileDOExample);
		if(profileDOList.isEmpty() || profileDOList.size() < 1){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~没有此用户~");
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOList.get(0);
		//ProfileDO profileDO = userService.fetchProfileById(profileId);
		
		
		if (null != profileDO) {
			MikuCsadDO mikuCsadDO = null;
			MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
    		mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileDO.getId());
    		List<MikuCsadDO> mikuCsadDOList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
    		if(!mikuCsadDOList.isEmpty()){
    			mikuCsadDO = mikuCsadDOList.get(0);
    		}
    		if(null != mikuCsadDO){
    			resultMap.put("nickName", mikuCsadDO.getCsadName());
    			if(StringUtils.isBlank(mikuCsadDO.getCsadPicUrl())){
    				resultMap.put("headPic", profileDO.getProfilePic());
    			}else{
    				resultMap.put("headPic", mikuCsadDO.getCsadPicUrl());
    			}
			}else{
				resultMap.put("nickName", profileDO.getNickname());
    			resultMap.put("headPic", profileDO.getProfilePic());
			}
			
			resultMap.put("profileId", profileId);
			resultMap.put("isAgency", profileDO.getIsAgency());
			resultMap.put("emUserName", profileDO.getEmUserName());
			resultMap.put("isExpert", null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert());
			//resultMap.put("emUserPw", profileDO.getEmUserPw());
			resultMap.put("mobile", profileDO.getMobile());
		} else {
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~没有此用户~");
			return JSON.toJSONString(welinkVO);
		}
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 更新个人信息
	 * @param request
	 * @param response
	 * @param emUserName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = { "/api/m/1.0/updateMineInfo.json",
		"/api/h/1.0/updateMineInfo.json" }, produces = "application/json;charset=utf-8")
	public String updateMineInfo(HttpServletRequest request,
		HttpServletResponse response, ProfileDO profileDO) throws Exception {
		// para
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		welinkVO.setStatus(1);
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		if(null == profileDO){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~请传入正确的参数~");
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO updateProfileDO = new ProfileDO();
		updateProfileDO.setId(profileId);
		updateProfileDO.setAgeGroup(profileDO.getAgeGroup());
		updateProfileDO.setSex(profileDO.getSex());
		updateProfileDO.setLastUpdated(new Date());
		if(profileDOMapper.updateByPrimaryKeySelective(updateProfileDO) < 1){
			resultMap.put("status", 0);
		}else{
			resultMap.put("status", 1);
		}
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	public static void main(String[] args) {
		String url = UpYunUtil.UPYUN_URL+"_12345";
		String url2 = url.substring(UpYunUtil.UPYUN_URL.length());
		System.out.println(url2);
		
	}
}
