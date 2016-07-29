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
import com.welink.commons.domain.MikuMineQuestionOptions;
import com.welink.commons.domain.MikuMineQuestionOptionsExample;
import com.welink.commons.domain.MikuMineQuestionRecords;
import com.welink.commons.domain.MikuMineQuestionRecordsExample;
import com.welink.commons.domain.MikuMineQuestionsRoute;
import com.welink.commons.domain.MikuMineQuestionsRouteExample;
import com.welink.commons.domain.MikuMineSkincareSuggestion;
import com.welink.commons.domain.MikuMineSkincareSuggestionExample;
import com.welink.commons.domain.MikuOperMeaureData;
import com.welink.commons.domain.MikuOperMeaureDetail;
import com.welink.commons.domain.MikuOperMeaureListData;
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
import com.welink.commons.persistence.MikuMineQuestionOptionsMapper;
import com.welink.commons.persistence.MikuMineQuestionRecordsMapper;
import com.welink.commons.persistence.MikuMineQuestionsRouteMapper;
import com.welink.commons.persistence.MikuMineSkincareSuggestionMapper;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by daniel on 15-3-18.
 */
@Service
public class MikuMineSkinQuestionService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(MikuMineSkinQuestionService.class);
    
    @Resource
	private MikuInstrumentMeasureLogDOMapper mikuInstrumentMeasureLogDOMapper;
    @Resource
    private MikuMineSkincareSuggestionMapper mikuMineSkincareSuggestionMapper;
    @Resource
    private ItemMapper itemMapper;
    @Resource
    private MikuMineCqMapper mikuMineCqMapper;
    @Resource
    private MikuMineCqQuestionsMapper mikuMineCqQuestionsMapper;
    @Resource
    private MikuMineQuestionOptionsMapper mikuMineQuestionOptionsMapper;
    @Resource
    private MikuMineQuestionRecordsMapper mikuMineQuestionRecordsMapper;
    @Resource
    private MikuMineQuestionsRouteMapper mikQuestionsRouteMapper;
    
    @Resource
    private MikuMineDetectReportMapper mikuMineDetectReportMapper;
    @Resource
    private MikuCsadDOMapper mikuCsadDOMapper;
    
    
    //测试版拿出对应的报告
    public Map getoneSkinRecord(String content,String skinType){
    	Map map=new HashMap<>();
    	map.put("skinType", skinType);
    	map.put("itemlist", getItemList());
    	return map;
    }
    
    
    
    
    
    
    
    
    
    
    //测试版
    public Map getQuestionByIndexTest(Long index,Long optionId,Long questionId,Long profileId,Byte quireType,String value,String flag){
    	Map map=new HashMap<>();
    	List<MikuMineCqQuestions> list=new ArrayList<MikuMineCqQuestions>();
    	MikuMineQuestionsRoute route=new MikuMineQuestionsRoute();
    	MikuMineCqQuestions questions=new MikuMineCqQuestions();
    	List<MikuMineQuestionOptions> optionList=new ArrayList<MikuMineQuestionOptions>();
    	
    	if(index==0L){
    		list=getTheQuestions(1L);
    		questions=list.get(0);
    	}else{
    		questions=getTheQuestions(questionId).get(0);
    		//插入对应的数据
    		if(!("1".equals(flag))){
    			insertOneRecord(profileId, optionId, 1L, questionId, questions.getQuestionName(), questions.getQuestionType(), value);
    		}else{
    			//更新的是数据
    			updateOneRecord(profileId, optionId, 1L, questionId, questions.getQuestionName(), questions.getQuestionType(), value);
    		}
    		if(getOneRoute(optionId).size()>0){
    			route=getOneRoute(optionId).get(0);
    			questions=getTheQuestions(route.getOptionNextskipQuestionId()).get(0);
    		}
    	}
    	if(questions!=null){
    		optionList=getOptionsByQuestionsId(questions.getId());
    	}
    	map.put("question", questions);
    	map.put("option", optionList);
    	map.put("route", route);
    	
    	return map;
    }
    
    
    //根据最后一题插入数据再进行把对应的报告展示出来
    public Map insertFinalAndPrintReort(Long questionId,Long profileId,Byte quireType,String optionIds,String flag,String uuid){
    	Map map=new HashMap<>();
    	//先拿到必选题
    	MikuMineCqQuestions questions=new MikuMineCqQuestions();
    	//找到对应的题目
		questions=getTheQuestions(questionId).get(0);
		List<Long> optionids= getOptionIds(optionIds);
		List<MikuMineQuestionsRoute> routelist=getsomeRouteBylonglist(optionids);
    	//插入对应的数据
		if(!("1".equals(flag))){
			//是否为单选还是双选的操作
			if(optionids.size()>0 && optionids.size()==1){
				insertOneRecord(profileId, optionids.get(0), 1L, questionId, questions.getQuestionName(),questions.getQuestionType(), optionIds,questions.getId(),getChildIdsByquestionId(routelist),uuid);
			}else if(optionids.size()>1){
				insertOneRecord(profileId, 0L, 1L, questionId, questions.getQuestionName(),questions.getQuestionType(), optionIds,questions.getId(),getChildIdsByquestionId(routelist),uuid);
			}
		}else{
			if(optionids.size()>0 && optionids.size()==1){
				//更新的是数据
    			updateOneRecord(profileId, optionids.get(0), 1L, questionId, questions.getQuestionName(), questions.getQuestionType(), optionIds,questions.getId(),getChildIdsByquestionId(routelist),uuid);
			}else if(optionids.size()>1){
				updateOneRecord(profileId, 0L, 1L, questionId, questions.getQuestionName(), questions.getQuestionType(), optionIds,questions.getId(),getChildIdsByquestionId(routelist),uuid);
			}
		}
		
		//显示出全部的选择题与答案
		List<MikuMineQuestionRecords> rlist=getOneUserRecordByuuid(uuid);
//    	for(int z=0;z<rlist.size();z++){
//    		MikuMineQuestionRecords mm=rlist.get(z);
//    		List<Long> oneQuestionByoptionids= getOptionIds(mm.getOptionValue());
//    		String str="";
//    		for(int j=0;j<oneQuestionByoptionids.size();j++){
//    			List<MikuMineQuestionOptions> olist= getOneOptionsById(oneQuestionByoptionids.get(j));
//    			if(olist.size()>0){
//    				str+=olist.get(0).getOptionValue();
//    				str+="  ";
//    			}
//    		}
//    		mm.setUuid(str);
//    	}
    	map.put("data", getFinalQuestion(rlist));
    	map.put("prevId", questionId);
    	map.put("prevOptionId", optionIds);
    	return map;
    }
    
    
    
    
    //根据对应的UUID来获取对应的结论
    public Map getFinalRecord(String uuid){
		    	Map map=new HashMap<>();
		    	//显示出全部的选择题与答案
    			List<MikuMineQuestionRecords> rlist=getOneUserRecordByuuid(uuid);
//    	    	for(int z=0;z<rlist.size();z++){
//    	    		MikuMineQuestionRecords mm=rlist.get(z);
//    	    		List<Long> oneQuestionByoptionids= getOptionIds(mm.getOptionValue());
//    	    		String str="";
//    	    		for(int j=0;j<oneQuestionByoptionids.size();j++){
//    	    			List<MikuMineQuestionOptions> olist= getOneOptionsById(oneQuestionByoptionids.get(j));
//    	    			if(olist.size()>0){
//    	    				str+=olist.get(0).getOptionValue();
//    	    				str+="  ";
//    	    			}
//    	    		}
//    	    		mm.setUuid(str);
//    	    	}
    	    	map.put("data", getFinalQuestion(rlist));
    	    	return map;
    }
    
    
    
    public List<QuestionInfo> getFinalQuestion(List<MikuMineQuestionRecords> rlist){
//    	MikuMineQuestionRecords mm=rlist.get(z);
//		List<Long> oneQuestionByoptionids= getOptionIds(mm.getOptionValue());
//		String str="";
//		for(int j=0;j<oneQuestionByoptionids.size();j++){
//			List<MikuMineQuestionOptions> olist= getOneOptionsById(oneQuestionByoptionids.get(j));
//			if(olist.size()>0){
//				str+=olist.get(0).getOptionValue();
//				str+="  ";
//			}
//		}
    	List<QuestionInfo> finalList=new ArrayList<QuestionInfo>();
    	for(int z=0;z<rlist.size();z++)
    	{
    		MikuMineQuestionRecords mm=rlist.get(z);
    		QuestionInfo onenew=new QuestionInfo();
    		onenew.setId(mm.getId());
    		onenew.setChildids(mm.getChildids());
    		onenew.setOptionValue(mm.getOptionValue());
    		onenew.setParentQid(mm.getParentQid());
    		onenew.setQuestionId(mm.getQuestionId());
    		onenew.setQuestionName(mm.getQuestionName());
    		onenew.setQuestionType(mm.getQuestionType());
    		onenew.setQuestionnaireId(mm.getQuestionnaireId());
    		onenew.setUserId(mm.getUserId());
    		onenew.setUuid(mm.getUuid());
    		onenew.setPrevId(-1L);
    		onenew.setPrevOptionId("-1");
    		//添加的是optionName,optionShowStyle,questionShorter,reportPrintArea
    		//找到对应的题目
    		if(z>0){
    			onenew.setPrevId((rlist.get(z-1)).getQuestionId());
    			onenew.setPrevOptionId((rlist.get(z-1)).getOptionValue());
    			//问题
    			MikuMineCqQuestions question=getTheQuestions(mm.getQuestionId()).get(0);
        		onenew.setReportPrintArea(question.getReportPrintArea());
        		onenew.setQuestionShorter(question.getQuestionShorter());
        		//选项
        		List<Long> oneQuestionByoptionids= getOptionIds(mm.getOptionValue());
        		String optionstylestr="",optionname="",optionrvalue="";
        		for(int j=0;j<oneQuestionByoptionids.size();j++){
        			List<MikuMineQuestionOptions> olist= getOneOptionsById(oneQuestionByoptionids.get(j));
        			if(olist.size()>0){
        				optionstylestr+=olist.get(0).getOptionShowStyle()+"";
        				optionname+=olist.get(0).getOptionName()+"";
        				optionrvalue+=olist.get(0).getOptionValue()+"";
        				if(j!=oneQuestionByoptionids.size()-1){
        					optionstylestr+=",";
        					optionname+=",";
        					optionrvalue+=",";
        				}
        			}
        		}
        		onenew.setOptionShowStyle(optionstylestr);
        		onenew.setOptionName(optionname);
        		onenew.setOptionRvalue(optionrvalue);
    		}
    		finalList.add(onenew);
    	}
    	return finalList;
    }
    
    
    //qid1$id1;id2;id3|qid2$id11;id12;id13
    //1$1;2;3|23$66
    public void deletMytargetContent(String deleteContent,String uuid){
    	if(!("".equals(deleteContent)) && deleteContent!=null && deleteContent.indexOf("$")>-1){
    		String[] strarr=deleteContent.split("\\|");
    		for(int i=0;i<strarr.length;i++){
    			String oneobj=strarr[i];
    			if(!("".equals(oneobj)) && oneobj!=null){
    				String[] onestrarr=oneobj.split("\\$");
    				for(int j=0;j<onestrarr.length;j++){
    					String qid=onestrarr[0];
    					String optionsid=onestrarr[1];
    					deleOneData(qid,optionsid,uuid);
    				}
    			}
    		}
    	}
    }
    
    
    
    
    
    
    //根据前一道题与前一个选项来进行获取下一道题
    public Map selectByoneQuqstionByLastQuestionAndOptionIds(Long questionId,Long profileId,Byte quireType,String optionIds,String flag,String uuid,String deleteContent){
    	Map map=new HashMap<>();
    	map.put("isend",false);
    	String targetStr="";
    	//先拿到必选题
    	MikuMineCqQuestions questions=new MikuMineCqQuestions();
    	//获取的第一道题目
    	if(questionId==0L && ("0".equals(flag))){
    		List<MikuMineCqQuestions>  firstlist= getFirstQuestions((byte) 1);
    		UUID adduuid=UUID.randomUUID();
    		questions=firstlist.get(0);
    		//一开始就进行插入对应的初始化记录
    		insertOneRecord(profileId, -1L, 1L, 0L, "begin",(byte)-1, "0",0L,getOneIdsStr(firstlist),adduuid.toString());
    		uuid=adduuid.toString();
    	}else{
    		//删除对应缓存的数据
    		deletMytargetContent(deleteContent,uuid);
    		//找到对应的题目
    		questions=getTheQuestions(questionId).get(0);
    		List<Long> optionids= getOptionIds(optionIds);
    		List<MikuMineQuestionsRoute> routelist=getsomeRouteBylonglist(optionids);
    		Long realOptionId=0L;
    		String realChilds="";
    		//插入对应的数据
    		if(!("1".equals(flag))){
    			//是否为单选还是双选的操作
    			if(optionids.size()>0 && optionids.size()==1){
    				realOptionId=optionids.get(0);
    			}
    			realChilds=getChildIdsByquestionId(routelist);
    			//这道题比较特殊:回答测试皮肤内容【额外的处理】
    			if(questionId == 1L){
    				//进行跟路由表进行校验
    				List<MikuMineQuestionsRoute> targetrouteList= getTargetRoute(questionId,3L);
    				if(targetrouteList.size()>0){
    					MikuMineQuestionsRoute targetroute=targetrouteList.get(0);
    					List<Long> routechilds=getOptionIds(targetroute.getQuestionRouteTag());
    					boolean routeflag=compareTwoStr(routechilds,optionids);
    					if(routeflag){
    						realChilds=targetroute.getOptionNextskipQuestionId()+";"+realChilds;
        					routelist=insertFirstRoute(routelist,targetroute);
    					}
    				}
    			}
    			insertOneRecord(profileId, realOptionId, 1L, questionId, questions.getQuestionName(),questions.getQuestionType(), optionIds,questions.getId(),realChilds,uuid);
    		}else{
    			//这道题比较特殊:回答测试皮肤内容【额外的处理】
    			if(optionids.size()>0 && optionids.size()==1){
					realOptionId=optionids.get(0);
    			}
    			realChilds=getChildIdsByquestionId(routelist);
    			//这道题比较特殊:回答测试皮肤内容【额外的处理】
    			if(questionId == 1L){
    				//进行跟路由表进行校验
    				List<MikuMineQuestionsRoute> targetrouteList= getTargetRoute(questionId,3L);
    				if(targetrouteList.size()>0){
    					MikuMineQuestionsRoute targetroute=targetrouteList.get(0);
    					List<Long> routechilds=getOptionIds(targetroute.getQuestionRouteTag());
    					boolean routeflag=compareTwoStr(routechilds,optionids);
    					if(routeflag){
    						realChilds=targetroute.getOptionNextskipQuestionId()+";"+realChilds;
        					routelist=insertFirstRoute(routelist,targetroute);
    					}
    				}
    			}
				updateOneRecord(profileId,realOptionId, 1L, questionId, questions.getQuestionName(), questions.getQuestionType(), optionIds,questions.getId(),realChilds,uuid);
    		}
    		//找题还是比较难
    		if(routelist.size()>0){
    			questions=getQuestionsById(routelist.get(0).getOptionNextskipQuestionId()).get(0);
    		}else{
    			//首先根据路由表来获取对应上一题父级题目的id
    			//获取是上级的路由列表
    			List<MikuMineQuestionsRoute>  getsameChilds= selectParentBychildId(questionId);
    			MikuMineQuestionRecords parentRecords=new MikuMineQuestionRecords();
    			if(getsameChilds.size()>0){
    				List<MikuMineQuestionRecords> varlist=new ArrayList<MikuMineQuestionRecords>();
        			
    				//再进行与记录表进行校验，完成抽取此人调查问卷题目父级id的集合
        			for(int z=0;z<getsameChilds.size();z++){
        				MikuMineQuestionsRoute oneroute=getsameChilds.get(z);
            			//校验的集合来自于
            			List<MikuMineQuestionRecords> selectlit=getOneRecordByuuIdAndQuestionId(uuid,oneroute.getQuestionId());
            			int nflag=0;
            			for(int jz=0;jz<selectlit.size();jz++){
            				MikuMineQuestionRecords mm=selectlit.get(jz);
            				String records=mm.getChildids();
            				if(records.length()>0){
            					String[] arr=records.split(";");
                		    	for(int jx=0;jx<arr.length;jx++){
                		    		if(questionId == Long.parseLong(arr[jx])){
                		    			nflag=1;
                		    		}
                		    	}
            				}
            			}
            			if(nflag==0){
            				continue;
            			}
            			if(selectlit.size()>0){
            				varlist.add(selectlit.get(0));
            			}
        			}
        			
        			//比如这里的需求是:  14作为的子节点 为例子      查找对应的父节点的时候是需要判断，以免死循环
        			int varsize=varlist.size();
        			if(varsize==1){
        				parentRecords=varlist.get(0);
        			}else if(varsize>1){
        				//这里的坐标需要往前找
        				List<MikuMineQuestionRecords> sresultlist=getOneRecordByuuIdAndQuestionId(uuid,questionId);
        				parentRecords=varlist.get(varsize-sresultlist.size());
        			}
        			
    			}else{
    				parentRecords=getOneRecordByuuIdAndQuestionId(uuid,profileId).get(0);
    			}
    			//这是下一道题的id
    			targetStr=getonePathForParentBynowId(parentRecords,questionId,uuid);
    			//最后一题的判断
    			if("-9999".equals(targetStr)){
    				map.put("end", "ok");
    				return map;
    			}else{
    				questions=getQuestionsById(Long.parseLong(targetStr)).get(0);
    			}
    			map.put("isend", finalQuestionByFlag(parentRecords,questionId,uuid));
    		}
    	}
    	List<MikuMineQuestionOptions> optionList=new ArrayList<MikuMineQuestionOptions>();
    	if(questions!=null){
    		optionList=getOptionsByQuestionsId(questions.getId());
    	}
    	map.put("question", questions);
    	map.put("option", optionList);
    	map.put("uuid", uuid);
    	map.put("targetStr", targetStr);
    	map.put("prevId", questionId);
    	map.put("prevOptionId", optionIds);
    	return map;
    }
    
    
    
    
    //判断是否是最后一道题
    public Boolean finalQuestionByFlag(MikuMineQuestionRecords parentRecords,Long question,String uuid){
    	Boolean flg=false;
    	String records=parentRecords.getChildids();
    	String[] arr=records.split(";");
    	if(arr.length>0 && !("".equals(records))){
    		if(arr.length>2 && parentRecords.getParentQid() == 4L && question == Long.parseLong(arr[arr.length-2])){
    			flg=true;
			}
    	}
    	return flg;
    }
    
    
    
    
    //获取对应的记录，看对应的字节点是否走完    需要的参数是当前的问题ID
    public String getonePathForParentBynowId(MikuMineQuestionRecords parentRecords,Long question,String uuid){
    	String str="";
    	String records=parentRecords.getChildids();
    	String[] arr=records.split(";");
    	//判断此id为当前父节点下最后一个节点值
    	if(arr.length>0 && !("".equals(records))){
    		//再进行跳到对应
    		if(question == Long.parseLong(arr[arr.length-1])){
    			//判断是否为必选题目
    			if(parentRecords.getParentQid() == 0L){
    				str="-9999";
    			}else{
    				//再进行找父节点
        			List<MikuMineQuestionsRoute>  getsameChilds= selectParentBychildId(parentRecords.getParentQid());
        			MikuMineQuestionRecords nextrecords=new MikuMineQuestionRecords();
        			for(int z=0;z<getsameChilds.size();z++){
        				MikuMineQuestionsRoute oneroute=getsameChilds.get(z);
            			//校验的集合来自于
            			List<MikuMineQuestionRecords> selectlit=getOneRecordByuuIdAndQuestionId(uuid,oneroute.getQuestionId());
            			if(selectlit.size()>0){
            				nextrecords=selectlit.get(0);
            				break;
            			}
        			}
        			str=getonePathForParentBynowId(nextrecords,parentRecords.getParentQid(),uuid);
    			}
    			
    		}else{
    			//当前节点还没有走完
    			for(int z=0;z<arr.length;z++){
    	    		if(question == Long.parseLong(arr[z])){
    	    			str=arr[z+1];
    	    			break;
    	    		}
    	    	}
    			
    		}
    	}
    	return str;
    }
    
    
    public boolean compareTwoStr(List<Long> onelist,List<Long> twolist){
    	boolean flag=false;
    	for(int j=0,len=onelist.size();j<len;j++){
    		Long one=onelist.get(j);
    		for(int z=0,zlen=twolist.size();z<zlen;z++){
    			if(one==twolist.get(z)){
    				flag=true;
    				break;
    			}
    		}
    		if(flag){
    			break;
    		}
    	}
    	return flag;
    }
    
    
    
    
    
    
    //进行为第三题额外动态添加这道题,并添加到首位
    public List<MikuMineQuestionsRoute> insertFirstRoute(List<MikuMineQuestionsRoute> list,MikuMineQuestionsRoute model){
    	List<MikuMineQuestionsRoute> nlist=new ArrayList<MikuMineQuestionsRoute>();
    	nlist.add(model);
    	nlist.addAll(list);
    	return nlist;
    }
    
    
    
    
    //根据对应的多选的内容值则需要用户进行拿到对应的下一道题的内容值
    public String getChildIdsByquestionId(List<MikuMineQuestionsRoute> rlist){
    	String str="";
    	for(int z=0;z<rlist.size();z++){
    		str+=rlist.get(z).getOptionNextskipQuestionId();
    		if(z!=(rlist.size()-1)){
    			str+=";";
    		}
    	}
    	return str;
    }
    
    
    //插入一条用户使用的记录
    public int insertOneRecord(Long profileId,Long optionId,Long questionnaireId,Long questionId,String name,Byte questionType,String value,Long parentPid,String childids,String uuid){
    	MikuMineQuestionRecords record=new MikuMineQuestionRecords();
    	record.setDateCreated(new Date());
    	record.setLastUpdated(new Date());
    	record.setOptionId(optionId);
    	record.setQuestionnaireId(questionnaireId);
    	record.setQuestionType(questionType);
    	record.setQuestionId(questionId);
    	record.setVersion(1L);
    	record.setUserId(profileId);
    	record.setOptionValue(value);
    	record.setQuestionName(name);
    	//插入的是父级信息
    	record.setParentQid(parentPid);
    	record.setChildids(childids);
    	record.setUuid(uuid);
    	return mikuMineQuestionRecordsMapper.insert(record);
    } 
    
    //进行找出对应的数据【已经插入过的操作了】
    public int updateOneRecord(Long profileId,Long optionId,Long questionnaireId,Long questionId,String name,Byte questionType,String value,Long parentPid,String childids,String uuid){
    	List<MikuMineQuestionRecords>  list=getOneRecord(profileId,questionId);
    	int flag=0;
    	if(list.size()>0){
    		MikuMineQuestionRecords record=list.get(list.size()-1);
        	record.setDateCreated(new Date());
        	record.setLastUpdated(new Date());
        	record.setOptionId(optionId);
        	record.setQuestionnaireId(questionnaireId);
        	record.setQuestionType(questionType);
        	record.setQuestionId(questionId);
        	record.setVersion(1L);
        	record.setUserId(profileId);
        	record.setOptionValue(value);
        	record.setQuestionName(name);
        	//插入的是父级信息
        	record.setParentQid(parentPid);
        	record.setChildids(childids);
        	record.setUuid(uuid);
        	flag=mikuMineQuestionRecordsMapper.updateByPrimaryKey(record);
    	}
    	return flag;
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
    
    
    //根据集合list来获取对应的ids
    public String getOneIdsStr(List<MikuMineCqQuestions> list){
    	String str="";
    	for(int z=0;z<list.size();z++){
    		str+=list.get(z).getId();
    		if(z!=list.size()-1){
    			str+=";";
    		}
    	}
    	return str;
    }
    
    
    //根据optionIds集合来进行获取对应的全部路由
    public List<MikuMineQuestionsRoute>  getsomeRouteBylonglist(List<Long> oplist){
    	List<MikuMineQuestionsRoute> rlist=new ArrayList<MikuMineQuestionsRoute>();
    	for(int j=0;j<oplist.size();j++){
    		rlist.addAll(getOneRoute(oplist.get(j)));
    	}
    	return rlist;
    }
    
    
    
    
  
    
    
    
    
    
    //查找全部题目  根据optionid来进行获取对应的题目
    public String getMoreQuestion(Long optionId){
    	List<MikuMineCqQuestions> list=new ArrayList<MikuMineCqQuestions>();
    	
    	List<MikuMineCqQuestions>  firstlist= getFirstQuestions((byte) 1);
    	
    	list.addAll(firstlist);
    	
    	List<MikuMineQuestionsRoute> routeList=getOneRoute(optionId);
    	
    	if(routeList.size()>0){
    		Map map=new HashMap();
    		String str="";
    		for(int j=0;j<routeList.size();j++){
    			MikuMineQuestionsRoute monedata=routeList.get(j);
    			str+=monedata.getOptionNextskipQuestionId();
    			if(j!=routeList.size()-1){
    				str+=";";
    			}
    		}
    		map.put("ids", str);
    		List<MikuMineCqQuestions>  addquestions=mikuMineCqQuestionsMapper.selectQuestionOrderByIds(map);
    		list.addAll(addquestions);
    	}
    	
    	
    	return JSON.toJSONString(list);
    }
    
    
    
    //根据题目的id与uuid与optionsid的值来进行查出对应的值
    public int deleOneData(String qid,String optionids,String uuid){
    	MikuMineQuestionRecordsExample mikuMineQuestionRecordsExample=new MikuMineQuestionRecordsExample();
    	mikuMineQuestionRecordsExample.createCriteria().andUuidEqualTo(uuid).andQuestionIdEqualTo(Long.parseLong(qid)).andOptionValueEqualTo(optionids);
    	List<MikuMineQuestionRecords> list=mikuMineQuestionRecordsMapper.selectByExample(mikuMineQuestionRecordsExample);
    	if(list.size()>0){
    		MikuMineQuestionRecords mm=list.get(0);
    		mikuMineQuestionRecordsMapper.deleteByPrimaryKey(mm.getId());
    	}
    	return 0;
    }
    
   
    
    
    //根据对应的uuid来进行获取对应全部数据
    public List<MikuMineQuestionRecords> getOneUserRecordByuuid(String uuid){
    	MikuMineQuestionRecordsExample mikuMineQuestionRecordsExample=new MikuMineQuestionRecordsExample();
    	mikuMineQuestionRecordsExample.createCriteria().andUuidEqualTo(uuid);
    	List<MikuMineQuestionRecords> list=mikuMineQuestionRecordsMapper.selectByExample(mikuMineQuestionRecordsExample);
    	return list;
    }
    
 
    
    
    
    
    //插入一条用户使用的记录
    public int insertOneRecord(Long profileId,Long optionId,Long questionnaireId,Long questionId,String name,Byte questionType,String value){
    	MikuMineQuestionRecords record=new MikuMineQuestionRecords();
    	record.setDateCreated(new Date());
    	record.setLastUpdated(new Date());
    	record.setOptionId(optionId);
    	record.setQuestionnaireId(questionnaireId);
    	record.setQuestionType(questionType);
    	record.setQuestionId(questionId);
    	record.setVersion(1L);
    	record.setUserId(profileId);
    	record.setOptionValue(value);
    	record.setQuestionName(name);
    	//插入的是
    	return mikuMineQuestionRecordsMapper.insert(record);
    }
    
    
    //进行找出对应的数据【已经插入过的操作了】
    public int updateOneRecord(Long profileId,Long optionId,Long questionnaireId,Long questionId,String name,Byte questionType,String value){
    	List<MikuMineQuestionRecords>  list=getOneRecord(profileId,questionId);
    	int flag=0;
    	if(list.size()>0){
    		MikuMineQuestionRecords record=list.get(list.size()-1);
        	record.setDateCreated(new Date());
        	record.setLastUpdated(new Date());
        	record.setOptionId(optionId);
        	record.setQuestionnaireId(questionnaireId);
        	record.setQuestionType(questionType);
        	record.setQuestionId(questionId);
        	record.setVersion(1L);
        	record.setUserId(profileId);
        	record.setOptionValue(value);
        	record.setQuestionName(name);
        	flag=mikuMineQuestionRecordsMapper.updateByPrimaryKey(record);
    	}
    	return flag;
    }
    
    
   
    //开发版
    public Map getQuestionByIndex(Long index,Long optionId,Long questionId,Long profileId,Byte quireType,String value,String flag){
    	Map map=new HashMap<>();
    	List<MikuMineCqQuestions> list=new ArrayList<MikuMineCqQuestions>();
    	MikuMineQuestionsRoute route=new MikuMineQuestionsRoute();
    	MikuMineCqQuestions questions=new MikuMineCqQuestions();
    	List<MikuMineQuestionOptions> optionList=new ArrayList<MikuMineQuestionOptions>();
    	if(index==0L){
    		list=getTheQuestions(1L);
    		questions=list.get(0);
    	}else{
    		questions=getTheQuestions(questionId).get(0);
    		//插入对应的数据
    		insertOneRecord(profileId, optionId, 1L, questionId, questions.getQuestionName(), questions.getQuestionType(), value);
    		if(getOneRoute(optionId).size()>0){
    			route=getOneRoute(optionId).get(0);
    			questions=getTheQuestions(route.getOptionNextskipQuestionId()).get(0);
    		}
    	}
    	if(questions!=null){
    		optionList=getOptionsByQuestionsId(questions.getId());
    	}
    	map.put("question", questions);
    	map.put("option", optionList);
    	map.put("route", route);
    	return map;
    }
    
    
    
    
    
    //根据对应的uuid与当前的父级id进行校验
    public List<MikuMineQuestionRecords> getOneRecordByuuIdAndQuestionId(String uuid,Long pid){
    	MikuMineQuestionRecordsExample mikuMineQuestionRecordsExample=new MikuMineQuestionRecordsExample();
    	mikuMineQuestionRecordsExample.createCriteria().andUuidEqualTo(uuid).andParentQidEqualTo(pid);
    	List<MikuMineQuestionRecords> list=mikuMineQuestionRecordsMapper.selectByExample(mikuMineQuestionRecordsExample);
    	return list;
    }
    
    
    //根据的对应questionid与next-questionId
    public List<MikuMineQuestionsRoute> getTargetRoute(Long questionId,Long nextQuestionId){
    	MikuMineQuestionsRouteExample mikuMineQuestionsRouteExample=new MikuMineQuestionsRouteExample();
    	mikuMineQuestionsRouteExample.createCriteria().andQuestionIdEqualTo(questionId).andOptionNextskipQuestionIdEqualTo(nextQuestionId);
    	List<MikuMineQuestionsRoute> list=mikQuestionsRouteMapper.selectByExample(mikuMineQuestionsRouteExample);
    	return list;
    }
    
    
    
    
    public List<MikuMineCqQuestions> getTheQuestions(Long index){
    	MikuMineCqQuestionsExample mikuMineCqQuestionsExample=new MikuMineCqQuestionsExample();
    	mikuMineCqQuestionsExample.createCriteria().andIdEqualTo(index);
    	List<MikuMineCqQuestions> list=mikuMineCqQuestionsMapper.selectByExample(mikuMineCqQuestionsExample);
    	return list;
    }
    
    
    //根据对应的参数进行查找对应的记录信息
    public List<MikuMineQuestionRecords>  getOneRecord(Long userId,Long questionId){
    	MikuMineQuestionRecordsExample mikuMineQuestionRecordsExample=new MikuMineQuestionRecordsExample();
    	mikuMineQuestionRecordsExample.createCriteria().andUserIdEqualTo(userId).andQuestionIdEqualTo(questionId);
    	List<MikuMineQuestionRecords> list=mikuMineQuestionRecordsMapper.selectByExample(mikuMineQuestionRecordsExample);
    	return list;
    }
    
    //根据路由的节点信息来获取对应的下一条题目的基本信息
    public List<MikuMineQuestionsRoute> getOneRoute(Long index){
    	MikuMineQuestionsRouteExample mikuMineQuestionsRouteExample=new MikuMineQuestionsRouteExample();
    	mikuMineQuestionsRouteExample.createCriteria().andOptionIdEqualTo(index);
    	List<MikuMineQuestionsRoute> list=mikQuestionsRouteMapper.selectByExample(mikuMineQuestionsRouteExample);
    	return list;
    }
    
    
   
    //根据选项来进行获取对应的题目
    public List<MikuMineCqQuestions> getQuestionsById(Long id){
    	MikuMineCqQuestionsExample mikuMineCqQuestionsExample=new MikuMineCqQuestionsExample();
    	mikuMineCqQuestionsExample.createCriteria().andIdEqualTo(id);
    	List<MikuMineCqQuestions> list=mikuMineCqQuestionsMapper.selectByExample(mikuMineCqQuestionsExample);
    	return list;
    }
    
    //根据题目的id来进行获取的对应的选项内容
    public List<MikuMineQuestionOptions>  getOptionsByQuestionsId(Long id){
    	MikuMineQuestionOptionsExample mikuMineQuestionOptionsExample=new MikuMineQuestionOptionsExample();
    	mikuMineQuestionOptionsExample.createCriteria().andQuestionIdEqualTo(id);
    	List<MikuMineQuestionOptions> list=mikuMineQuestionOptionsMapper.selectByExample(mikuMineQuestionOptionsExample);
    	return list;
    }
    
    
    //根据用户的id来进行获取对应最后一次的调查问卷
    public List<MikuMineQuestionRecords> selectFinalRecordByUid(Long uid,Long type){
    	MikuMineQuestionRecordsExample mikuMineQuestionRecordsExample=new MikuMineQuestionRecordsExample();
    	mikuMineQuestionRecordsExample.createCriteria().andUserIdEqualTo(uid).andQuestionIdEqualTo(type);
    	List<MikuMineQuestionRecords> list=mikuMineQuestionRecordsMapper.selectByExample(mikuMineQuestionRecordsExample);
    	return list;
//    	Map map=new HashMap<>();
//    	map.put("uid", uid);
//    	map.put("type", type);
//    	return mikuMineCqQuestionsMapper.selectFinalRecordByUid(map);
    }
    
    
    //根据对应的option的id来找到对应的答案
    public List<MikuMineQuestionOptions> getOneOptionsById(Long id){
    	MikuMineQuestionOptionsExample mikuMineQuestionOptionsExample=new MikuMineQuestionOptionsExample();
    	mikuMineQuestionOptionsExample.createCriteria().andIdEqualTo(id);
    	List<MikuMineQuestionOptions> list=mikuMineQuestionOptionsMapper.selectByExample(mikuMineQuestionOptionsExample);
    	return list;
    }
    
	//进行获取对应的商品列表
	public List<Item> getItemList(){
		ItemExample itemExample=new ItemExample();
		itemExample.createCriteria().andApproveStatusEqualTo((byte)1).andIdGreaterThan(13850L).andApproveStatusEqualTo((byte)1);
		List<Item> list=itemMapper.selectByExample(itemExample);
		return list;
	}
	
	
	
	//获取全部数据【路由信息】
	public  List<MikuMineQuestionsRoute>  getAllData(){
		MikuMineQuestionsRouteExample mikuMineQuestionsRouteExample=new MikuMineQuestionsRouteExample();
    	List<MikuMineQuestionsRoute> list=mikQuestionsRouteMapper.selectByExample(mikuMineQuestionsRouteExample);
    	return list;
	}
	
	
	//获取的是第一道必答的题目
	public List<MikuMineCqQuestions>  getFirstQuestions(Byte type){
//		MikuMineCqQuestionsExample mikuMineCqQuestionsExample=new MikuMineCqQuestionsExample();
//    	mikuMineCqQuestionsExample.createCriteria().andQuestionFirstEqualTo((byte) 1).andQuestionnaireIdEqualTo(1L);
//    	List<MikuMineCqQuestions> list=mikuMineCqQuestionsMapper.selectByExample(mikuMineCqQuestionsExample);
		Map map=new HashMap();
		map.put("firstId", type);
    	List<MikuMineCqQuestions> list=mikuMineCqQuestionsMapper.selectQuestionByShowOrder(map);
    	return list;
	}
	
	
	//根据字级的id来查找对应的父级的节点
	public  List<MikuMineQuestionsRoute>  selectParentBychildId(Long childId){
		MikuMineQuestionsRouteExample mikuMineQuestionsRouteExample=new MikuMineQuestionsRouteExample();
		mikuMineQuestionsRouteExample.createCriteria().andOptionNextskipQuestionIdEqualTo(childId);
    	List<MikuMineQuestionsRoute> list=mikQuestionsRouteMapper.selectByExample(mikuMineQuestionsRouteExample);
    	return list;
	}
	
	
	
	
	
	//对树的深度遍历
	public void deptSearth(NodePoint nodepoint,List<NodePoint> list){
		if(list.contains(nodepoint)) return ;
		list.add(nodepoint);
		System.out.println("遍历的节点是:"+nodepoint.word);
		for(int i=0;i<nodepoint.line.size();i++){
			deptSearth(nodepoint.line.get(i).end,list);
		}
	}
	
	
	
	//获取的是第一级节点信息
	public List<NodePoint> getOneFirstNodeInfo(Byte type){
		List<NodePoint> NodeList=new ArrayList<NodePoint>();
		List<MikuMineCqQuestions>  firstlist= getFirstQuestions(type);
		NodePoint begin=new NodePoint("0");
		NodeList.add(begin);
		for(int i=0;i<firstlist.size();i++){
			MikuMineCqQuestions quesiton=firstlist.get(i);
			NodePoint oneNode=new NodePoint(quesiton.getId()+"");
			oneNode.question=quesiton;
			NodeList.add(oneNode);
		}
		return NodeList;
	}
	
	
	
	
	
	
	//建立对应的图的关系数据的关系的准备
	public String getReadyData(Byte type){
		//建立首个节点  再进行建立 关系
		List<MikuMineCqQuestions>  firstlist= getFirstQuestions(type);
		NodePoint begin=new NodePoint("0");
		List<NodePoint> NodeList=new ArrayList<NodePoint>();
		for(int i=0;i<firstlist.size();i++){
			MikuMineCqQuestions quesiton=firstlist.get(i);
			NodePoint oneNode=new NodePoint(quesiton.getId()+"");
			oneNode.question=quesiton;
			oneNode.qid=quesiton.getId();
			oneNode.optionId=0L;
			oneNode.optionslist=getOptionsByQuestionsId(quesiton.getId());
			System.err.println("===="+JSON.toJSONString(getOptionsByQuestionsId(quesiton.getId())));
			ArcTline oneline=new ArcTline(begin,oneNode);
			begin.line.add(oneline);
			NodeList.add(oneNode);
		}
		
		
		for(int k=0;k<NodeList.size();k++){
//			System.out.println(NodeList.get(k).word+"---->"+NodeList.get(k).question.getQuestionName());
		}
		//进行第二级建立关系
		//全部的路由信息
		
		List<MikuMineQuestionsRoute> rlist= getAllData();
		for(int j=0;j<rlist.size();j++){
			MikuMineQuestionsRoute oneroute=rlist.get(j);
			for(int z=0;z<NodeList.size();z++){
				NodePoint oneNode=NodeList.get(z);
				if((oneroute.getQuestionId()+"").equals(oneNode.word)){
					NodePoint roneNode=new NodePoint(oneroute.getOptionNextskipQuestionId()+"");
					roneNode.question=getQuestionsById(oneroute.getOptionNextskipQuestionId()).get(0);
//					System.out.println("------------------------->"+roneNode.question.getQuestionName()+"<---------------------");
					roneNode.qid=roneNode.question.getId();
					roneNode.optionId=oneroute.getOptionId();
					roneNode.optionslist=getOptionsByQuestionsId(roneNode.qid);
					ArcTline oneline=new ArcTline(oneNode,roneNode);
					oneNode.line.add(oneline);
				}
			}
		}
		
		//进行遍历的操作
		List<NodePoint> allNodeList=new ArrayList<NodePoint>();
		deptSearth(begin,allNodeList);
		for(int k=1;k<allNodeList.size();k++){
			NodePoint node=allNodeList.get(k);
			System.out.println("id值:"+node.word+"       分支个数"+node.line.size());
			if(node!=null){
				System.out.println("问题名称:"+node.question.getQuestionName());
				System.err.println("选项是:"+JSON.toJSONString(node.optionslist));
			}
//			System.out.println(allNodeList.get(k).word+"---->"+allNodeList.get(k).question.getQuestionName());
		}
		return JSON.toJSONString(allNodeList);
	}
	
	
	
	
	
	
	
	//==================================报告生成===================================================
//		public Map InsertOneReportData(String recordId,String userInfo,String picUrls,String skinInfo,String suggestionInfo,Long uid,Long serviceId,String money, Long questionnaireId,String flag,Long rid){
//			MikuMineDetectReport mikuMineDetectReport=new MikuMineDetectReport();
//			mikuMineDetectReport.setDateCreated(new Date());
//			mikuMineDetectReport.setLastUpdated(new Date());
////			mikuMineDetectReport.setMoney(money);
//			mikuMineDetectReport.setPicUrls(picUrls);
////			mikuMineDetectReport.setRecordId(recordId);
//			mikuMineDetectReport.setUserId(uid);
//			mikuMineDetectReport.setVersion(1L);
////			mikuMineDetectReport.setUserInfo(userInfo);
////			mikuMineDetectReport.setSkinInfo(skinInfo);
////			mikuMineDetectReport.setSuggestionInfo(suggestionInfo);
//			mikuMineDetectReport.setServiceId(serviceId);
////			mikuMineDetectReport.setQuestionnaireId(questionnaireId);
//			if("0".equals(flag)){
//				mikuMineDetectReportMapper.insert(mikuMineDetectReport);
//			}
////			else{
////				mikuMineDetectReport.setId(rid);
////				mikuMineDetectReportMapper.updateByPrimaryKeyWithBLOBs(mikuMineDetectReport);
////			}
//			Map map=new HashMap<>();
//			map.put("data", mikuMineDetectReport);
//			return map;
//		}
		
		
		
		
		
		
		
		
		
		public List<MikuMineDetectReport> getDataByUid(Long uid){
			MikuMineDetectReportExample mikuMineDetectReportExample=new MikuMineDetectReportExample();
			mikuMineDetectReportExample.createCriteria().andUserIdEqualTo(uid);
			List<MikuMineDetectReport> list=mikuMineDetectReportMapper.selectByExample(mikuMineDetectReportExample);
	    	return list;
		}
		
		
		
		
//		public MikuMineDetectReport getDataByrid(Long reportId){
////			MikuMineDetectReportExample mikuMineDetectReportExample=new MikuMineDetectReportExample();
////			mikuMineDetectReportExample.createCriteria().andIdEqualTo(reportId);
////			List<MikuMineDetectReport> list=mikuMineDetectReportMapper.selectByExampleWithBLOBs(mikuMineDetectReportExample);
////	    	return list.get(0);
//		}
		
		
		public MikuCsadDO getOneZjData(Long zjid){
			MikuCsadDOExample mikuCsadDOExample=new MikuCsadDOExample();
			mikuCsadDOExample.createCriteria().andUserIdEqualTo(zjid);
			List<MikuCsadDO> zjlist=mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
			return zjlist.get(0);
		}
	
	

}





//进行图的深度遍历
//自己定义的点
class NodePoint{
	List<ArcTline> line;
	MikuMineCqQuestions question;
	String word;
	Long qid;
	Long optionId;
	List<MikuMineQuestionOptions> optionslist;
	public NodePoint(String word) {
		this.word = word;
		line=new ArrayList<ArcTline>();
	}
}


//单个图点的关系
class ArcTline{
	NodePoint start,end;
	public ArcTline(NodePoint start, NodePoint end) {
		this.start = start;
		this.end = end;
	}
}












