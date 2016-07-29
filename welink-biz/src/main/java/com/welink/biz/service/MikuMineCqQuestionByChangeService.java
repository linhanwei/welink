package com.welink.biz.service;

import groovy.json.internal.Byt;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.welink.commons.domain.InstallActiveDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.domain.MikuInstrumentMeasureLogDO;
import com.welink.commons.domain.MikuInstrumentMeasureLogDOExample;
import com.welink.commons.domain.MikuMineCqQuestions;
import com.welink.commons.domain.MikuMineCqQuestionsExample;
import com.welink.commons.domain.MikuMineDetectReport;
import com.welink.commons.domain.MikuMineDetectReportExample;
import com.welink.commons.domain.MikuMineIqrecordsDO;
import com.welink.commons.domain.MikuMineIqrecordsDOExample;
import com.welink.commons.domain.MikuMineQoptionsDO;
import com.welink.commons.domain.MikuMineQoptionsDOExample;
import com.welink.commons.domain.MikuMineQuestionOptions;
import com.welink.commons.domain.MikuMineQuestionOptionsExample;
import com.welink.commons.domain.MikuMineQuestionRecords;
import com.welink.commons.domain.MikuMineQuestionRecordsExample;
import com.welink.commons.domain.MikuMineQuestionnaireRecordsDO;
import com.welink.commons.domain.MikuMineQuestionnaireRecordsDOExample;
import com.welink.commons.domain.MikuMineQuestionsDO;
import com.welink.commons.domain.MikuMineQuestionsDOExample;
import com.welink.commons.domain.MikuMineQuestionsRoute;
import com.welink.commons.domain.MikuMineQuestionsRouteExample;
import com.welink.commons.domain.MikuMineScProblemItemDO;
import com.welink.commons.domain.MikuMineScProblemItemDOExample;
import com.welink.commons.domain.MikuMineSkincareSuggestion;
import com.welink.commons.domain.MikuMineSkincareSuggestionExample;
import com.welink.commons.domain.MikuOperMeaureData;
import com.welink.commons.domain.MikuOperMeaureDetail;
import com.welink.commons.domain.MikuOperMeaureListData;
import com.welink.commons.domain.QuestionAndOptionsModel;
import com.welink.commons.domain.QuestionInfo;
import com.welink.commons.domain.WeChatProfileDO;
import com.welink.commons.domain.WeChatProfileDOExample;
import com.welink.commons.persistence.InstallActiveDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.MikuInstrumentMeasureLogDOMapper;
import com.welink.commons.persistence.MikuMineCqMapper;
import com.welink.commons.persistence.MikuMineCqQuestionsMapper;
import com.welink.commons.persistence.MikuMineDetectReportMapper;
import com.welink.commons.persistence.MikuMineIqrecordsDOMapper;
import com.welink.commons.persistence.MikuMineQoptionsDOMapper;
import com.welink.commons.persistence.MikuMineQuestionOptionsMapper;
import com.welink.commons.persistence.MikuMineQuestionRecordsMapper;
import com.welink.commons.persistence.MikuMineQuestionnaireRecordsDOMapper;
import com.welink.commons.persistence.MikuMineQuestionsDOMapper;
import com.welink.commons.persistence.MikuMineQuestionsRouteMapper;
import com.welink.commons.persistence.MikuMineScProblemItemDOMapper;
import com.welink.commons.persistence.MikuMineSkincareSuggestionMapper;

import net.spy.memcached.MemcachedClient;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by daniel on 15-3-18.
 */
