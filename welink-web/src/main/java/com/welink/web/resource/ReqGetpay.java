/**
 * Project Name:welink-web
 * File Name:Getpay.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月2日下午3:23:32
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.MikuGetPayService;
import com.welink.biz.service.UserService;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuGetpayDO;
import com.welink.commons.domain.MikuGetpayDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuGetpayDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.MikuShareGetpayDOMapper;
import com.welink.commons.persistence.WeChatProfileDOMapper;
import com.welink.commons.utils.BigDecimalUtils;
import com.welink.commons.vo.MikuGetpayVO;
import com.welink.commons.vo.MikuSalesRecordVO;

/**
 * ClassName:Getpay <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午3:23:32 <br/>
 * @author   LuoGuangChun
 */
@RestController
public class ReqGetpay {
	
	@Resource
	private MikuGetpayDOMapper mikuGetpayDOMapper;
	
	@Resource
	private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private MikuShareGetpayDOMapper mikuShareGetpayDOMapper;
	
	@Resource
	private MikuGetPayService mikuGetPayService;
	
	@Resource
    private UserService userService;
	
	/**
	 * 
	 * checkHasRegisterWeiXin:(判断用户是否在微信公众号注册). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/checkHasRegisterWeiXin.json", "/api/h/1.0/checkHasRegisterWeiXin.json"}, produces = "application/json;charset=utf-8")
    public String checkHasRegisterWeiXin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
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
		
		ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(profileId);
		if(null != profileWeChatDO){
			welinkVO.setStatus(1);
			resultMap.put("status", 1);	//已在公众号注册
			resultMap.put("openid", profileWeChatDO.getOpenid());
		}else{
			welinkVO.setStatus(1);
			resultMap.put("status", 0);	//未注册在公众号注册
			//welinkVO.setMsg("啊哦~你未关注我们的微信公众号,不能使用微信提现");
		}
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * execute:(申请提现). <br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param getpayType	//提现类型（1支付宝，2微信钱包，3银行卡）
	 * @param account		//账号
	 * @param accountName	//收款人姓名
	 * @param getpayFee		//申请提现金额
	 */
	@RequestMapping(value = {"/api/m/1.0/reqGetPay.json", "/api/h/1.0/reqGetPay.json"}, produces = "application/json;charset=utf-8")
    public String reqGetPay(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam Integer getpayType,
    		@RequestParam(value="accountName", required = false, defaultValue="") String accountName, 
    		@RequestParam(value="account", required = false, defaultValue="") String account,
    		@RequestParam(value="getpayFee", required = false, defaultValue="0") BigDecimal getpayFee) throws Exception {
		/*java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#"); 		//去小数
		Long getpayFeeL = Long.valueOf(df.format(getpayFee.multiply(new BigDecimal("100"))));		//把元转成分*/
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		/*if(profileId == -1L){
			welinkVO.setStatus(0);
			welinkVO.setMsg("近两天米酷需要进行公众号的迁移，因此提现功能暂停，并将于3月28号开放，给您带来不便，敬请原谅~");
			return JSON.toJSONString(welinkVO);
		}*/
		profileId = (long) session.getAttribute("profileId");
		if(getpayType < 1 || getpayType > 3){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~请选择转账类型！如(支付宝，微信，银行卡)");
			return JSON.toJSONString(welinkVO);
		}
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		Calendar c = Calendar.getInstance();//可以对每个时间域单独修改

		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH); 
		
		String dateStartStr = year + "-" + month + "-" + "15" + " 00:00:00";
		String dateEndStr = year + "-" + month + "-" + "20" + " 23:59:59";
		
		String dateCreatedQueryStr = year + "-" + month + "-" + "01" + " 00:00:00";
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		Date dateStart = sdf.parse(dateStartStr);	//每月15号
		Date dateEnd = sdf.parse(dateEndStr);		//每月20号
		
