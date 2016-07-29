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

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.MikuCrowdfundDetailVO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.ParametersStringMaker;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.domain.MikuCrowdfundDetailDO;
import com.welink.commons.domain.MikuCrowdfundDetailDOExample;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDetailDOMapper;
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
public class MikuCrowdfundService implements InitializingBean {

	static Logger log = LoggerFactory.getLogger(MikuCrowdfundService.class);

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	@Resource
	private ItemService itemService;
	
	@Resource
	private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
	
	@Resource
	private MikuCrowdfundDetailDOMapper mikuCrowdfundDetailDOMapper;
	
	/**
	 * 众筹列表缓存
	 */
	private LoadingCache<String, List<MikuCrowdfundDO>> crowdfundListCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            /*.initialCapacity(10)	//设置缓存容器的初始容量为10
            .maximumSize(1000)	//设置缓存最大容量为1000，超过1000之后就会按照LRU最近虽少使用算法来移除缓存项*/
            .removalListener(new RemovalListener<String, List<MikuCrowdfundDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<MikuCrowdfundDO>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<MikuCrowdfundDO>>from(new Function<String, List<MikuCrowdfundDO>>() {
                @Override
                public List<MikuCrowdfundDO> apply(@Nullable String key) {
                	Date nowDate = new Date();
                    String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    String orderBy = String.valueOf(params[0]);
                    Integer startRow = Integer.valueOf(params[1]);
                    Integer size = Integer.valueOf(params[2]);
                    Integer timeType = Integer.valueOf(params[3]);
                    Byte status = Byte.valueOf(params[4]);
                    MikuCrowdfundDOExample mikuCrowdfundDOExample = new MikuCrowdfundDOExample();
                    Criteria createCriteria = mikuCrowdfundDOExample.createCriteria();
                    createCriteria.andApproveStatusEqualTo((byte)1);
        			//createCriteria.andStatusEqualTo(Constants.CrowdfundStatus.NORMAL.getStatusId());
                    if(status >= Constants.CrowdfundStatus.NORMAL.getStatusId()){
        				createCriteria.andStatusEqualTo(status);
        			}else{
        				createCriteria.andStatusNotEqualTo(Constants.CrowdfundStatus.DEL.getStatusId());
        			}
             		if(timeType.equals(1)){	//众筹中的
             			createCriteria.andStartTimeLessThan(nowDate).andEndTimeGreaterThan(nowDate);
            		}else if(timeType.equals(2)){	//未开始的众筹
            			createCriteria.andStartTimeGreaterThan(nowDate);
            		}else if(timeType.equals(3)){	//已结束的众筹
            			createCriteria.andEndTimeLessThan(nowDate);
            		}else if(timeType.equals(4)){	//未结束的众筹
            			createCriteria.andEndTimeGreaterThan(nowDate);
            		}else{	//上架的全部的众筹
            			
            		}
            		mikuCrowdfundDOExample.setOffset(startRow);
            		mikuCrowdfundDOExample.setLimit(size);
            		//mikuCrowdfundDOExample.setOrderByClause("date_created desc");
            		mikuCrowdfundDOExample.setOrderByClause(orderBy);
            		return mikuCrowdfundDOMapper.selectByExample(mikuCrowdfundDOExample);
                }
            }));
	
