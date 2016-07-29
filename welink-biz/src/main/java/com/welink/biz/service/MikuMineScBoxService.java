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

import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuMineCourseDO;
import com.welink.commons.domain.MikuMineCourseStepDO;
import com.welink.commons.domain.MikuMineDetectReport;
import com.welink.commons.domain.MikuMineExpertDbDO;
import com.welink.commons.domain.MikuMineExpertDbDOExample;
import com.welink.commons.domain.MikuMineQuestionnaireRecordsDO;
import com.welink.commons.domain.MikuMineScBoxDO;
import com.welink.commons.domain.MikuMineScProblemItemDO;
import com.welink.commons.domain.MikuMineScProblemItemDOExample;
import com.welink.commons.domain.MikuMineScProductDO;
import com.welink.commons.domain.MikuMineScProductDOExample;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuMineCourseDOMapper;
import com.welink.commons.persistence.MikuMineCourseStepDOMapper;
import com.welink.commons.persistence.MikuMineDetectReportMapper;
import com.welink.commons.persistence.MikuMineExpertDbDOMapper;
import com.welink.commons.persistence.MikuMineQuestionnaireRecordsDOMapper;
import com.welink.commons.persistence.MikuMineScBoxDOMapper;
import com.welink.commons.persistence.MikuMineScProblemItemDOMapper;
import com.welink.commons.persistence.MikuMineScProductDOMapper;

/**
 * 
 * @author miku03
 *
 */
@Service
public class MikuMineScBoxService implements InitializingBean {

	static Logger log = LoggerFactory.getLogger(MikuMineScBoxService.class);

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private MikuMineDetectReportMapper mikuMineDetectReportMapper;
	
	@Resource
	private MikuMineExpertDbDOMapper mikuMineExpertDbDOMapper;
	
	@Resource
	private MikuMineCourseDOMapper mikuMineCourseDOMapper;
	
	@Resource
	private MikuMineCourseStepDOMapper mikuMineCourseStepDOMapper;
	
	@Resource
	private MikuMineScProductDOMapper mikuMineScProductDOMapper;
	
	@Resource
	private MikuMineScBoxDOMapper mikuMineScBoxDOMapper;
	
	@Resource
	private MikuMineQuestionnaireRecordsDOMapper mikuMineQuestionnaireRecordsDOMapper;
	
	@Resource
	private MikuMineScProblemItemDOMapper mikuMineScProblemItemDOMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	/**
	 * 生成盒子
	 * @param mikuMineDetectReport
	 * @return
	 */
	public WelinkVO createMineScBox(final MikuMineDetectReport mikuMineDetectReport) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						
						//查询问卷
				        MikuMineQuestionnaireRecordsDO mikuMineQuestionnaireRecordsDO 
					    	= mikuMineQuestionnaireRecordsDOMapper.selectByPrimaryKey(mikuMineDetectReport.getQuestionnaireRecordsId());
					    if(null == mikuMineQuestionnaireRecordsDO){
					    	welinkVO.setStatus(0);
							welinkVO.setMsg("啊哦~问卷不能为空~");
							return welinkVO;
					    }
				    
					    //找出问题ids
				        String scProblemIds = mikuMineQuestionnaireRecordsDO.getScProblemIds();
				        List<Long> scProblemIdList = new ArrayList<Long>();
				        if(!StringUtils.isBlank(scProblemIds)){
				        	String[] scProblemIdsStr = scProblemIds.split(";");
				        	if(null != scProblemIdsStr && scProblemIdsStr.length > 0){
				        		for(String str : scProblemIdsStr){
				        			scProblemIdList.add(Long.valueOf(str));
				        		}
				        	}
				        }
				        