		Date nowDate = new Date();
		if(nowDate.getTime() > dateStart.getTime() && dateEnd.getTime() > nowDate.getTime()){
			
		}else{
			welinkVO.setStatus(0);
			welinkVO.setMsg("由于提现业务繁忙，所以每月只能提现一次，提现时间为每月的15至20日");
			return JSON.toJSONString(welinkVO);
		}
		
		Date dateCreatedQuery = sdf.parse(dateCreatedQueryStr);
		MikuGetpayDOExample mikuGetpayDOExample = new MikuGetpayDOExample();
		mikuGetpayDOExample.createCriteria().andAgencyIdEqualTo(profileId)
			.andStatusNotEqualTo(Constants.GetPayStatus.GETPAYABNORMAL.getStatusId())
			.andDateCreatedGreaterThan(dateCreatedQuery);
		int countByExample = mikuGetpayDOMapper.countByExample(mikuGetpayDOExample);
		if(countByExample > 0){
			welinkVO.setStatus(0);
			welinkVO.setMsg("亲~一个月最多只能提现一次，提现时间为每月的15至20日");
			return JSON.toJSONString(welinkVO);
		}
		if(null != getpayType && getpayType == 2){
			ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(profileId);
			if(null != profileWeChatDO){
				account = profileWeChatDO.getOpenid();
			}else{
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~你未关注我们的微信公众号,不能使用微信提现");
				return JSON.toJSONString(welinkVO);
			}
		}else{
			if(null == account || "".equals(account.trim())){
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~账号不能为空");
				return JSON.toJSONString(welinkVO);
			}
		}
		//处理提现流程
		return JSON.toJSONString(mikuGetPayService.reqGetPay(profileId, getpayType, account, accountName));
		/*MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
		mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(profileId);
		List<MikuAgencyShareAccountDO> mikuAgencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
		if(!mikuAgencyShareAccountDOList.isEmpty()){
			Date nowDate = new Date();
			MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
			mikuSalesRecordDOExample.createCriteria().andIsGetpayEqualTo((byte)0);		//isGetpay(0=未提现；1=提现中；2=已提现)
			List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectByExample(mikuSalesRecordDOExample);
			Map<Long, Long> tradeShareFeeMap = new HashMap<Long, Long>();
			Set<Long> tradeIds = new HashSet<Long>(); 
			Long srShareFee = 0L;	//可提现金额
			for(MikuSalesRecordDO mikuSalesRecordDO : mikuSalesRecordDOList){
				srShareFee += (null == mikuSalesRecordDO.getShareFee() ? 0L : mikuSalesRecordDO.getShareFee());
				if(null != mikuSalesRecordDO.getTradeId() && mikuSalesRecordDO.getTradeId() > 0){
					tradeIds.add(mikuSalesRecordDO.getTradeId());
				}
			}
			
			MikuAgencyShareAccountDO mikuAgencyShareAccountDO = mikuAgencyShareAccountDOList.get(0);
			Long noGetpayFee = mikuAgencyShareAccountDO.getNoGetpayFee();		//未提现金额
			Long getpayingFee = mikuAgencyShareAccountDO.getGetpayingFee();		//提现中申请金额
			Long canReqFee = noGetpayFee - getpayingFee;						//可申请提现金额
			if(canReqFee < 1 || srShareFee > canReqFee){		//超出可申请提现金额
				welinkVO.setStatus(0);
				welinkVO.setCode(BizErrorEnum.REQ_PAY_OUT.getCode());
				welinkVO.setMsg(BizErrorEnum.REQ_PAY_OUT.getMsg());
				return JSON.toJSONString(welinkVO);
			}
			MikuGetpayDO mikuGetpayDO = new MikuGetpayDO();
			mikuGetpayDO.setAgencyId(profileId);
			mikuGetpayDO.setGetpayFee(srShareFee);	//根据订单得出的可提现金额
			mikuGetpayDO.setGetpayType(getpayType);
			if(1 == getpayType){	//1支付宝
				mikuGetpayDO.setGetpayAccount(account);
			}else if(2 == getpayType){	//2微信钱包
				mikuGetpayDO.setGetpayAccount(account);
			}else if(3 == getpayType){	//3银行卡
				mikuGetpayDO.setGetpayAccount(account);
			}
			mikuGetpayDO.setGetpayUserName(accountName);
			mikuGetpayDO.setApplyDate(nowDate);
			mikuGetpayDO.setDateCreated(nowDate);
			mikuGetpayDO.setLastUpdated(nowDate);
			mikuGetpayDO.setStatus(Byte.valueOf("0"));		//申请状态（0提现中/待审核，1已审核）
			if(mikuGetpayDOMapper.insertSelective(mikuGetpayDO) > 0){		//插入提现申请记录
				MikuAgencyShareAccountDO mikuAgencyShareAccountDOUpdate = new MikuAgencyShareAccountDO();
				mikuAgencyShareAccountDOUpdate.setId(mikuAgencyShareAccountDO.getId());
				mikuAgencyShareAccountDOUpdate.setGetpayingFee(canReqFee +
						(null == mikuAgencyShareAccountDO.getGetpayingFee() ? 0L : mikuAgencyShareAccountDO.getGetpayingFee()));
				mikuAgencyShareAccountDOMapper.updateByPrimaryKeySelective(mikuAgencyShareAccountDOUpdate);
				welinkVO.setStatus(1);
				return JSON.toJSONString(welinkVO);
			}
		}
		welinkVO.setStatus(0);
		welinkVO.setCode(BizErrorEnum.NO_AGENCY.getCode());
		welinkVO.setMsg(BizErrorEnum.NO_AGENCY.getMsg());
		return JSON.toJSONString(welinkVO);*/
	}
	
	/**
	 * 
	 * reqGetPayAbnormal:(重新提现异常提现数据). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param getpayId		//提现id
	 * @param getpayType	//提现类型（1支付宝，2微信钱包，3银行卡）
	 * @param account		//账号
	 * @param accountName	//收款人姓名
	 * @param getpayFee		//申请提现金额
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/reqGetPayAbnormal.json", "/api/h/1.0/reqGetPayAbnormal.json"}, produces = "application/json;charset=utf-8")
    public String reqGetPayAbnormal(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam Long getpayId,
    		@RequestParam Integer getpayType,
    		@RequestParam String accountName, 
    		@RequestParam(value="account", required = false, defaultValue="") String account,
    		@RequestParam(value="getpayFee", required = false, defaultValue="0") BigDecimal getpayFee) throws Exception {
			long profileId = -1l;
			org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
			Session session = currentUser.getSession();
			WelinkVO welinkVO = new WelinkVO();
			Map resultMap = new HashMap();
			if( 1 > 2){
				welinkVO.setStatus(0);
				welinkVO.setMsg("无重新提现功能异常");
				return JSON.toJSONString(welinkVO);
			}
			profileId = (long) session.getAttribute("profileId");
			if(getpayType < 1 || getpayType > 3){
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~请选择转账类型！如(支付宝，微信，银行卡)");
				return JSON.toJSONString(welinkVO);
			}
			if (profileId < 0) {
				welinkVO.setStatus(0);
				welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
				welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
				return JSON.toJSONString(welinkVO);
			}
			if(null == getpayId){
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~提现异常数据不能为空");
				return JSON.toJSONString(welinkVO);
			}
			MikuGetpayDO mikuGetpayDO = mikuGetpayDOMapper.selectByPrimaryKey(getpayId);
			if(null == mikuGetpayDO){
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~提现异常数据不能为空");
				return JSON.toJSONString(welinkVO);
			}else{
				if(mikuGetpayDO.getStatus() != (byte)2){	//status(-1=未提现；0=提现中；1=已提现;2=提现异常)
					welinkVO.setStatus(0);
					welinkVO.setMsg("啊哦~此提现数据不是异常数据");
					return JSON.toJSONString(welinkVO);
				}
				if(!mikuGetpayDO.getAgencyId().equals(profileId)){
					welinkVO.setStatus(0);
					welinkVO.setMsg("啊哦~此提现数据不是你的，请重新选择");
					return JSON.toJSONString(welinkVO);
				}
				if(null != getpayType && getpayType == 2){
					ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(profileId);
					if(null != profileWeChatDO){
						account = profileWeChatDO.getOpenid();
					}else{
						welinkVO.setStatus(0);
						welinkVO.setMsg("啊哦~你未关注我们的微信公众号");
						return JSON.toJSONString(welinkVO);
					}
				}else{
					if(null == account || "".equals(account.trim())){
						welinkVO.setStatus(0);
						welinkVO.setMsg("啊哦~账号不能为空");
						return JSON.toJSONString(welinkVO);
					}
				}
				mikuGetpayDO.setGetpayType(getpayType);
				mikuGetpayDO.setGetpayAccount(account);
				mikuGetpayDO.setGetpayUserName(accountName);
				return JSON.toJSONString(mikuGetPayService.reqGetPayAbnormal(mikuGetpayDO));
			}
	}
	
	/**
	 * 
	 * getPayList:(提现记录). <br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param status
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getPayList.json", "/api/h/1.0/getPayList.json"}, produces = "application/json;charset=utf-8")
    public String getPayList(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="status", required = false) Byte status,
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
		
		long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
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
		
		MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
		mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(profileId);
		List<MikuAgencyShareAccountDO> likuAgencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
		if(!likuAgencyShareAccountDOList.isEmpty()){
			MikuAgencyShareAccountDO mikuAgencyShareAccountDO = likuAgencyShareAccountDOList.get(0);
			resultMap.put("ye", BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getNoGetpayFee()));		//帐户余额
		}else{
			resultMap.put("ye", 0L);
		}
		
		MikuGetpayDOExample mikuGetpayDOExample = new MikuGetpayDOExample();
		if(null != status && !"".equals(status)){
			mikuGetpayDOExample.createCriteria().andStatusEqualTo(status).andAgencyIdEqualTo(profileId);
		}else{
			List<Byte> statusList = new ArrayList<Byte>();
			statusList.add(Constants.GetPayStatus.NOGETPAY.getStatusId());
			statusList.add(Constants.GetPayStatus.GETPAYING.getStatusId());
			statusList.add(Constants.GetPayStatus.GETPAYED.getStatusId());
			statusList.add(Constants.GetPayStatus.GETPAYABNORMAL.getStatusId());
			mikuGetpayDOExample.createCriteria().andAgencyIdEqualTo(profileId).andStatusIn(statusList);
		}
		mikuGetpayDOExample.setOffset(startRow);
		mikuGetpayDOExample.setLimit(size);
		List<MikuGetpayDO> mikuGetpayDOList = mikuGetpayDOMapper.selectByExample(mikuGetpayDOExample);
		List<MikuGetpayVO> mikuGetpayVOList = new ArrayList<MikuGetpayVO>();
		MikuGetpayVO mikuGetpayVO = null;
		for(int i=0; i<mikuGetpayDOList.size(); i++){
			if(null != mikuGetpayDOList.get(i)){
				mikuGetpayVO = new MikuGetpayVO();
				BeanUtils.copyProperties(mikuGetpayDOList.get(i), mikuGetpayVO);
				mikuGetpayVO.setGetpayFee(BigDecimalUtils.divFee100(mikuGetpayDOList.get(i).getGetpayFee()));	//设置金额分为元
				mikuGetpayVOList.add(mikuGetpayVO);
			}
		}
		
		/*List<MikuGetpayDO> mikuGetpayDOList = new ArrayList<MikuGetpayDO>();
		mikuGetpayDOList(mikuGetpayDOList, profileId);*/
		
		boolean hasNext = true;
        if (null != mikuGetpayVOList && mikuGetpayVOList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuGetpayVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getPayList:提现记录明细). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param isGetpays
	 * @param getpayId
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getPayDetailList.json", "/api/h/1.0/getPayDetailList.json"}, produces = "application/json;charset=utf-8")
	public String getPayDetailList(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="getpayId", required = false) Long getpayId,
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
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("getpayId", getpayId);			//提现记录id
		paramMap.put("profileId", profileId);
		paramMap.put("offset", startRow);
		paramMap.put("limit", size);
		paramMap.put("orderByClause", "msg.date_created DESC");
		
		List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectShareGetpay(paramMap);
		
		List<MikuSalesRecordVO> mikuSalesRecordVOList = new ArrayList<MikuSalesRecordVO>();
		MikuSalesRecordVO mikuSalesRecordVO = null;
		for(int i=0; i<mikuSalesRecordDOList.size(); i++){
			if(null != mikuSalesRecordDOList.get(i)){
				mikuSalesRecordVO = new MikuSalesRecordVO();
				BeanUtils.copyProperties(mikuSalesRecordDOList.get(i), mikuSalesRecordVO);
				mikuSalesRecordVO.setPrice(BigDecimalUtils.divFee100(
						mikuSalesRecordDOList.get(i).getPrice()));	//设置金额分为元
				mikuSalesRecordVO.setAmount(BigDecimalUtils.divFee100(
						mikuSalesRecordDOList.get(i).getAmount()));	//设置金额分为元
				mikuSalesRecordVO.setShareFee(BigDecimalUtils.divFee100(
						mikuSalesRecordDOList.get(i).getShareFee()));	//设置金额分为元
				mikuSalesRecordVOList.add(mikuSalesRecordVO);
			}
		}
		
		boolean hasNext = true;
        if (null != mikuSalesRecordVOList && mikuSalesRecordVOList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuSalesRecordVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	public void mikuGetpayDOList(List<MikuGetpayDO> mikuGetpayDOList, Long profileId){
		for(int i=0; i<5; i++){
			MikuGetpayDO mikuGetpayDO = new MikuGetpayDO();
			mikuGetpayDO.setAgencyId(profileId);
			mikuGetpayDO.setGetpayType(1);
			mikuGetpayDO.setGetpayAccount("ali111@163.com");
			mikuGetpayDO.setGetpayFee(10L+i);
			mikuGetpayDO.setGetpayUserName("用户一");
			mikuGetpayDO.setStatus(Byte.valueOf("0"));
			mikuGetpayDO.setClerkerId(1L);
			mikuGetpayDO.setClerkerName("优理氏");
			mikuGetpayDO.setClerkerUserName("优理氏");
			mikuGetpayDO.setDateCreated(new Date());
			
			mikuGetpayDOList.add(mikuGetpayDO);
		}
	}
	
	public static void main(String[] args) throws ParseException {
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式

		String hehe = dateFormat.format( now ); 
		System.out.println(hehe); 

		Calendar c = Calendar.getInstance();//可以对每个时间域单独修改

		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH); 
		int date = c.get(Calendar.DATE); 
		int hour = c.get(Calendar.HOUR_OF_DAY); 
		int minute = c.get(Calendar.MINUTE); 
		int second = c.get(Calendar.SECOND); 
		System.out.println(year + "/" + month + "/" + date + " " +hour + ":" +minute + ":" + second); 
		
		String dateStartStr = year + "-" + month + "-" + "15" + " 00:00:00";
		String dateEndStr = year + "-" + month + "-" + "20" + " 23:59:59";
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		Date dateStart = sdf.parse(dateStartStr);
		Date dateEnd = sdf.parse(dateEndStr);
		System.out.println("---------------------------------");
		System.out.println(dateFormat.format( dateStart ));
		System.out.println(dateFormat.format( dateEnd ));
	}
}