	/**
	 * 获取众筹详情
	 */
	private LoadingCache<String, Map> crowdfundInfoCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            /*.initialCapacity(10)	//设置缓存容器的初始容量为10
            .maximumSize(1000)	//设置缓存最大容量为1000，超过1000之后就会按照LRU最近虽少使用算法来移除缓存项*/
            .removalListener(new RemovalListener<String, Map>() {
                @Override
                public void onRemoval(RemovalNotification<String, Map> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, Map>from(new Function<String, Map>() {
                @Override
                public Map apply(@Nullable String key) {
                	String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    Long crowdfundId = Long.valueOf(params[0]);
                	
                	Map resultMap = new HashMap();
            		
            		MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(crowdfundId);
            		if(null != mikuCrowdfundDO){
            			MikuCrowdfundDetailDOExample mikuCrowdfundDetailDOExample = new MikuCrowdfundDetailDOExample();
            			mikuCrowdfundDetailDOExample.createCriteria().andCrowdfundIdEqualTo(crowdfundId).andApproveStatusEqualTo(Constants.ApproveStatus.ON_SALE.getApproveStatusId());
            			List<MikuCrowdfundDetailDO> mikuCrowdfundDetailDOList = mikuCrowdfundDetailDOMapper.selectByExample(mikuCrowdfundDetailDOExample);
            			MikuCrowdfundDetailVO mikuCrowdfundDetailVO = null;
            			List<MikuCrowdfundDetailVO> mikuCrowdfundDetailVOList = new ArrayList<MikuCrowdfundDetailVO>();
            			if(null == mikuCrowdfundDetailDOList || mikuCrowdfundDetailDOList.isEmpty()){
            				return null;
            			}
            			List<Long> itemPriceList = new ArrayList<Long>();
            			Integer totalNum = 0;
            			for(MikuCrowdfundDetailDO mikuCrowdfundDetailDO : mikuCrowdfundDetailDOList){
            				mikuCrowdfundDetailVO = new MikuCrowdfundDetailVO();
            				BeanUtils.copyProperties(mikuCrowdfundDetailDO, mikuCrowdfundDetailVO);
            				//Item item = itemMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getItemId());
            				ItemExample iiExample = new ItemExample();
            	            iiExample.createCriteria().andIdEqualTo(mikuCrowdfundDetailDO.getItemId());//查询商品资料
            	            List<Item> tmpItems = itemMapper.selectByExample(iiExample);
            	            List<ItemViewDO> tmpItemViews = itemService.combineItemTags(tmpItems);

            	            for(Item item : tmpItems){
            	            	itemPriceList.add(item.getPrice());
            	            	totalNum += (null == item.getNum() ? 0 : item.getNum());
            	            }
            	            
            	            if(null != tmpItemViews && !tmpItemViews.isEmpty()){
            	            	mikuCrowdfundDetailVO.setItemViewDO(tmpItemViews.get(0));
            	            }
            				mikuCrowdfundDetailVOList.add(mikuCrowdfundDetailVO);
            			}
            			Long minItemPrice = 999999999999L, maxItemPrice = 0L;
            			for(Long itemPrice : itemPriceList){
            				if(itemPrice < minItemPrice){
            					minItemPrice = itemPrice;
            				}
            				if(itemPrice > maxItemPrice){
            					maxItemPrice = itemPrice;
            				}
            			}
            			resultMap.put("minItemPrice", minItemPrice);
            			resultMap.put("maxItemPrice", maxItemPrice);
            			resultMap.put("totalNum", totalNum);
            			resultMap.put("crowdfundDO", mikuCrowdfundDO);
            			resultMap.put("detailVOList", mikuCrowdfundDetailVOList);
            		}else{
            			return null;
            		}
            		return resultMap;
                }
            }));
	
	/**
	 * 
	 * getCrowdfundList:(查询众筹列表). <br/>
	 *
	 * @author LuoGuangChun
	 * @param orderBy
	 * @param startRow
	 * @param size
	 * @param timeType (1=众筹中的; 2=未开始的众筹; 3=已结束的众筹; 4=未结束的众筹; 5=上架的全部的众筹;)
	 * @param status 状态(-1=无效;0=正常;1=成功;2=失败) 默认-2查询不是无效的全部
	 * @return
	 */
	public List<MikuCrowdfundDO> getCrowdfundList(String orderBy, Integer startRow, Integer size, Integer timeType, Byte status){
		if(null == timeType){
			timeType = 1;	//众筹中的
		}
		List<MikuCrowdfundDO> mikuCrowdfundList = null;
		Date nowDate = new Date();
		try {
			mikuCrowdfundList = crowdfundListCache.getUnchecked(ParametersStringMaker.parametersMake(orderBy, startRow, size, timeType, status));
		} catch (Exception e) {
		}
		if (null == mikuCrowdfundList || mikuCrowdfundList.isEmpty()) {
			MikuCrowdfundDOExample mikuCrowdfundDOExample = new MikuCrowdfundDOExample();
			Criteria createCriteria = mikuCrowdfundDOExample.createCriteria();
			//createCriteria.andStatusEqualTo(Constants.CrowdfundStatus.NORMAL.getStatusId());
			createCriteria.andApproveStatusEqualTo((byte)1);
			if(status >= Constants.CrowdfundStatus.NORMAL.getStatusId()){
				createCriteria.andStatusEqualTo(status);
			}else{
				createCriteria.andStatusNotEqualTo(Constants.CrowdfundStatus.DEL.getStatusId());
			}
     		if(timeType.equals(1)){	//众筹中的
     			createCriteria.andStartTimeLessThan(nowDate).andEndTimeGreaterThan(nowDate);
    		}else if(timeType.equals(2)){	//未开始的众筹
    			createCriteria.andStartTimeGreaterThan(nowDate);
    		}else if(timeType.equals(3)){	//已结束的众筹
    			createCriteria.andEndTimeLessThan(nowDate);
    		}else if(timeType.equals(4)){	//未结束的众筹
    			createCriteria.andEndTimeGreaterThan(nowDate);
    		}else{	//上架的全部的众筹
    			
    		}
     		mikuCrowdfundDOExample.setOrderByClause(orderBy);
     		mikuCrowdfundDOExample.setOffset(startRow);
     		mikuCrowdfundDOExample.setLimit(size);
     		//mikuCrowdfundDOExample.setOrderByClause("date_created desc");
     		return mikuCrowdfundDOMapper.selectByExample(mikuCrowdfundDOExample);
        }
		return mikuCrowdfundList;
	}
	
	/**
	 * 
	 * getCrowdfundInfo:(获取众筹详情). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param crowdfundId
	 * @return
	 */
	public Map getCrowdfundInfo(Long crowdfundId){
		Map resultMap = null;
		try {
			resultMap = crowdfundInfoCache.getUnchecked(ParametersStringMaker.parametersMake(crowdfundId));
		} catch (Exception e) {
		}
		if (null == resultMap || (null != resultMap && null == resultMap.get("crowdfundDO"))
				|| (null != resultMap && null == resultMap.get("detailVOList"))) {
			resultMap = new HashMap();
    		
    		MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(crowdfundId);
    		/*if(null != mikuCrowdfundDO && null != mikuCrowdfundDO.getStatus() && 
    				mikuCrowdfundDO.getStatus().equals(Constants.CrowdfundStatus.NORMAL.getStatusId())){*/
    		if(null != mikuCrowdfundDO){
    			MikuCrowdfundDetailDOExample mikuCrowdfundDetailDOExample = new MikuCrowdfundDetailDOExample();
    			mikuCrowdfundDetailDOExample.createCriteria().andCrowdfundIdEqualTo(crowdfundId).andApproveStatusEqualTo(Constants.ApproveStatus.ON_SALE.getApproveStatusId());
    			List<MikuCrowdfundDetailDO> mikuCrowdfundDetailDOList = mikuCrowdfundDetailDOMapper.selectByExample(mikuCrowdfundDetailDOExample);
    			MikuCrowdfundDetailVO mikuCrowdfundDetailVO = null;
    			List<MikuCrowdfundDetailVO> mikuCrowdfundDetailVOList = new ArrayList<MikuCrowdfundDetailVO>();
    			List<Long> itemPriceList = new ArrayList<Long>();
    			Integer totalNum = 0;
    			for(MikuCrowdfundDetailDO mikuCrowdfundDetailDO : mikuCrowdfundDetailDOList){
    				mikuCrowdfundDetailVO = new MikuCrowdfundDetailVO();
    				BeanUtils.copyProperties(mikuCrowdfundDetailDO, mikuCrowdfundDetailVO);
    				//Item item = itemMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getItemId());
    				ItemExample iiExample = new ItemExample();
    	            iiExample.createCriteria().andIdEqualTo(mikuCrowdfundDetailDO.getItemId());//查询商品资料
    	            List<Item> tmpItems = itemMapper.selectByExample(iiExample);
    	            List<ItemViewDO> tmpItemViews = itemService.combineItemTags(tmpItems);

    	            for(Item item : tmpItems){
    	            	itemPriceList.add(item.getPrice());
    	            	totalNum += (null == item.getNum() ? 0 : item.getNum());
    	            }
    	            
    	            if(null != tmpItemViews && !tmpItemViews.isEmpty()){
    	            	mikuCrowdfundDetailVO.setItemViewDO(tmpItemViews.get(0));
    	            }
    				mikuCrowdfundDetailVOList.add(mikuCrowdfundDetailVO);
    			}
    			Long minItemPrice = 999999999999L, maxItemPrice = 0L;
    			for(Long itemPrice : itemPriceList){
    				if(itemPrice < minItemPrice){
    					minItemPrice = itemPrice;
    				}
    				if(itemPrice > maxItemPrice){
    					maxItemPrice = itemPrice;
    				}
    			}
    			resultMap.put("minItemPrice", minItemPrice);
    			resultMap.put("maxItemPrice", maxItemPrice);
    			resultMap.put("totalNum", totalNum);
    			resultMap.put("crowdfundDO", mikuCrowdfundDO);
    			resultMap.put("detailVOList", mikuCrowdfundDetailVOList);
    		}
		}
		return resultMap;
	}
	
	
	public WelinkVO test(final Long profileId, final Integer getpayType, final String account, final String accountName) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						welinkVO.setStatus(0);
						
						
						
						welinkVO.setCode(BizErrorEnum.NO_AGENCY.getCode());
						welinkVO.setMsg(BizErrorEnum.NO_AGENCY.getMsg());
						return welinkVO;
					}
				});
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {

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
