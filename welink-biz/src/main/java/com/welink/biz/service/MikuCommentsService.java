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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.ParametersStringMaker;
import com.welink.commons.domain.MikuCommentsCountDO;
import com.welink.commons.domain.MikuCommentsCountDOExample;
import com.welink.commons.domain.MikuCommentsDO;
import com.welink.commons.domain.MikuSensitiveWordsDO;
import com.welink.commons.domain.MikuSensitiveWordsDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.MikuCommentsCountDOMapper;
import com.welink.commons.persistence.MikuCommentsDOMapper;
import com.welink.commons.persistence.MikuSensitiveWordsDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * 
 * ClassName: MikuCommentsService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2016年1月6日 下午6:03:23 <br/>
 *
 * @author LuoGuangChun
 * @version 
 * @since JDK 1.6
 */
@Service
public class MikuCommentsService implements InitializingBean {

	static Logger log = LoggerFactory.getLogger(MikuCommentsService.class);

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuSensitiveWordsDOMapper mikuSensitiveWordsDOMapper;
	
	@Resource
	private MikuCommentsDOMapper mikuCommentsDOMapper;
	
	@Resource
	private MikuCommentsCountDOMapper mikuCommentsCountDOMapper;
	
	@Resource
	private OrderMapper orderMapper;
	
	@Resource
	private TradeMapper tradeMapper;
	
