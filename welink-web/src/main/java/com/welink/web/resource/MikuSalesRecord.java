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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuGetpayDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuGetpayDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.utils.BigDecimalUtils;
import com.welink.commons.vo.MikuGetpayVO;
import com.welink.commons.vo.MikuSalesRecordVO;

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
public class MikuSalesRecord {
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
	private MikuGetpayDOMapper mikuGetpayDOMapper;
	
	/**
	 * 
	 * salesRecordList:(这里用一句话描述这个方法的作用). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 * TODO(这里描述这个方法的执行流程 – 可选).<br/>
	 * TODO(这里描述这个方法的使用方法 – 可选).<br/>
	 * TODO(这里描述这个方法的注意事项 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param isGetpays	提现状态(-1=未提现；0=提现中；1=已提现;2=提现异常)
	 * @param isCanGetPay	//isCanGetPay(0=不可提现包括退货的;1=可提现;2=余额中的不可提现;3余额)
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/salesRecordList.json", "/api/h/1.0/salesRecordList.json"}, produces = "application/json;charset=utf-8")
	public String salesRecordList(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="isGetpays", required = false) String isGetpays,
    		@RequestParam(value="isCanGetPay", required = false) Integer isCanGetPay,
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
		
		MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
		mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(profileId);
		List<MikuAgencyShareAccountDO> likuAgencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
		if(!likuAgencyShareAccountDOList.isEmpty()){
			welinkVO.setStatus(1);
			MikuAgencyShareAccountDO mikuAgencyShareAccountDO = likuAgencyShareAccountDOList.get(0);
			resultMap.put("noGetpayFee", BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getNoGetpayFee()));		//可用余额
		}else{
			resultMap.put("noGetpayFee", 0);//可用余额
		}
		
		/*welinkVO.setStatus(1);
		resultMap.put("noGetpayFee", 100L);		//可用余额*/
		
		/*MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
		if(null == isGetpays || "".equals(isGetpays.trim())){
			mikuSalesRecordDOExample.createCriteria().andAgencyIdEqualTo(profileId);
		}else{
			String isGetpaysArr[] = isGetpays.split(",");	
	        List<Byte> isGetpaysList = new ArrayList<Byte>();
	        for (String isGetpay : isGetpaysArr) {
	        	isGetpaysList.add(Byte.valueOf(isGetpay));	//提现状态
	        }
			if(isGetpaysArr.length > 1){
				mikuSalesRecordDOExample.createCriteria().andAgencyIdEqualTo(profileId)
						.andIsGetpayIn(isGetpaysList);
			}else{
				mikuSalesRecordDOExample.createCriteria().andAgencyIdEqualTo(profileId)
						.andIsGetpayEqualTo(Byte.valueOf(isGetpays));
			}
		}
		
		mikuSalesRecordDOExample.setOrderByClause("date_created DESC");
		mikuSalesRecordDOExample.setOffset(startRow);
		mikuSalesRecordDOExample.setLimit(size);
		List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectGroupTradeByExample(mikuSalesRecordDOExample);*/
		
        List<Byte> isGetpaysList = new ArrayList<Byte>();
        if(null != isGetpays && !"".equals(isGetpays.trim())){
        	String isGetpaysArr[] = isGetpays.split(",");	
        	for (String isGetpay : isGetpaysArr) {
        		isGetpaysList.add(Byte.valueOf(isGetpay));	//提现状态
        	}
        }
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("profileId", profileId);			//代理商id
		//paramMap.put("isGetpay", Constants.GetPayStatus.NOGETPAY.getStatusId());	//未提现
		if(null != isGetpays && !"".equals(isGetpays.trim()) && isGetpaysList.size() > 0){
			paramMap.put("isGetpays", isGetpaysList);	//提现状态
		}
		paramMap.put("isCrowdfundRefundStatus", 0);	//是否众筹退款	//0=不用众筹退款;1=众筹退款的
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		paramMap.put("tradeReturnStatus", Constants.TradeReturnStatus.NORMAL.getStatusId());	//交易退货状态
		paramMap.put("isCanGetPay", isCanGetPay);	//isCanGetPay(0=不可提现;1=可提现)
		
		paramMap.put("orderByClause", " tr.date_created DESC ");
		paramMap.put("offset", startRow);
		paramMap.put("limit", size);
		List<MikuSalesRecordVO> mikuSalesRecordVOList = mikuSalesRecordDOMapper.selectSalesRecordVOList(paramMap);	//根据trade分组的分润列表
		
		/*List<MikuSalesRecordVO> mikuSalesRecordVOList = new ArrayList<MikuSalesRecordVO>();
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
		}*/
		//List<MikuSalesRecordVO> mikuSalesRecordVOList = new ArrayList<MikuSalesRecordVO>();
		