				        String scProblemShortNames = "";	//问题名（用于做盒子和课程名称）
				        MikuMineScProblemItemDOExample mikuMineScProblemItemDOExample = new MikuMineScProblemItemDOExample();
				        mikuMineScProblemItemDOExample.createCriteria().andIdIn(scProblemIdList);
				        List<MikuMineScProblemItemDO> mikuMineScProblemItemDOList = mikuMineScProblemItemDOMapper.selectByExample(mikuMineScProblemItemDOExample);
				        if(!mikuMineScProblemItemDOList.isEmpty()){
				        	for(MikuMineScProblemItemDO mikuMineScProblemItemDO : mikuMineScProblemItemDOList){
				        		scProblemShortNames += null == mikuMineScProblemItemDO.getScProblemShortName() ? 
				        				"" : (mikuMineScProblemItemDO.getScProblemShortName()+"+");
				        	}
				        	if(scProblemShortNames.endsWith("+")){
				        		scProblemShortNames = scProblemShortNames.substring(0, scProblemShortNames.length()-1);
				        	}
				        }
						
				        //多个问题的经验List
				        List<List<MikuMineExpertDbDO>> mineExpertDbByProblemIdList = new ArrayList<List<MikuMineExpertDbDO>>();
						if(null != scProblemIdList && !scProblemIdList.isEmpty()){
							List<Byte> ageRegionList = new ArrayList<Byte>();
							ageRegionList.add((byte)0);	//不限
							if(mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)1)){
								ageRegionList.add((byte)1);	//25岁以下
							}
							if(mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)2) ||
									mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)3) ||
									mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)4) ||
									mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)5)){
								ageRegionList.add((byte)2);	//26岁以上
							}
							if(mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)1) ||
									mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)2)){
								ageRegionList.add((byte)3);	//32岁以下
							}
							if(mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)3) ||
									mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)4) ||
									mikuMineQuestionnaireRecordsDO.getAgeRegion().equals((byte)5)){
								ageRegionList.add((byte)4);	//33岁以上
							}
							for(Long scProblemId : scProblemIdList){
								//查询问题经验
								MikuMineExpertDbDOExample mikuMineExpertDbDOExample = new MikuMineExpertDbDOExample();
								mikuMineExpertDbDOExample.createCriteria().andScProblemIdEqualTo(scProblemId)
									.andEnvAreaEqualTo(mikuMineQuestionnaireRecordsDO.getEnvArea())
									.andEnvAgeRegionIn(ageRegionList)
									.andEnvSkinTypeEqualTo(mikuMineQuestionnaireRecordsDO.getSkinType())
									.andEnvSeasonsLike("%"+String.valueOf(mikuMineQuestionnaireRecordsDO.getSeason())+"%");
									
								mikuMineExpertDbDOExample.setOffset(0);
								mikuMineExpertDbDOExample.setLimit(1);
								List<MikuMineExpertDbDO> mikuMineExpertDbList = mikuMineExpertDbDOMapper.selectByExample(mikuMineExpertDbDOExample);
								if(null != mikuMineExpertDbList && !mikuMineExpertDbList.isEmpty()){
									MikuMineExpertDbDO mikuMineExpertDbDO = mikuMineExpertDbList.get(0);
									
									//通过第一个问题经验查询出问题经验库列表
									MikuMineExpertDbDOExample mikuMineExpertDbDOExample2 = new MikuMineExpertDbDOExample();
									mikuMineExpertDbDOExample2.createCriteria().andScProblemIdEqualTo(scProblemId)
										.andEnvAreaEqualTo(mikuMineQuestionnaireRecordsDO.getEnvArea())
										.andScThinkingIdEqualTo(mikuMineExpertDbDO.getScThinkingId())
										.andEnvSkinTypeEqualTo(mikuMineExpertDbDO.getEnvSkinType())
										.andEnvSeasonsLike("%"+String.valueOf(mikuMineQuestionnaireRecordsDO.getSeason())+"%");
										//.andEnvSkinTypeEqualTo(mikuMineQuestionnaireRecordsDO.getSkinType());
									//需待加条件
									List<MikuMineExpertDbDO> mikuMineExpertDbList2 = mikuMineExpertDbDOMapper.selectByExample(mikuMineExpertDbDOExample2);
									if(null != mikuMineExpertDbList2 && !mikuMineExpertDbList2.isEmpty()){
										mineExpertDbByProblemIdList.add(mikuMineExpertDbList2);
									}
								}else{
									
								}
							}
						}
						
						if(null != mineExpertDbByProblemIdList && !mineExpertDbByProblemIdList.isEmpty()){
							//已插入的课程步骤列表
							List<MikuMineCourseStepDO> insertedMineCourseStepDOList = new ArrayList<MikuMineCourseStepDO>();
							
							//已插入步骤的专家经验id数组
							List<Long> insertedCourseStepExpertDbIds  = new ArrayList<Long>();
							
							//插入课程
							MikuMineCourseDO mikuMineCourseDO = new MikuMineCourseDO();
							if(!insertMineCourseDO(mikuMineCourseDO, mikuMineDetectReport, scProblemShortNames, transactionStatus)){
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setMsg("啊哦~插入课程失败~");
								return welinkVO;
							}
							
							int choosedProblemIndex = -1; 	//所选中的问题经验在列表中序号
							int size = 0;	//列表中的最大长度
							//for(List<MikuMineExpertDbDO> list : mineExpertDbByProblemIdList){
							for(int i = 0; i < mineExpertDbByProblemIdList.size(); i++){
								if(mineExpertDbByProblemIdList.get(i).size() > size){
									choosedProblemIndex = i;						//所选中的问题经验在列表中序号
									size = mineExpertDbByProblemIdList.get(i).size();	//列表中的最大长度
								}
							}
							//选中的问题经验s
							List<MikuMineExpertDbDO> choosedMineExpertDbDOList = mineExpertDbByProblemIdList.get(choosedProblemIndex);
							int minFunctionStepOrder = 100, maxFunctionStepOrder = -1;	//最小和最大功能性步骤号
							for(MikuMineExpertDbDO mikuMineExpertDbDO : choosedMineExpertDbDOList){
								if(null != mikuMineExpertDbDO.getStepOrder()){
									if(mikuMineExpertDbDO.getStepOrder() < minFunctionStepOrder){
										//寻找出最小功能性步骤号
										minFunctionStepOrder = mikuMineExpertDbDO.getStepOrder();
									}
									if(mikuMineExpertDbDO.getStepOrder() > maxFunctionStepOrder){
										//寻找出最大功能性步骤号
										maxFunctionStepOrder = mikuMineExpertDbDO.getStepOrder();
									}
								}
								MikuMineCourseStepDO mikuMineCourseStepDO = new MikuMineCourseStepDO();
								//给选中的问题插入课程步骤
								if(!insertMikuMineCourseStepDO(mikuMineCourseStepDO, mikuMineCourseDO.getId(), 
										mikuMineExpertDbDO.getStepOrder(), mikuMineExpertDbDO, transactionStatus)){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~插入课程步骤失败~");
									return welinkVO;
								}else{
									//加入到已插入的课程步骤列表
									insertedMineCourseStepDOList.add(mikuMineCourseStepDO);
									//已插入步骤的专家经验id数组
									insertedCourseStepExpertDbIds.add(mikuMineExpertDbDO.getId());
								}
							}
							
							//给其它问题插入课程步骤
							for(int i = 0; i < mineExpertDbByProblemIdList.size(); i++){
								if(i == choosedProblemIndex){
									continue;
								}
								List<MikuMineExpertDbDO> mineExpertDbList = mineExpertDbByProblemIdList.get(i); 
								for(MikuMineExpertDbDO mikuMineExpertDbDO : mineExpertDbList){
									boolean suredSetpFlag = false;	//是否确定步骤；
									Integer setpOrder = null;		//步骤号
									if(null != mikuMineExpertDbDO.getStepType() && mikuMineExpertDbDO.getStepType() == 2){
										//如果是功能性步骤
										for(MikuMineExpertDbDO choosedMineExpertDbDO : choosedMineExpertDbDOList){
											//循环选中的问题经验，并找出与当前问题经验步骤相等的值
											if(null != choosedMineExpertDbDO.getStepType() 
													&& choosedMineExpertDbDO.getStepType() == 2
													&& null != choosedMineExpertDbDO.getStepOrder()
													&& choosedMineExpertDbDO.getStepOrder().equals(mikuMineExpertDbDO.getStepType())){
												suredSetpFlag = true;
												setpOrder = mikuMineExpertDbDO.getStepOrder();
											}
										}
										if(suredSetpFlag){
											continue;	//如果已经选中步骤则不执行下面的语句，并继续执行
										}
										setpOrder = (null == mikuMineExpertDbDO.getStepOrder() ? 0 : mikuMineExpertDbDO.getStepOrder());
										if(setpOrder >= maxFunctionStepOrder){
											setpOrder = maxFunctionStepOrder;
										}else if(setpOrder < minFunctionStepOrder){
											setpOrder = minFunctionStepOrder;
										}else{
											setpOrder = maxFunctionStepOrder;
										}
										MikuMineCourseStepDO mikuMineCourseStepDO = new MikuMineCourseStepDO();
										if(!insertMikuMineCourseStepDO(mikuMineCourseStepDO, mikuMineCourseDO.getId(), 
												mikuMineExpertDbDO.getStepOrder(), mikuMineExpertDbDO, transactionStatus)){
											transactionStatus.setRollbackOnly();
											welinkVO.setStatus(0);
											welinkVO.setMsg("啊哦~插入课程步骤失败~");
											return welinkVO;
										}else{
											//加入到已插入的课程步骤列表
											insertedMineCourseStepDOList.add(mikuMineCourseStepDO);
											//已插入步骤的专家经验id数组
											insertedCourseStepExpertDbIds.add(mikuMineExpertDbDO.getId());
										}
									}
								}
							}
							
							//找出私人定制产品
							List<Long> prodIdList = new ArrayList<Long>();	//产品id数组
							if(insertedMineCourseStepDOList.isEmpty()){
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setMsg("啊哦~无课程步骤~");
								return welinkVO;
							}
							//循环课程步骤列表
							Integer totalVideoTime = 0;	//总视频时长
							for(MikuMineCourseStepDO mikuMineCourseStepDO : insertedMineCourseStepDOList){
								if(null != mikuMineCourseStepDO && null != mikuMineCourseStepDO.getProdId()){
									prodIdList.add(mikuMineCourseStepDO.getProdId());
								}
								totalVideoTime += (null == mikuMineCourseStepDO.getVideoTime() ? 0 : mikuMineCourseStepDO.getVideoTime());
							}
							//更新课程信息
							if(null != mikuMineCourseDO && null != mikuMineCourseDO.getId()){
								mikuMineCourseDO.setCourseTime(totalVideoTime);
								mikuMineCourseDO.setCourseSteps(insertedMineCourseStepDOList.size());
								if(mikuMineCourseDOMapper.updateByPrimaryKeySelective(mikuMineCourseDO) < 1){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~更新课程信息失败~");
									return welinkVO;
								}
							}
							if(!prodIdList.isEmpty()){
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setMsg("啊哦~无私人定制产品~");
								return welinkVO;
							}
							
							//查询出产品
							MikuMineScProductDOExample mikuMineScProductDOExample = new MikuMineScProductDOExample();
							mikuMineScProductDOExample.createCriteria().andIdIn(prodIdList);
							List<MikuMineScProductDO> mikuMineScProductDOList 
								= mikuMineScProductDOMapper.selectByExample(mikuMineScProductDOExample); 
							if(!mikuMineScProductDOList.isEmpty()){
								Long allProdRetailPrice = 0L;
								for(MikuMineScProductDO mikuMineScProductDO : mikuMineScProductDOList){
									allProdRetailPrice += (null == mikuMineScProductDO.getProdRetailPrice() ? 
											0L : mikuMineScProductDO.getProdRetailPrice());
								}
								
								ItemExample itemExample = new ItemExample();
								itemExample.createCriteria().andTypeEqualTo(Constants.TradeType.dz_type.getTradeTypeId())
									.andApproveStatusEqualTo((byte)1)
									.andBaseItemIdIsNotNull();
								itemExample.setOffset(0);
								itemExample.setLimit(1);
								List<Item> itemList = itemMapper.selectByExample(itemExample);
								Long itemId = null;
								if(!itemList.isEmpty()){
									itemId = itemList.get(0).getId();
								}
								if(null == itemId){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~无私人定制产品~");
									return welinkVO;
								}
								
								MikuMineScBoxDO mikuMineScBoxDO = new MikuMineScBoxDO();
								String insertedCourseStepExpertDbIdsStr 
									= StringUtils.join(insertedCourseStepExpertDbIds.toArray(),";");
								String prodIdListStr = StringUtils.join(prodIdList.toArray(),";");
								mikuMineScBoxDO.setBoxName(scProblemShortNames); 	//盒子名
								mikuMineScBoxDO.setUserId(mikuMineDetectReport.getUserId()); //盒子所属用户
								mikuMineScBoxDO.setItemId(itemId);
								//插入盒子
								if(insertMikuMineScBoxDO(mikuMineScBoxDO, allProdRetailPrice, mikuMineDetectReport.getId(), 
										mikuMineCourseDO.getId(), insertedCourseStepExpertDbIdsStr, prodIdListStr, transactionStatus)){
									transactionStatus.setRollbackOnly();
									welinkVO.setStatus(0);
									welinkVO.setMsg("啊哦~生成盒子失败~");
									return welinkVO;
								}else{
									welinkVO.setStatus(1);
									Map resultMap = new HashMap();
									//resultMap.put("mineScBoxDO", mikuMineScBoxDO);
									resultMap.put("mineScBoxId", mikuMineScBoxDO.getId());
									welinkVO.setResult(resultMap);
									return welinkVO;
								}
								
							}else{
								transactionStatus.setRollbackOnly();
								welinkVO.setStatus(0);
								welinkVO.setMsg("啊哦~无私人定制产品~");
								return welinkVO;
							}
							
						}
						
						welinkVO.setStatus(0);
						welinkVO.setMsg(BizErrorEnum.NO_AGENCY.getMsg());
						return welinkVO;
					}
				});
	}
	
	/**
	 * 插入课程
	 * @param mikuMineCourseDO
	 * @param mikuMineDetectReport
	 * @return
	 */
	public boolean insertMineCourseDO(MikuMineCourseDO mikuMineCourseDO, MikuMineDetectReport mikuMineDetectReport, String scProblemShortNames,
			TransactionStatus transactionStatus){
		if(null == mikuMineCourseDO){
			mikuMineCourseDO = new MikuMineCourseDO();
		}
		Date nowDate = new Date();
		mikuMineCourseDO.setCourseName(scProblemShortNames);
		mikuMineCourseDO.setCourseShortName(scProblemShortNames);
		mikuMineCourseDO.setCourseBelongUserid(mikuMineDetectReport.getUserId());
		mikuMineCourseDO.setCreaterId(mikuMineDetectReport.getServiceId());
		mikuMineCourseDO.setVersion(1L);
		mikuMineCourseDO.setDateCreated(nowDate);
		mikuMineCourseDO.setLastUpdated(nowDate);
		if(mikuMineCourseDOMapper.insert(mikuMineCourseDO) < 1){
			transactionStatus.setRollbackOnly();
			return false;
		}
		return true;
	}
	
	/**
	 * 插入课程步骤
	 * @param mikuMineCourseDO
	 * @param courseId
	 * @param stepOrder
	 * @param mikuMineExpertDbDO
	 * @param transactionStatus
	 * @return
	 */
	public boolean insertMikuMineCourseStepDO(MikuMineCourseStepDO mikuMineCourseStepDO, Long courseId, Integer stepOrder,
			MikuMineExpertDbDO mikuMineExpertDbDO,
			TransactionStatus transactionStatus){
		if(null == mikuMineCourseStepDO){
			mikuMineCourseStepDO = new MikuMineCourseStepDO();
		}
		Date nowDate = new Date();
		mikuMineCourseStepDO.setCourseId(courseId);      
		//lgc认为步骤少了个经验id
		mikuMineCourseStepDO.setStepName(mikuMineExpertDbDO.getStepName());       
		mikuMineCourseStepDO.setStepShortName(mikuMineExpertDbDO.getStepShortName());  
		mikuMineCourseStepDO.setStepType(mikuMineExpertDbDO.getStepType());
		mikuMineCourseStepDO.setStepOrder(stepOrder);      
		mikuMineCourseStepDO.setStepInterval(mikuMineExpertDbDO.getStepInterval());   
		mikuMineCourseStepDO.setUseTime(mikuMineExpertDbDO.getUseTime());        
		mikuMineCourseStepDO.setProdId(mikuMineExpertDbDO.getProdId());         
		mikuMineCourseStepDO.setProdUseRemind(mikuMineExpertDbDO.getProdUseRemind());  
		mikuMineCourseStepDO.setVideoName(mikuMineExpertDbDO.getVideoName());      
		mikuMineCourseStepDO.setVideoShortName(mikuMineExpertDbDO.getVideoShortName()); 
		mikuMineCourseStepDO.setVideoUrl(mikuMineExpertDbDO.getVideoUrl());       
		mikuMineCourseStepDO.setVideoTime(mikuMineExpertDbDO.getVideoTime());      
		mikuMineCourseStepDO.setVideoUseRemind(mikuMineExpertDbDO.getVideoUseRemind()); 
		mikuMineCourseStepDO.setVersion(1L);        
		mikuMineCourseStepDO.setDateCreated(nowDate);    
		mikuMineCourseStepDO.setLastUpdated(nowDate);  
		if(mikuMineCourseStepDOMapper.insert(mikuMineCourseStepDO) < 1){
			transactionStatus.setRollbackOnly();
			return false;
		}
		return true;
	}
	
	/**
	 * 插入盒子
	 * @return
	 */
	
	public boolean insertMikuMineScBoxDO(MikuMineScBoxDO mikuMineScBoxDO, Long price, Long detectReportId,
			Long courseId, String insertedCourseStepExpertDbIdsStr, String prodIdListStr, TransactionStatus transactionStatus){
		if(null == mikuMineScBoxDO){
			mikuMineScBoxDO = new MikuMineScBoxDO();
		}
		Date nowDate = new Date();
		mikuMineScBoxDO.setBoxName("boxName");       
		mikuMineScBoxDO.setPrice(price);         
		//mikuMineScBoxDO.setpayStatus     
		mikuMineScBoxDO.setDetectReportId(detectReportId);
		mikuMineScBoxDO.setExpertDbIds(insertedCourseStepExpertDbIdsStr);   
		mikuMineScBoxDO.setScProductIds(prodIdListStr);  
		mikuMineScBoxDO.setCourseId(courseId);      
		mikuMineScBoxDO.setVersion(1L);       
		mikuMineScBoxDO.setDateCreated(nowDate);   
		mikuMineScBoxDO.setLastUpdated(nowDate);   
		if(mikuMineScBoxDOMapper.insert(mikuMineScBoxDO) < 1){
			transactionStatus.setRollbackOnly();
			return false;
		}
		return true;
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