	//敏感词Map缓存
	private LoadingCache<String, Map<String, List<String>>> sensitiveWordsMapCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            /*.initialCapacity(10)	//设置缓存容器的初始容量为10
            .maximumSize(1000)	//设置缓存最大容量为1000，超过1000之后就会按照LRU最近虽少使用算法来移除缓存项*/
            .removalListener(new RemovalListener<String, Map<String, List<String>>>() {
                @Override
                public void onRemoval(RemovalNotification<String, Map<String, List<String>>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, Map<String, List<String>>>from(new Function<String, Map<String, List<String>>>() {
                @Override
                public Map<String, List<String>> apply(String key) {
                	Map<String, List<String>> mikuSensitiveWordsAllMap = new HashMap<String, List<String>>();
                	MikuSensitiveWordsDOExample mikuSensitiveWordsDOExample = new MikuSensitiveWordsDOExample();
                	mikuSensitiveWordsDOExample.createCriteria().andStatusEqualTo((byte)1);
                	List<MikuSensitiveWordsDO> mikuSensitiveWordsDOList = mikuSensitiveWordsDOMapper.selectByExample(mikuSensitiveWordsDOExample);
                	if(null != mikuSensitiveWordsDOList && !mikuSensitiveWordsDOList.isEmpty()){
                		for(MikuSensitiveWordsDO mikuSensitiveWordsDO : mikuSensitiveWordsDOList){
                			if(null != mikuSensitiveWordsDO && null != mikuSensitiveWordsDO.getType()){
                				if(null == mikuSensitiveWordsAllMap.get(mikuSensitiveWordsDO.getType())){
                					if(null != mikuSensitiveWordsDO.getWord() && !"".equals(mikuSensitiveWordsDO.getWord().trim())){
                						List<String> wordsList = mikuSensitiveWordsAllMap.get(mikuSensitiveWordsDO.getType());
                						wordsList.add(mikuSensitiveWordsDO.getWord());
                						mikuSensitiveWordsAllMap.put(String.valueOf(mikuSensitiveWordsDO.getType()), wordsList);
                					}
                				}else{
                					List<String> wordsList = new ArrayList<String>();
                					if(null != mikuSensitiveWordsDO.getWord() && !"".equals(mikuSensitiveWordsDO.getWord().trim())){
                						wordsList.add(mikuSensitiveWordsDO.getWord());
                					}
                					mikuSensitiveWordsAllMap.put(String.valueOf(mikuSensitiveWordsDO.getType()), wordsList);
                				}
                			}
                		}
                	}else{
                		return null;
                	}
                	return mikuSensitiveWordsAllMap;
                }
            }));

	/**
	 * 
	 * getSensitiveWordsMapCache:(获取敏感词Map). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *	type 类型(1=需要审核;2=黑名单)
	 * @author LuoGuangChun
	 * @return
	 */
	public Map<String, List<String>> getSensitiveWordsMapCache(){
		Map<String, List<String>> mikuSensitiveWordsAllMap = null;
		try {
			mikuSensitiveWordsAllMap = sensitiveWordsMapCache.getUnchecked(ParametersStringMaker.parametersMake(null));
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(null == mikuSensitiveWordsAllMap){
			MikuSensitiveWordsDOExample mikuSensitiveWordsDOExample = new MikuSensitiveWordsDOExample();
        	mikuSensitiveWordsDOExample.createCriteria().andStatusEqualTo((byte)1);
        	List<MikuSensitiveWordsDO>mikuSensitiveWordsDOList = mikuSensitiveWordsDOMapper.selectByExample(mikuSensitiveWordsDOExample);
        	if(null != mikuSensitiveWordsDOList && !mikuSensitiveWordsDOList.isEmpty()){
        		for(MikuSensitiveWordsDO mikuSensitiveWordsDO : mikuSensitiveWordsDOList){
        			if(null != mikuSensitiveWordsDO && null != mikuSensitiveWordsDO.getType()){
        				if(null == mikuSensitiveWordsAllMap.get(mikuSensitiveWordsDO.getType())){
        					if(null != mikuSensitiveWordsDO.getWord() && !"".equals(mikuSensitiveWordsDO.getWord().trim())){
        						List<String> wordsList = mikuSensitiveWordsAllMap.get(mikuSensitiveWordsDO.getType());
        						wordsList.add(mikuSensitiveWordsDO.getWord());
        						mikuSensitiveWordsAllMap.put(String.valueOf(mikuSensitiveWordsDO.getType()), wordsList);
        					}
        				}else{
        					List<String> wordsList = new ArrayList<String>();
        					if(null != mikuSensitiveWordsDO.getWord() && !"".equals(mikuSensitiveWordsDO.getWord().trim())){
        						wordsList.add(mikuSensitiveWordsDO.getWord());
        					}
        					mikuSensitiveWordsAllMap.put(String.valueOf(mikuSensitiveWordsDO.getType()), wordsList);
        				}
        				
        			}
        		}
        	}
		}
		return mikuSensitiveWordsAllMap;
	}
	
	/**
	 * 
	 * addComments:(添加评论). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param profileDO
	 * @param commentsJson	[{\"buildingId\":1,\"star\":\"5\",\"content\":\"content\",\"picUrls\":\"picUrls\"}]
	 * @param tradeId
	 * @param buildingType	评论类型（如商品=1；众筹=2）
	 * @return
	 * @since JDK 1.6
	 */
	public WelinkVO addComments(final ProfileDO profileDO, final String commentsJson, final Long tradeId, final byte buildingType) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						Map resultMap = new HashMap();
						welinkVO.setStatus(1);
						Date now = new Date();
						
						List<MikuCommentsDO> mikuCommentsDOList = JSONArray.parseArray(commentsJson, MikuCommentsDO.class);
						if(null != mikuCommentsDOList && !mikuCommentsDOList.isEmpty()){
							boolean wordCheckFlag = false;	//敏感词需要审核Flag
							boolean wordBlacklistFlag = false;	//敏感词黑名单Flag
							
							List<Long> itemIds = new ArrayList<Long>();
							//获取敏感词
							Map<String, List<String>> sensitiveWordsMapCache = getSensitiveWordsMapCache();
							if(buildingType == 1){
								//查询是否存在此交易
								TradeExample tradeExampleCount = new TradeExample();
								tradeExampleCount.createCriteria().andTradeIdEqualTo(tradeId);
								List<Trade> tradeList = tradeMapper.selectByExample(tradeExampleCount);
								if(null == tradeList || tradeList.isEmpty()){
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~订单不能为空");
									return welinkVO;
								}
								List<Byte> tradeStatusList = new ArrayList<Byte>();
								tradeStatusList.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
								tradeStatusList.add(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());
								for(Trade trade: tradeList){
									if(!tradeStatusList.contains(trade.getStatus()) ||
											!trade.getCanRate().equals((byte)1) ||
											(tradeStatusList.contains(trade.getStatus())
													&& (trade.getCanRate().equals((byte)1) && !trade.getBuyerRate().equals((byte)0)))){
										welinkVO.setStatus(0);
										welinkVO.setMsg("啊哦~你已评价或不是未评价状态不能评价");
										return welinkVO;
									}
									
								}
								/*if(tradeMapper.countByExample(tradeExampleCount) < 1){
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~订单不能为空");
									return welinkVO;
								}*/
								
								//查询此交易下的所有订单
								OrderExample orderExample = new OrderExample();
								orderExample.createCriteria().andTradeIdEqualTo(tradeId);
								List<Order> orderList = orderMapper.selectByExample(orderExample);
								if(null == orderList || orderList.isEmpty()){
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~订单不能为空");
									return welinkVO;
								}
								for(Order order : orderList){
									itemIds.add(order.getArtificialId());
								}
							}
							
							for(MikuCommentsDO commentsDO : mikuCommentsDOList){
								commentsDO.setTradeId(tradeId);
								commentsDO.setBuildingType(buildingType);
								if(buildingType == 1){
									if(!itemIds.contains(commentsDO.getBuildingId())){
										welinkVO.setStatus(0);
										welinkVO.setMsg("啊哦~此商品不属于当前订单");
										return welinkVO;
									}
								}
								
								if(null == commentsDO.getBuildingId() || commentsDO.getBuildingId() < 0){
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~订单不能为空");
									return welinkVO;
								}else{
									//敏感词type 类型(1=需要审核;2=黑名单)
									if(null != commentsDO.getContent() && !"".equals(commentsDO.getContent().trim()) && null != sensitiveWordsMapCache){
										List<String> words = sensitiveWordsMapCache.get("1");
										List<String> words2 = sensitiveWordsMapCache.get("2");
										if(null != words && !words2.isEmpty()){	//敏感词type 类型(2=黑名单)
											String hasWordBlack = "";
											for(String word : words2){
												if(commentsDO.getContent().indexOf(word) > -1){
													hasWordBlack = word;
													wordBlacklistFlag = true;
													break;
												}
											}
											if(wordBlacklistFlag){	//若包含黑名单词语
												welinkVO.setStatus(0);
												welinkVO.setCode(BizErrorEnum.SENSITIVE_WORD.getCode());
												welinkVO.setMsg(BizErrorEnum.SENSITIVE_WORD.getMsg()+"<"+hasWordBlack+">");
												return welinkVO;
											}
										}
										if(null != words && !words.isEmpty()){	//敏感词type 类型(1=需要审核)
											for(String word : words){
												if(commentsDO.getContent().indexOf(word) > -1){
													wordCheckFlag = true;
													break;
												}
											}
										}
									}
								}
							}
							
							Byte buildingType = null, star = (byte)5;
							String content = "", picUrls = "";
							Long buildingId = -1L;
							for(MikuCommentsDO commentsDO : mikuCommentsDOList){
								buildingType = null == commentsDO.getBuildingType() ? (byte)1 : commentsDO.getBuildingType();
								if(null == commentsDO.getStar() || commentsDO.getStar() > (byte)5 || commentsDO.getStar() < (byte)1){
									star = (byte)5;
								}else{
									star = commentsDO.getStar();
								}
								content = commentsDO.getContent();
								picUrls = commentsDO.getPicUrls();
								buildingId = commentsDO.getBuildingId();
								
								MikuCommentsDO mikuCommentsDO = new MikuCommentsDO();
								mikuCommentsDO.setUserId(profileDO.getId());
								mikuCommentsDO.setTradeId(tradeId);
								mikuCommentsDO.setBuildingId(buildingId);
								mikuCommentsDO.setBuildingType(buildingType);
								if(StringUtils.isNotBlank(content)){
									mikuCommentsDO.setContent(content);
								}
								if((null == content || "".equals(content.trim()))
									&& null != picUrls && !"".equals(picUrls.trim())){
									mikuCommentsDO.setContent("图片");
								}else if(null != content && "".equals(content.trim())){
									mikuCommentsDO.setContent(null);
								}
								mikuCommentsDO.setUserName(profileDO.getNickname());
								mikuCommentsDO.setStar(star);
								if(wordCheckFlag){	//包含敏感词
									mikuCommentsDO.setStatus((byte)0);
								}else{
									mikuCommentsDO.setStatus((byte)1);
								}
								mikuCommentsDO.setPicUrls(picUrls);
								mikuCommentsDO.setDateCreated(now);
								mikuCommentsDO.setLastUpdated(now);
								//mikuCommentsService.addComments(profileDO, mikuCommentsDO);
								if(mikuCommentsDOMapper.insertSelective(mikuCommentsDO) < 1){	//插入评论
									transactionStatus.setRollbackOnly();
									log.error("插入评论失败");
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
									welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
									return welinkVO;
								}
								resultMap.put("commentsId", mikuCommentsDO.getId());
								resultMap.put("picUrls", mikuCommentsDO.getPicUrls());
								
								if(!wordCheckFlag){	//如果不包含敏感词，插入评论统计
									MikuCommentsCountDOExample mikuCommentsCountDOExample = new MikuCommentsCountDOExample();
									mikuCommentsCountDOExample.createCriteria().andBuildingIdEqualTo(buildingId).andBuildingTypeEqualTo(buildingType);
									List<MikuCommentsCountDO> mikuCommentsCountDOs = mikuCommentsCountDOMapper.selectByExample(mikuCommentsCountDOExample);
									MikuCommentsCountDO mikuCommentsCountDO = null;
									if(mikuCommentsCountDOs.isEmpty()){	//没有此评论统计记录，插入评论统计
										mikuCommentsCountDO = new MikuCommentsCountDO();
										mikuCommentsCountDO.setBuildingId(buildingId);
										mikuCommentsCountDO.setBuildingType(buildingType);
										mikuCommentsCountDO.setCount((null == mikuCommentsCountDO.getCount() ? 1 : mikuCommentsCountDO.getCount()+1));
										if(null != mikuCommentsDO.getContent() && !"".equals(mikuCommentsDO.getContent())){
											mikuCommentsCountDO.setMouthCount((null == mikuCommentsCountDO.getMouthCount() ? 1 : mikuCommentsCountDO.getMouthCount()+1));
										}
										mikuCommentsCountDO.setStarCount(0);
										mikuCommentsCountDO.setStar2Count(0);
										mikuCommentsCountDO.setStar3Count(0);
										mikuCommentsCountDO.setStar4Count(0);
										mikuCommentsCountDO.setStar5Count(0);
										if(star == (byte)1){
											mikuCommentsCountDO.setStarCount(1);
										}else if(star == (byte)2){
											mikuCommentsCountDO.setStar2Count(1);
										}else if(star == (byte)3){
											mikuCommentsCountDO.setStar3Count(1);
										}else if(star == (byte)4){
											mikuCommentsCountDO.setStar4Count(1);
										}else{
											mikuCommentsCountDO.setStar5Count(1);
										}
										if(star > 3){
											mikuCommentsCountDO.setHighOpinion(100);
										}else{
											mikuCommentsCountDO.setHighOpinion(0);
										}
										mikuCommentsCountDO.setDateCreated(now);
										mikuCommentsCountDO.setLastUpdated(now);
										if(mikuCommentsCountDOMapper.insertSelective(mikuCommentsCountDO) < 1){//插入评论统计
											transactionStatus.setRollbackOnly();
											log.error("插入评论统计失败");
											welinkVO.setStatus(0);
											welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
											welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
											return welinkVO;
										}
									}else{	//有此评论统计记录，更新评论统计
										mikuCommentsCountDO = mikuCommentsCountDOs.get(0);
										if(null != mikuCommentsCountDO && null != mikuCommentsCountDO.getId() && mikuCommentsCountDO.getId() > 0){
											int startCount = null == mikuCommentsCountDO.getStarCount() ? 0 : mikuCommentsCountDO.getStarCount();
											int start2Count = null == mikuCommentsCountDO.getStar2Count() ? 0 : mikuCommentsCountDO.getStar2Count();
											int start3Count = null == mikuCommentsCountDO.getStar3Count() ? 0 : mikuCommentsCountDO.getStar3Count();
											int start4Count = null == mikuCommentsCountDO.getStar4Count() ? 0 : mikuCommentsCountDO.getStar4Count();
											int start5Count = null == mikuCommentsCountDO.getStar5Count() ? 0 : mikuCommentsCountDO.getStar5Count();
											int count = null == mikuCommentsCountDO.getCount() ? 0 : mikuCommentsCountDO.getCount();
											
											if(null != mikuCommentsDO.getContent() && !"".equals(mikuCommentsDO.getContent())){
												mikuCommentsCountDO.setMouthCount((null == mikuCommentsCountDO.getMouthCount() ? 1 : mikuCommentsCountDO.getMouthCount()+1));
											}
											
											if(star == 1){
												mikuCommentsCountDO.setStarCount(startCount + 1);
												startCount += 1;
											}else if(star == 2){
												mikuCommentsCountDO.setStar2Count(start2Count + 1);
												start2Count += 1;
											}else if(star == 3){
												mikuCommentsCountDO.setStar3Count(start3Count + 1);
												start3Count += 1;
											}else if(star == 4){
												mikuCommentsCountDO.setStar4Count(start4Count + 1);
												start4Count += 1;
											}else{
												mikuCommentsCountDO.setStar5Count(start5Count + 1);
												start5Count += 1;
											}
											mikuCommentsCountDO.setCount(count + 1);
											mikuCommentsCountDO.setHighOpinion(100);	//好评率
											count = startCount + start2Count + start3Count + start4Count + start5Count;
											int goodCount = start4Count + start5Count;	//好评数
											if(count > 0){
												mikuCommentsCountDO.setHighOpinion((int)((double)goodCount / (double)count * 100));//好评率
											}else{
												mikuCommentsCountDO.setHighOpinion(0);	//好评率
											}
											mikuCommentsCountDO.setLastUpdated(now);
											if(mikuCommentsCountDOMapper.updateByPrimaryKeySelective(mikuCommentsCountDO) < 1){//更新评论统计
												transactionStatus.setRollbackOnly();
												log.error("更新评论统计失败");
												welinkVO.setStatus(0);
												welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
												welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
												return welinkVO;
											}
										}
									}
								}
							}
							
							if(buildingType == 1){
								TradeExample tradeExample = new TradeExample();
								tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
								Trade trade = new Trade();
								trade.setBuyerRate(Constants.RateType.RATED.getRateTypeId());
								if(tradeMapper.updateByExampleSelective(trade, tradeExample) < 1){
									transactionStatus.setRollbackOnly();
									log.error("更新交易为已评价失败");
									welinkVO.setStatus(0);
									welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
									welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
									return welinkVO;
								}
							}
							
						}else{
							welinkVO.setStatus(0);
							welinkVO.setMsg("啊哦~评论为空");
							return welinkVO;
						}
						
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