		List<Byte> toDealStatus = new ArrayList<>();	//取消和未付款trade类型
		toDealStatus.add(Constants.TradeStatus.TRADE_NO_CREATE_PAY.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.TRADE_CLOSED.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
		
		List<Byte> notGetpay = new ArrayList<>();	//未提现和提现异常状态
		notGetpay.add(Constants.GetPayStatus.NOGETPAY.getStatusId());
		notGetpay.add(Constants.GetPayStatus.GETPAYABNORMAL.getStatusId());
		
		MikuSalesRecordVO mikuSalesRecordVO = null;
		for(int i=0; i<mikuSalesRecordVOList.size(); i++){
			mikuSalesRecordVO = mikuSalesRecordVOList.get(i);
			if(null != mikuSalesRecordVO){
				//BeanUtils.copyProperties(mikuSalesRecordDOList.get(i), mikuSalesRecordVO);
				mikuSalesRecordVO.setPrice(BigDecimalUtils.divFee100(
						mikuSalesRecordVO.getPrice()));	//设置金额分为元
				mikuSalesRecordVO.setAmount(BigDecimalUtils.divFee100(
						mikuSalesRecordVO.getAmount()));	//设置金额分为元
				mikuSalesRecordVO.setShareFee(BigDecimalUtils.divFee100(
						mikuSalesRecordVO.getShareFee()));	//设置金额分为元
				//是否可提现设置
				if((!toDealStatus.contains(mikuSalesRecordVO.getTradeStatus()) 
						&& null != mikuSalesRecordVO.getTradeStatus()
						&& null != mikuSalesRecordVO.getOrderReturnStatus()
						&& null != mikuSalesRecordVO.getCrowdfundRefundStatus()
						&& null != mikuSalesRecordVO.getIsrefund()
						&& !mikuSalesRecordVO.getTradeStatus().equals(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId()) 
						&& mikuSalesRecordVO.getIsrefund().equals((byte)1))
						&& notGetpay.contains(mikuSalesRecordVO.getIsGetpay())
						&& !mikuSalesRecordVO.getOrderReturnStatus().equals((byte)5)
						&& mikuSalesRecordVO.getCrowdfundRefundStatus().equals(Constants.CrowdfundRefundStatus.SUCCESS.getStatusId())
						&& mikuSalesRecordVO.getCrowdfundRefundStatus().equals(Constants.CrowdfundRefundStatus.NORMAL.getStatusId())){
					//不可提现 
					mikuSalesRecordVO.setIsCanGetPay(0);
				}else if(null != mikuSalesRecordVO.getTradeStatus()
						&& null != mikuSalesRecordVO.getOrderReturnStatus()
						&& null != mikuSalesRecordVO.getCrowdfundRefundStatus()
						&& null != mikuSalesRecordVO.getIsrefund()
						&& (mikuSalesRecordVO.getTradeStatus().equals(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId())
								|| (!toDealStatus.contains(mikuSalesRecordVO.getTradeStatus()) &&  mikuSalesRecordVO.getIsrefund().equals((byte)0)))
						&& notGetpay.contains(mikuSalesRecordVO.getIsGetpay())
						&& !mikuSalesRecordVO.getOrderReturnStatus().equals((byte)5)
						&& mikuSalesRecordVO.getCrowdfundRefundStatus().equals(Constants.CrowdfundRefundStatus.SUCCESS.getStatusId())){
					//可提现
					mikuSalesRecordVO.setIsCanGetPay(1);
				}else{
					//不可提现
					mikuSalesRecordVO.setIsCanGetPay(0);
				}
			}else{
				mikuSalesRecordVOList.remove(i);
			}
		}
		
		/*List<MikuSalesRecordDO> mikuSalesRecordDOList = new ArrayList<MikuSalesRecordDO>();
		mikuSalesRecordDOList(mikuSalesRecordDOList, profileId);*/
		
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
	
	/**
	 * 
	 * getReturnSaleRecordVOList:(退货分润记录表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getReturnSaleRecordVOList.json", "/api/h/1.0/getReturnSaleRecordVOList.json"}, produces = "application/json;charset=utf-8")
	public String getReturnSaleRecordVOList(HttpServletRequest request, HttpServletResponse response,
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
		paramMap.put("profileId", profileId);			//代理商id
		paramMap.put("orderByClause", " sr.return_date DESC ");
		paramMap.put("offset", startRow);
		paramMap.put("limit", size);
		List<MikuSalesRecordVO> returnSaleRecordVOList = mikuSalesRecordDOMapper.getReturnSaleRecordVOList(paramMap);
		
		boolean hasNext = true;
        if (null != returnSaleRecordVOList && returnSaleRecordVOList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", returnSaleRecordVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	public void mikuSalesRecordDOList(List<MikuSalesRecordDO> mikuSalesRecordDOList, Long profileId){
		for(int i=0; i<5; i++){
			MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
			mikuSalesRecordDO.setAgencyId(profileId);
			//mikuSalesRecordDO.setAmount(Double.valueOf("100"+i));
			mikuSalesRecordDO.setBuyerId(profileId);
			mikuSalesRecordDO.setTradeId(111111111L);
			mikuSalesRecordDO.setPrice(10L);
			mikuSalesRecordDO.setShareFee(50L);
			mikuSalesRecordDO.setDateCreated(new Date());
			mikuSalesRecordDO.setNum(5);
			mikuSalesRecordDOList.add(mikuSalesRecordDO);
		}
	}
	
}

