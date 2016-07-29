/**
 * Project Name:welink-biz
 * File Name:MikuReturnGoodsService.java
 * Package Name:com.welink.biz.service
 * Date:2016年1月11日下午2:41:47
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import sun.misc.BASE64Decoder;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.LogisticsDO;
import com.welink.commons.domain.MikuFaceScoreExchangeDO;
import com.welink.commons.domain.MikuReturnGoodsDO;
import com.welink.commons.domain.MikuReturnGoodsDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.LogisticsDOMapper;
import com.welink.commons.persistence.MikuFaceScoreExchangeDOMapper;
import com.welink.commons.persistence.MikuReturnGoodsDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.utils.UpYunUtil;

/**
 * ClassName:MikuReturnGoodsService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月11日 下午2:41:47 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Service
public class MikuFaceScoreExchangeService implements InitializingBean {
	
	static Logger log = LoggerFactory.getLogger(MikuFaceScoreExchangeService.class);
	
	public static final String FACE_SCORE_EXCHANGE = "face_score_exchange";
	
	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuFaceScoreExchangeDOMapper mikuFaceScoreExchangeDOMapper;
	
	@Resource
    private MemcachedClient memcachedClient;
	
	/**
	 * 
	 * faceScoreExchange:(颜值兑换). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param profileId
	 * @param imgStr
	 * @return
	 */
	public WelinkVO faceScoreExchange(final Long profileId, final String imgStr) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						Map resultMap = new HashMap();
						welinkVO.setStatus(1);
						Date now = new Date();
						MikuFaceScoreExchangeDO mikuFaceScoreExchangeDO = null;
						//String imgUrl = profileId+""+System.currentTimeMillis()+".jpg";
						//String imgUrlDetail = UpYunUtil.UPYUN_URL+UpYunUtil.FACESCORE_DIR_ROOT+imgUrl;
						
						if(null != memcachedClient){
							Object object = memcachedClient.get(FACE_SCORE_EXCHANGE+profileId);
							if(null != object){
								ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
								if(null == profileDO){
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~用户不存在!");
									return welinkVO;
								}
								mikuFaceScoreExchangeDO = (MikuFaceScoreExchangeDO)object;
								mikuFaceScoreExchangeDO.setUserId(profileId);
								mikuFaceScoreExchangeDO.setUserName(profileDO.getNickname());
								mikuFaceScoreExchangeDO.setMobile(profileDO.getMobile());
								//mikuFaceScoreExchangeDO.setPicUrl(imgUrlDetail);
								mikuFaceScoreExchangeDO.setDateCreated(now);
								mikuFaceScoreExchangeDO.setLastUpdated(now);
								
								if(mikuFaceScoreExchangeDOMapper.insertSelective(mikuFaceScoreExchangeDO) < 1){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
								}
								/*BASE64Decoder decoder = new BASE64Decoder();
								try {
									// Base64解码
									byte[] b = decoder.decodeBuffer(imgStr);
									for (int i = 0; i < b.length; ++i) {
										if (b[i] < 0) {// 调整异常数据
											b[i] += 256;
										}
									}
									Boolean uploadFlag = UpYunUtil.writePicByBytes(b, UpYunUtil.FACESCORE_DIR_ROOT, imgUrl);
									if(!uploadFlag){
										transactionStatus.setRollbackOnly();
										welinkVO.setStatus(0);
										welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
										welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
										return welinkVO;
									}
								} catch (Exception e) {
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
									welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
									return welinkVO;
								}*/
							}else{
								welinkVO.setStatus(0);
								welinkVO.setMsg("请重新上传头像");
								return welinkVO;
							}
						}
						resultMap.put("do", mikuFaceScoreExchangeDO);
						welinkVO.setResult(resultMap);
						return welinkVO;
					}
				});
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {

		//checkNotNull(mikuGetpayDOMapper);
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

