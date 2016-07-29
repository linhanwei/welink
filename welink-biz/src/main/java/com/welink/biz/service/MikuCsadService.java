package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mysql.jdbc.Connection;
import com.welink.buy.utils.ParametersStringMaker;
import com.welink.commons.domain.MikuCsadGroupDO;
import com.welink.commons.domain.MikuCsadGroupDOExample;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.MikuCsadGroupDOMapper;
import com.welink.commons.vo.MikuCsadVO;
import com.welink.commons.vo.MikuGroupCsadsVO;

@Service
public class MikuCsadService implements InitializingBean {
	static Logger log = LoggerFactory.getLogger(MikuCrowdfundService.class);

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;
	
	@Resource
	private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
	private MikuCsadGroupDOMapper mikuCsadGroupDOMapper;
	
	/**
	 * 查询客服分组列表cache
	 */
	private LoadingCache<String, List<MikuGroupCsadsVO>> groupCsadVOListCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            /*.initialCapacity(10)	//设置缓存容器的初始容量为10
            .maximumSize(1000)	//设置缓存最大容量为1000，超过1000之后就会按照LRU最近虽少使用算法来移除缓存项*/
            .removalListener(new RemovalListener<String, List<MikuGroupCsadsVO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<MikuGroupCsadsVO>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<MikuGroupCsadsVO>>from(new Function<String, List<MikuGroupCsadsVO>>() {
                @Override
                public List<MikuGroupCsadsVO> apply(@Nullable String key) {
                    String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    return getMikuGroupCsadsVOList();
                }
            }));
	
	/**
	 * 查询客服分组列表cache
	 * @return
	 */
	public List<MikuGroupCsadsVO> getMikuGroupCsadsVOCacheList(){
		List<MikuGroupCsadsVO> mikuCrowdfundList = null;
		try {
			mikuCrowdfundList = groupCsadVOListCache.getUnchecked(ParametersStringMaker.parametersMake(null));
		} catch (Exception e) {
		}
		mikuCrowdfundList = getMikuGroupCsadsVOList();
		return mikuCrowdfundList;
	}
	
	/**
	 * 查询客服分组列表
	 * @param orderBy
	 * @param startRow
	 * @param size
	 * @param timeType
	 * @param status
	 * @return
	 */
	public List<MikuGroupCsadsVO> getMikuGroupCsadsVOList(){
		MikuCsadGroupDOExample mikuCsadGroupDOExample = new MikuCsadGroupDOExample();
		List<MikuCsadGroupDO> mikuCsadGroupDOs = mikuCsadGroupDOMapper.selectByExample(mikuCsadGroupDOExample);
		List<MikuGroupCsadsVO> mikuGroupCsadsVOList = null;
		mikuGroupCsadsVOList = new ArrayList<MikuGroupCsadsVO>();
		if(!mikuCsadGroupDOs.isEmpty()){
			List<MikuCsadVO> tempMikuCsadVOList = null;
			MikuGroupCsadsVO tempMikuGroupCsadsVO = null;
			for(MikuCsadGroupDO mcg : mikuCsadGroupDOs){
				tempMikuCsadVOList = new ArrayList<MikuCsadVO>();
				tempMikuGroupCsadsVO = new MikuGroupCsadsVO();
				tempMikuGroupCsadsVO.setGroupId(mcg.getId());
				tempMikuGroupCsadsVO.setGroupName(mcg.getCsadGroupName());
				mikuGroupCsadsVOList.add(tempMikuGroupCsadsVO);
				tempMikuGroupCsadsVO.setCsadVOList(tempMikuCsadVOList);
			}
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		//paramMap.put("userId", paramMap);
		List<MikuCsadVO> mikuCsadVOList = mikuCsadDOMapper.getGroupCsadList(paramMap);
		if(!mikuCsadVOList.isEmpty()){
			for(MikuCsadVO vo : mikuCsadVOList){
				for(MikuGroupCsadsVO mgc : mikuGroupCsadsVOList){
					if(null != vo && null != mgc && vo.getCsadGroupId().equals(mgc.getGroupId())){
						mgc.getCsadVOList().add(vo);
					}
				}
			}
		}
		return mikuGroupCsadsVOList;
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
	}
	
	
}