@Service
public class MikuMineCqQuestionByChangeService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(MikuMineCqQuestionByChangeService.class);
   
    @Resource
    private MikuMineIqrecordsDOMapper mikuMineIqrecordsDOMapper;
    
    @Resource
    private MikuMineQuestionsDOMapper mikuMineQuestionsDOMapper;
    
    @Resource
    private MikuMineQuestionnaireRecordsDOMapper mikuMineQuestionnaireRecordsDOMapper;
    
    @Resource
    private MikuMineQoptionsDOMapper mikuMineQoptionsDOMapper;
    
    @Resource
    private MikuMineQuestionsDOMapper mikQuestionsDOMapper;
    
    @Resource
    private MikuMineScProblemItemDOMapper mikuMineScProblemItemDOMapper;
    @Resource
    private MikuMineDetectReportMapper mikuMineDetectReportMapper;
    

    
    //进行全部的查询
    public Map getAllData(){
    	Map map=new HashMap<>();
    	List<MikuMineQuestionsDO> all=getAllQuestionData();
    	List<QuestionAndOptionsModel> modelList=new ArrayList<QuestionAndOptionsModel>();
    	for(int j=0;j<all.size();j++){
    		MikuMineQuestionsDO one=all.get(j);
    		List<MikuMineQoptionsDO> list=getOptionsByQuestionId(one.getId());
//    		modelList.add(new QuestionAndOptionsModel(one,list));
    	}
    	map.put("data", modelList);
    	return map;
    }
    
    
    
    //列出对应的所有的问题
    public List<MikuMineQuestionsDO>  getAllQuestionData(){
    	MikuMineQuestionsDOExample mikuMineQuestionsDOExample=new MikuMineQuestionsDOExample();
    	List<MikuMineQuestionsDO>  list=mikQuestionsDOMapper.selectByExample(mikuMineQuestionsDOExample);
    	return list;
    }
    
    
    public List<MikuMineQoptionsDO> getOptionsByQuestionId(Long qid){
    	MikuMineQoptionsDOExample mikuMineQoptionsDOExample=new MikuMineQoptionsDOExample();
    	mikuMineQoptionsDOExample.createCriteria().andQuestionIdEqualTo(qid);
    	List<MikuMineQoptionsDO> list=mikuMineQoptionsDOMapper.selectByExample(mikuMineQoptionsDOExample);
    	return list;
    }
    
    
    
    //初始化插入一条数据[记录信息]
    public MikuMineQuestionnaireRecordsDO insertOneData(Long userId){
    	MikuMineQuestionnaireRecordsDO one=new MikuMineQuestionnaireRecordsDO();
    	one.setUserId(userId);
    	one.setVersion(1L);
    	one.setDateCreated(new Date());
    	mikuMineQuestionnaireRecordsDOMapper.insert(one);
    	return one;
    }
    
    //查询出选择项的内容
    public List<MikuMineScProblemItemDO>  getAllProblemItems(){
    	MikuMineScProblemItemDOExample xample=new MikuMineScProblemItemDOExample();
    	List<MikuMineScProblemItemDO> list=mikuMineScProblemItemDOMapper.selectByExample(xample);
    	return list;
    }
    
    
    
    //根据题目的内容来写入对应的来获取对应的选项id集合
    public List<Long> getOptionIds(String value){
    	List<Long> list=new ArrayList<Long>();
    	String[] arr=value.split(";");
    	for(int i=0;i<arr.length;i++){
    		list.add(Long.parseLong(arr[i]));
    	}
    	return list;
    }
    
    //根据id来查找的问题的记录
