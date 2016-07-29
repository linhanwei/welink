/**
 * Project Name:welink-biz
 * File Name:UserAgencyService.java
 * Package Name:com.welink.biz.service
 * Date:2015年11月1日上午11:09:27
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
 */

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuGetpayDO;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.domain.MikuShareGetpayDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuGetpayDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.MikuShareGetpayDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;

/**
 * 
 * ClassName: MikuGetPayService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015年12月7日 下午3:59:20 <br/>
 *
 * @author LuoGuangChun
 * @version 
 * @since JDK 1.6
 */
@Service
public class MikuGetPayService implements InitializingBean {

	static Logger log = LoggerFactory.getLogger(MikuGetPayService.class);

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private MikuGetpayDOMapper mikuGetpayDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private MikuShareGetpayDOMapper mikuShareGetpayDOMapper;
	
	public WelinkVO reqGetPay(final Long profileId, final Integer getpayType, final String account, final String accountName) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
						mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(profileId);
						List<MikuAgencyShareAccountDO> mikuAgencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
						if(!mikuAgencyShareAccountDOList.isEmpty()){
							Date nowDate = new Date();
							ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
							/*List<Byte> returnGoodsStatus = new ArrayList<>();	//退货状态
							returnGoodsStatus.add(Constants.ReturnGoodsStatus.NORMAL.getStatusId());		//正常分润记录单
							returnGoodsStatus.add(Constants.ReturnGoodsStatus.REQ_EXAMINE.getStatusId());	//申请退货审核
							returnGoodsStatus.add(Constants.ReturnGoodsStatus.RETURN_ERROR.getStatusId());	//退货状态异常
							MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
							mikuSalesRecordDOExample.createCriteria().andAgencyIdEqualTo(profileId)
								//查询未提现状态 提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
								.andIsGetpayEqualTo(Constants.GetPayStatus.NOGETPAY.getStatusId())
								.andReturnStatusIn(returnGoodsStatus)	//状态状态(-1:删除;0:正常单;1:申请退货;2=退货中;3=退货完成;4=退货异常)
								.andTimeoutActionTimeGreaterThan(nowDate);		
							List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectByExample(mikuSalesRecordDOExample);*/
							
							Map<String,Object> paramMap = new HashMap<String, Object>();
							paramMap.put("profileId", profileId);			//代理商id
							//paramMap.put("isGetpay", Constants.GetPayStatus.NOGETPAY.getStatusId());	//未提现
							//paramMap.put("isCrowdfundRefundStatus", 0);	//是否众筹退款	//0=不用众筹退款;1=众筹退款的
							paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
							paramMap.put("isCanGetPay", 1);	//提现 (0=不可提现;1=可提现)
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
							List<Long> tradeIds = new ArrayList<Long>();	//订单tradeIds
							tradeIds.addAll(tradeIdSets);
							
							MikuAgencyShareAccountDO mikuAgencyShareAccountDO = mikuAgencyShareAccountDOList.get(0);
							Long noGetpayFee = mikuAgencyShareAccountDO.getNoGetpayFee();		//未提现金额
							Long getpayingFee = mikuAgencyShareAccountDO.getGetpayingFee();		//提现中申请金额
							//Long canReqFee = noGetpayFee - getpayingFee;						//可申请提现金额
							Long canReqFee = srShareFee;										//可申请提现金额
							/*if(getpayFeeL > canReqFee){		//超出可申请提现金额
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.REQ_PAY_OUT.getCode());
								welinkVO.setMsg(BizErrorEnum.REQ_PAY_OUT.getMsg());
								return JSON.toJSONString(welinkVO);
							}*/
							//if(canReqFee < 1 || srShareFee > canReqFee){		//超出可申请提现金额
							if(canReqFee < 1){		//超出可申请提现金额
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.REQ_PAY_OUT.getCode());
								welinkVO.setMsg(BizErrorEnum.REQ_PAY_OUT.getMsg());
								return welinkVO;
							}
							if(canReqFee <= 100){		//可提现金额必须大于一元
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.REQ_PAY_MIN.getCode());
								welinkVO.setMsg(BizErrorEnum.REQ_PAY_MIN.getMsg());
								return welinkVO;
							}
							MikuGetpayDO mikuGetpayDO = new MikuGetpayDO();
							mikuGetpayDO.setVersion(0L);
							mikuGetpayDO.setAgencyId(profileId);
							mikuGetpayDO.setGetpayFee(canReqFee);	//根据订单得出的可提现金额
							mikuGetpayDO.setGetpayType(getpayType);
							mikuGetpayDO.setGetpayAccount(account);
							/*if(1 == getpayType){	//1支付宝
								mikuGetpayDO.setGetpayAccount(account);
							}else if(2 == getpayType){	//2微信钱包
								mikuGetpayDO.setGetpayAccount(account);
							}else if(3 == getpayType){	//3银行卡
								mikuGetpayDO.setGetpayAccount(account);
							}*/
							mikuGetpayDO.setGetpayUserName(accountName);
							mikuGetpayDO.setApplyDate(nowDate);
							mikuGetpayDO.setDateCreated(nowDate);
							mikuGetpayDO.setLastUpdated(nowDate);
							//设置状态为提现中 提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
							mikuGetpayDO.setStatus(Constants.GetPayStatus.GETPAYING.getStatusId());	
							if(null != profileDO){
								mikuGetpayDO.setAgencyNickname(profileDO.getNickname());
								mikuGetpayDO.setAgencyMobile(profileDO.getMobile());
							}
							if(mikuGetpayDOMapper.insertSelective(mikuGetpayDO) > 0){		//插入提现申请记录
								MikuAgencyShareAccountDO mikuAgencyShareAccountDOUpdate = new MikuAgencyShareAccountDO();
								mikuAgencyShareAccountDOUpdate.setId(mikuAgencyShareAccountDO.getId());
								mikuAgencyShareAccountDOUpdate.setGetpayingFee(canReqFee +
										(null == mikuAgencyShareAccountDO.getGetpayingFee() ? 0L : mikuAgencyShareAccountDO.getGetpayingFee()));
								//更新代理帐户信息
								if(mikuAgencyShareAccountDOMapper.updateByPrimaryKeySelective(mikuAgencyShareAccountDOUpdate)<1){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
								}
								
								//插入分润提现关系表
								for(Long salesRecordId : salesRecordIds){
									MikuShareGetpayDO mikuShareGetpayDO = new MikuShareGetpayDO();
									mikuShareGetpayDO.setGetpayId(mikuGetpayDO.getId());
									mikuShareGetpayDO.setSalesRecordId(salesRecordId);
									mikuShareGetpayDO.setVersion(0L);
									mikuShareGetpayDO.setDateCreated(nowDate);
									mikuShareGetpayDO.setLastUpdated(nowDate);
									if(mikuShareGetpayDOMapper.insertSelective(mikuShareGetpayDO) < 1){
										transactionStatus.setRollbackOnly();
										welinkVO.setStatus(0);
										welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
										welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
										return welinkVO;
									}
								}
								
								List<Byte> notGetpay = new ArrayList<>();	//未提现和提现异常状态
								notGetpay.add(Constants.GetPayStatus.NOGETPAY.getStatusId());
								notGetpay.add(Constants.GetPayStatus.GETPAYABNORMAL.getStatusId());
								MikuSalesRecordDOExample mikuSalesRecordDOUpdateExample = new MikuSalesRecordDOExample();
								mikuSalesRecordDOUpdateExample.createCriteria().andAgencyIdEqualTo(profileId)
										.andIdIn(salesRecordIds)
										//查询未提现状态 提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
										.andIsGetpayIn(notGetpay);
										//.andIsGetpayEqualTo(Constants.GetPayStatus.NOGETPAY.getStatusId());
								
								MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
								//设置状态为提现中 提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
								mikuSalesRecordDO.setIsGetpay(Constants.GetPayStatus.GETPAYING.getStatusId());	
								//更新分润记录状态为提现中
								if(mikuSalesRecordDOMapper.updateByExampleSelective(mikuSalesRecordDO, mikuSalesRecordDOUpdateExample) < 1){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
								}
								
								welinkVO.setStatus(1);
								return welinkVO;
							}
						}
						welinkVO.setStatus(0);
						welinkVO.setCode(BizErrorEnum.NO_AGENCY.getCode());
						welinkVO.setMsg(BizErrorEnum.NO_AGENCY.getMsg());
						return welinkVO;
					}
				});
	}
	
	/**
	 * 
	 * reqGetPayAbnormal:(重新提现异常提现数据). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param mikuGetpayDO	//提现记录
	 * @return
	 */
	public WelinkVO reqGetPayAbnormal(final MikuGetpayDO mikuGetpayDO) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						try {
							if(mikuGetpayDOMapper.updateByPrimaryKeySelective(mikuGetpayDO) != 1){
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
								welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
								return welinkVO;
							}
							Map<String,Object> paramMap = new HashMap<String, Object>();
							paramMap.put("getpayId", mikuGetpayDO.getId());			//提现记录id
							List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectShareGetpay(paramMap);
							if(null != mikuSalesRecordDOList && !mikuSalesRecordDOList.isEmpty()){
								List<Long> salesRecordIds = new ArrayList<Long>();
								for(MikuSalesRecordDO mikuSalesRecordDO :mikuSalesRecordDOList){
									salesRecordIds.add(mikuSalesRecordDO.getId());
								}
								MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
								//设置状态为提现异常；提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
								mikuSalesRecordDO.setIsGetpay(Constants.GetPayStatus.GETPAYABNORMAL.getStatusId());
								MikuSalesRecordDOExample mikuSalesRecordDOExample = new MikuSalesRecordDOExample();
								mikuSalesRecordDOExample.createCriteria().andIdIn(salesRecordIds);
								//更新提现记录表下的分润记录数据为申请中状态
								if(mikuSalesRecordDOMapper.updateByExampleSelective(mikuSalesRecordDO, mikuSalesRecordDOExample) < 1){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
								}
							}
							welinkVO.setStatus(1);
							return welinkVO;
						} catch (Exception e) {
							welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
						}
						
					}
				});
	}
	

	@Override
	public void afterPropertiesSet() throws Exception {

		checkNotNull(mikuGetpayDOMapper);
		checkNotNull(mikuAgencyShareAccountDOMapper);
		checkNotNull(mikuSalesRecordDOMapper);
		checkNotNull(mikuShareGetpayDOMapper);
		checkNotNull(transactionManager);
		
		transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setName("userAgentcy-transaction");
		transactionTemplate
				.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate
				.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
		//transactionTemplate.setTimeout(3000);
	}

}
