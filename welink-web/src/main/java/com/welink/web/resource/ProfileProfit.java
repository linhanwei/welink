/**
 * Project Name:welink-web
 * File Name:ProfileProfit.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月2日下午12:44:47
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.utils.BigDecimalUtils;
import com.welink.commons.vo.MikuAgencyShareAccountVO;

/**
 * ClassName:ProfileProfit <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午12:44:47 <br/>
 * @author   LuoGuangChun
 */
@RestController
public class ProfileProfit {
	
	static Logger log = LoggerFactory.getLogger(ProfileProfit.class);
	
	@Resource
	MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
    private ProfileDOMapper profileDOMapper;
	
	@Resource
    private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@RequestMapping(value = {"/api/m/1.0/profileProfit.json", "/api/h/1.0/profileProfit.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long profileId = -1l;
		Integer isAgency = 0;
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
        
        Map<String,Object> sumReturnSaleRecordParamMap = new HashMap<String, Object>();
        sumReturnSaleRecordParamMap.put("profileId", profileId);
        String sumReturnSaleRecordStr = "0.00";
        Long sumReturnSaleRecord = mikuSalesRecordDOMapper.sumReturnSaleRecord(sumReturnSaleRecordParamMap);
        if(null != sumReturnSaleRecord && sumReturnSaleRecord > 0){
        	sumReturnSaleRecordStr = BigDecimalUtils.divFee100(sumReturnSaleRecord);
        }
        resultMap.put("sumReturnSaleRecord", sumReturnSaleRecordStr);	//统计退款分润金额
        
		MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
		mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(profileId);
		List<MikuAgencyShareAccountDO> likuAgencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
		welinkVO.setStatus(1);
		MikuAgencyShareAccountVO mikuAgencyShareAccountVO = new MikuAgencyShareAccountVO();
		MikuAgencyShareAccountDO mikuAgencyShareAccountDO = null;
		if(!likuAgencyShareAccountDOList.isEmpty()){
			mikuAgencyShareAccountDO = likuAgencyShareAccountDOList.get(0);
		}else{
			mikuAgencyShareAccountDO = new MikuAgencyShareAccountDO();
		}
		doCopyToVoSetFee(mikuAgencyShareAccountVO, mikuAgencyShareAccountDO);
		
		
		/*Date nowDate = new Date();
		List<Byte> returnGoodsStatus = new ArrayList<>();	//退货状态
		returnGoodsStatus.add(Constants.ReturnGoodsStatus.NORMAL.getStatusId());		//正常分润记录单
		returnGoodsStatus.add(Constants.ReturnGoodsStatus.FINISHED.getStatusId());	//已退货状态
		returnGoodsStatus.add(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId());	//退货状态异常
		MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
		mikuSalesRecordDOExample.createCriteria().andAgencyIdEqualTo(profileId)
			//查询未提现状态 提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
			.andIsGetpayEqualTo(Constants.GetPayStatus.NOGETPAY.getStatusId())
			.andReturnStatusIn(returnGoodsStatus)	//状态状态(-1:删除;0:正常单;1:申请退货;2=退货中;3=退货完成;4=退货异常)
			.andTimeoutActionTimeGreaterThan(nowDate);		
		List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectByExample(mikuSalesRecordDOExample);*/
		
		//----可提现start---------------
		Map<String,Object> paramMap = new HashMap<String, Object>();
		/*paramMap.put("profileId", profileId);			//代理商id
		paramMap.put("isGetpay", Constants.GetPayStatus.NOGETPAY.getStatusId());	//未提现
		paramMap.put("timeoutActionTime", new Date());	//交易过期时间
		paramMap.put("isCrowdfundRefundStatus", 0);	//是否众筹退款	//0=不用众筹退款;1=众筹退款的
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		paramMap.put("tradeStatus", Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());	//交易完成
		paramMap.put("tradeReturnStatus", Constants.TradeReturnStatus.NORMAL.getStatusId());	//交易退货状态*/
		
		paramMap.put("profileId", profileId);			//代理商id
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		//paramMap.put("tradeReturnStatus", Constants.TradeReturnStatus.NORMAL.getStatusId());	//交易退货状态
		paramMap.put("isCanGetPay", 1);	//提现 (0=不可提现;1=可提现;2=余额中的不可提现 )
		List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectSalesRecordList(paramMap);	//可提现分润
		
		Map<Long, Long> tradeShareFeeMap = new HashMap<Long, Long>();
		Set<Long> tradeIdSets = new HashSet<Long>(); 
		List<Long> salesRecordIds = new ArrayList<Long>();	//提现需要用的分润记录ids;
		Long srShareFee = 0L;	//可提现金额
		for(MikuSalesRecordDO mikuSalesRecordDO : mikuSalesRecordDOList){
			Long shareFee = null == mikuSalesRecordDO.getShareFee() ? 0L : mikuSalesRecordDO.getShareFee();
			salesRecordIds.add(mikuSalesRecordDO.getId());
			srShareFee += shareFee;
			if(null != mikuSalesRecordDO.getTradeId() && mikuSalesRecordDO.getTradeId() > 0){
				tradeIdSets.add(mikuSalesRecordDO.getTradeId());
			}
		}
		mikuAgencyShareAccountVO.setCanGetpayFee(BigDecimalUtils.divFee100(srShareFee)); 	//可提现金额
		//----可提现end---------------
		
		resultMap.put("agencyShareAccount", mikuAgencyShareAccountVO);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO); 
	}
	
	private void doCopyToVoSetFee(MikuAgencyShareAccountVO mikuAgencyShareAccountVO, MikuAgencyShareAccountDO mikuAgencyShareAccountDO){
		if(null !=mikuAgencyShareAccountVO && null != mikuAgencyShareAccountDO){
			mikuAgencyShareAccountVO.setAgencyId(null == mikuAgencyShareAccountDO.getAgencyId() ? 
					-1L : mikuAgencyShareAccountDO.getAgencyId());
			Long getpayingFee = null == mikuAgencyShareAccountDO.getGetpayingFee() ? 0L : mikuAgencyShareAccountDO.getGetpayingFee();
			Long noGetpayFee = null == mikuAgencyShareAccountDO.getNoGetpayFee() ? 0L : mikuAgencyShareAccountDO.getNoGetpayFee();
			String cangetFee = String.valueOf(noGetpayFee-getpayingFee);
			mikuAgencyShareAccountVO.setCanGetpayFee(BigDecimalUtils.divFee100(cangetFee));
			mikuAgencyShareAccountVO.setDirectSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getDirectSalesFee()));
			mikuAgencyShareAccountVO.setDirectShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getDirectShareFee()));
			mikuAgencyShareAccountVO.setIndirectSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getIndirectSalesFee()));
			mikuAgencyShareAccountVO.setIndirectShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getIndirectShareFee()));
			mikuAgencyShareAccountVO.setGetpayingFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getGetpayingFee()));
			mikuAgencyShareAccountVO.setNoGetpayFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getNoGetpayFee()));
			mikuAgencyShareAccountVO.setTotalGotpayFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getTotalGotpayFee()));
			mikuAgencyShareAccountVO.setpSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getpSalesFee()));
			mikuAgencyShareAccountVO.setTotalShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountDO.getTotalShareFee()));
			mikuAgencyShareAccountVO.setpTradesCount(null == mikuAgencyShareAccountDO.getpTradesCount() ? 0L : mikuAgencyShareAccountDO.getpTradesCount());
		}
	}
	
}