//    public MikuMineQuestionsDO getOneRecordById(Long Id){
//    	MikuMineQuestionsDOExample example=new MikuMineQuestionsDOExample();
//    	example.createCriteria().andIdEqualTo(Id);
//    	mikQuestionsDOMapper.se
//    }
    
    //根据id来查找对应的选择项的记录
    
    
    
    
    //根据对应的id的集合,则需要把对应的问题也就是细化的问题集合
    public List<QuestionAndOptionsModel> getOneQuestionDataById(String ids){
    	MikuMineQuestionsDOExample mikuMineQuestionsDOExample=new MikuMineQuestionsDOExample();
    	List<MikuMineQuestionsDO>  list=getOptionsQuestion(ids);
    	List<QuestionAndOptionsModel> modelList=new ArrayList<QuestionAndOptionsModel>();
    	for(int j=0;j<list.size();j++){
    		MikuMineQuestionsDO one=list.get(j);
    		List<MikuMineQoptionsDO> oneoption=getOptionsByQuestionId(one.getId());
    		QuestionAndOptionsModel model=new QuestionAndOptionsModel();
    		model.setOptionsList(oneoption);
    		model.setQuestion(one);
    		modelList.add(model);
    	}
    	return modelList;
    }
    
    
   //根据必答题来查出全部的选项题目
    public List<MikuMineQuestionsDO>  getOptionsQuestion(String ids){
    	Map map=new HashMap<>();
    	map.put("ids", ids);
    	List<MikuMineQuestionsDO> list=mikQuestionsDOMapper.selectQuestionByids(map);
    	return list;
    }
    
    //题目的集合
    //qid1$id1;id2;id3|qid2$id11;id12;id13
    //1$10|23$66
    public void InsertMoreDataMytargetContent(String ids,Long userId,Long recordId){
    	//ids就是问题与选项的集合
    	if(!("".equals(ids)) && ids!=null && ids.indexOf("$")>-1){
    		MikuMineQuestionnaireRecordsDO targetrecord=mikuMineQuestionnaireRecordsDOMapper.selectByPrimaryKey(recordId);
    		String proids="";
    		String[] strarr=ids.split("\\|");
    		for(int i=0;i<strarr.length;i++){
    			String oneobj=strarr[i];
    			if(!("".equals(oneobj)) && oneobj!=null){
    				String[] onestrarr=oneobj.split("\\$");
    				String qid=onestrarr[0];
					String optionsid=onestrarr[1];
					MikuMineQuestionsDO mikuMineQuestionsDO=mikQuestionsDOMapper.selectByPrimaryKey(Long.parseLong(qid));
					MikuMineQoptionsDO mikuMineQoptionsDO=mikuMineQoptionsDOMapper.selectByPrimaryKey(Long.parseLong(optionsid));
					MikuMineIqrecordsDO mikuMineIqrecordsDO=new MikuMineIqrecordsDO();
					mikuMineIqrecordsDO.setLastUpdated(new Date());
					mikuMineIqrecordsDO.setDateCreated(new Date());
					mikuMineIqrecordsDO.setVersion(1L);
					mikuMineIqrecordsDO.setUserId(userId);
					mikuMineIqrecordsDO.setQuestionnaireRecordsId(recordId);
					mikuMineIqrecordsDO.setQuestionId(mikuMineQuestionsDO.getId());
					mikuMineIqrecordsDO.setScProblemId(mikuMineQuestionsDO.getScProblemId());
					mikuMineIqrecordsDO.setQuestionName(mikuMineQuestionsDO.getQuestionName());
					mikuMineIqrecordsDO.setQuestionShortName(mikuMineQuestionsDO.getQuestionShortName());
					mikuMineIqrecordsDO.setOptionId(mikuMineQoptionsDO.getId());
					mikuMineIqrecordsDO.setOptionName(mikuMineQoptionsDO.getOptionName());
					mikuMineIqrecordsDO.setOptionValue(mikuMineQoptionsDO.getOptionValue());
					mikuMineIqrecordsDOMapper.insert(mikuMineIqrecordsDO);
					proids+=(mikuMineIqrecordsDO.getId()+",");
    			}
    		}
    		if(proids.length()>1){
    			targetrecord.setDetailedProblemRecordsId(proids.substring(0,proids.length()-1));
    			mikuMineQuestionnaireRecordsDOMapper.updateByPrimaryKey(targetrecord);
    		}
    	}
    }
    
    //根据对应的recordId来进行获取对应的记录
    public List<MikuMineIqrecordsDO> getResultByRecordId(Long recordId){
    	MikuMineIqrecordsDOExample example=new MikuMineIqrecordsDOExample();
    	example.createCriteria().andQuestionnaireRecordsIdEqualTo(recordId);
    	List<MikuMineIqrecordsDO> list=mikuMineIqrecordsDOMapper.selectByExample(example);
    	return list;
    }
    
    
    
    //拿出对应的报告值
    public Map getOneAllRecordsData(Long recordId){
    	Map map=new HashMap<>();
    	MikuMineQuestionnaireRecordsDO targetrecord=mikuMineQuestionnaireRecordsDOMapper.selectByPrimaryKey(recordId);
    	Map scmap=new HashMap<>();
    	scmap.put("ids", targetrecord.getScProblemIds());
		List<MikuMineScProblemItemDO> list=mikuMineScProblemItemDOMapper.selectscOptionsByIDs(scmap);
		String str="";
		for(int j=0;j<list.size();j++){
			str+=(list.get(j).getScProblemName()+" ");
		}
		targetrecord.setScProblemIds(str);
		//再进行到细化的问题了
		List<MikuMineIqrecordsDO> iqcords= getResultByRecordId(recordId);
		map.put("record", targetrecord);
		map.put("questions",iqcords);
    	return map;
    }
    
    
    
    //根据的用户的id来拿对应的全部的调查问卷
    public List<MikuMineQuestionnaireRecordsDO> selectAllRecordsByUserId(Long userId){
    	MikuMineQuestionnaireRecordsDOExample example=new MikuMineQuestionnaireRecordsDOExample();
    	example.createCriteria().andUserIdEqualTo(userId);
    	List<MikuMineQuestionnaireRecordsDO> list=mikuMineQuestionnaireRecordsDOMapper.selectByExample(example);
    	return list;
    }
    

    //插入与更新报告操作
	public Map InsertOneReportData(String recordId, String userInfo,
			String picUrls, String suggestionInfo, Long userId, Long serviceId,
			String flag, Long rid) {
		MikuMineDetectReport mikuMineDetectReport=new MikuMineDetectReport();
		mikuMineDetectReport.setDateCreated(new Date());
		mikuMineDetectReport.setLastUpdated(new Date());
		mikuMineDetectReport.setVersion(1L);
		mikuMineDetectReport.setQuestionnaireRecordsId(Long.parseLong(recordId));
		mikuMineDetectReport.setPicUrls(picUrls);
		mikuMineDetectReport.setUserId(userId);
		mikuMineDetectReport.setUserInfo(userInfo);
		mikuMineDetectReport.setSuggestionInfo(suggestionInfo);
		mikuMineDetectReport.setServiceId(serviceId);
		if("0".equals(flag)){
			mikuMineDetectReportMapper.insert(mikuMineDetectReport);
		}
		else{
			mikuMineDetectReport.setId(rid);
			mikuMineDetectReportMapper.updateByPrimaryKey(mikuMineDetectReport);
		}
		Map map=new HashMap<>();
		map.put("data", mikuMineDetectReport);
		
		MikuMineQuestionnaireRecordsDO targetrecord=mikuMineQuestionnaireRecordsDOMapper.selectByPrimaryKey(mikuMineDetectReport.getQuestionnaireRecordsId());
    	Map scmap=new HashMap<>();
    	scmap.put("ids", targetrecord.getScProblemIds());
		List<MikuMineScProblemItemDO> list=mikuMineScProblemItemDOMapper.selectscOptionsByIDs(scmap);
		String str="";
		for(int j=0;j<list.size();j++){
			str+=(list.get(j).getScProblemName()+" ");
		}
		targetrecord.setScProblemIds(str);
		//再进行到细化的问题了
		List<MikuMineIqrecordsDO> iqcords= getResultByRecordId(mikuMineDetectReport.getQuestionnaireRecordsId());
		map.put("record", targetrecord);
		map.put("questions",iqcords);
		
		
		return map;
	}
	
	
	
	//查询对应的用户的调查报告
	public List<MikuMineDetectReport> getDataByUid(Long uid){
		MikuMineDetectReportExample mikuMineDetectReportExample=new MikuMineDetectReportExample();
		mikuMineDetectReportExample.createCriteria().andUserIdEqualTo(uid);
		List<MikuMineDetectReport> list=mikuMineDetectReportMapper.selectByExample(mikuMineDetectReportExample);
    	return list;
	}
    

    
    
    
    
    
}





