package com.welink.web.resource.customized;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.DoMikuMeasureLogService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.MikuMineCqQuestionByChangeService;
import com.welink.biz.service.MikuMineSkinQuestionService;
import com.welink.commons.domain.MikuMineDetectReport;
import com.welink.commons.domain.MikuMineQuestionnaireRecordsDO;
import com.welink.commons.domain.MikuMineScProblemItemDO;
import com.welink.commons.domain.QuestionAndOptionsModel;
import com.welink.commons.persistence.MikuInstrumentMeasureLogDOMapper;
import com.welink.commons.persistence.MikuMineDetectReportDOMapper;
import com.welink.commons.persistence.MikuMineIqrecordsDOMapper;
import com.welink.commons.persistence.MikuMineQoptionsDOMapper;
import com.welink.commons.persistence.MikuMineQuestionRecordsMapper;
import com.welink.commons.persistence.MikuMineQuestionnaireRecordsDOMapper;
import com.welink.commons.persistence.MikuMineQuestionsDOMapper;
import com.welink.commons.persistence.MikuMineScProblemItemDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.vo.DetectReportTradeVO;


@RestController
public class MikuMineCqQuestionAction {

	//操作的是单表
	@Resource
	private MikuMineCqQuestionByChangeService mikuMineCqQuestionByChangeService;
    
	@Resource
    private MikuMineQuestionnaireRecordsDOMapper mikuMineQuestionnaireRecordsDOMapper;
    
	@Resource
	private MikuMineScProblemItemDOMapper mikuMineScProblemItemDOMapper;
	
	//=============================================开发接口进行书写=============================================================
    
	  //需求分析:对用户标示
	  @RequestMapping(value = {"/api/m/1.0/ineserOneRcordsTogiveParam.json", "/api/h/1.0/ineserOneRcordsTogiveParam.json"}, produces = "application/json;charset=utf-8")
	  public String ineserOneRcordsTogiveParam(HttpServletRequest request, HttpServletResponse response) throws Exception {
		  Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):0+""));
		  //插入数据
		  MikuMineQuestionnaireRecordsDO one=mikuMineCqQuestionByChangeService.insertOneData(userId);
		  //返回选项  
		  List<MikuMineScProblemItemDO> options= mikuMineCqQuestionByChangeService.getAllProblemItems();
	      WelinkVO welinkVO = new WelinkVO();
	      Map map=new HashMap<>();
	      map.put("record", one);
	      map.put("options", options);
	      welinkVO.setResult(map);
	      welinkVO.setStatus(1);
	      return JSON.toJSONString(welinkVO);
	  }
	  
	  
	  
	  
	    //答完基本题目插入对应的答案
	    @RequestMapping(value = {"/api/m/1.0/insertOneRecordByParams.json", "/api/h/1.0/insertOneRecordByParams.json"}, produces = "application/json;charset=utf-8")
	    public String insertOneRecordByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	//插入与更新的标示【0更新  1插入】
	    	Byte datadoflag=Byte.parseByte((String) (request.getParameter("datadoflag")!=null?request.getParameter("datadoflag"):0+""));
	    	Byte skinType=Byte.parseByte((String) (request.getParameter("skinType")!=null?request.getParameter("skinType"):0+""));
	    	Byte envArea=Byte.parseByte((String) (request.getParameter("envArea")!=null?request.getParameter("envArea"):0+""));
	    	Byte season=Byte.parseByte((String) (request.getParameter("season")!=null?request.getParameter("season"):0+""));
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):0+""));
	    	String questionnaireRecordsName=request.getParameter("questionnaireRecordsName");
	    	String scProblemIds=request.getParameter("scProblemIds");
	    	String ageRegion=request.getParameter("ageRegion");
	    	String age=request.getParameter("age");
	    	String sex=request.getParameter("sex");
	    	String skinColor=request.getParameter("skinColor");
	    	String baskDegree=request.getParameter("baskDegree");
	    	String skinSensitive=request.getParameter("skinSensitive");
	    	String skinSensitiveFrequency=request.getParameter("skinSensitiveFrequency");
	    	String skinSensitiveDegree=request.getParameter("skinSensitiveDegree");
	    	String skinRedness=request.getParameter("skinRedness");
	    	String skinRednessDegree=request.getParameter("skinRednessDegree");
	    	String stressDegree=request.getParameter("stressDegree");
	    	String liveEnv=request.getParameter("liveEnv");
	    	String sleepTime=request.getParameter("sleepTime");
	    	String envCity=request.getParameter("envCity");
	    	String detailedProblemRecordsId=request.getParameter("detailedProblemRecordsId");
	    	
	    	MikuMineQuestionnaireRecordsDO one=new MikuMineQuestionnaireRecordsDO();
	    	one.setVersion(2L);
	    	
	    	one.setQuestionnaireRecordsName(questionnaireRecordsName);
	    	one.setScProblemIds(scProblemIds);
	    	one.setAge(age);
	    	one.setAgeRegion(ageRegion);
	    	one.setSex(sex);
	    	one.setSkinColor(skinColor);
	    
	    	one.setSkinType(skinType);
	    	one.setBaskDegree(baskDegree);
	    	one.setSkinSensitiveDegree(skinSensitiveDegree);
	    	one.setSkinSensitive(skinSensitive);
	    	one.setSkinRedness(skinRedness);
	    	one.setSkinRednessDegree(skinRednessDegree);
	    	one.setStressDegree(stressDegree);
	    	one.setLiveEnv(liveEnv);
	    	one.setSleepTime(sleepTime);
	    	one.setEnvArea(envArea);
	    	one.setEnvCity(envCity);
	    	one.setSeason(season);
	    	one.setDetailedProblemRecordsId(detailedProblemRecordsId);
	    	one.setSkinSensitiveFrequency(skinSensitiveFrequency);
	    	one.setLastUpdated(new Date());
	    	one.setUserId(userId);
	    	//单纯的插入一条数据
	    	if(datadoflag == (byte)1){
	    		mikuMineQuestionnaireRecordsDOMapper.insert(one);
	    	}else{
	    		Long id= Long.parseLong((String) (request.getParameter("id")!=null?request.getParameter("id"):0+""));
	    		one.setId(id);
	    		mikuMineQuestionnaireRecordsDOMapper.updateByPrimaryKey(one);
	    	}
	    	
	    	WelinkVO welinkVO = new WelinkVO();
	    	//加多一个判断的标示：是否是专家与用户
	    	String flag=request.getParameter("flag");
	    	Map map=new HashMap<>();
	    	
	    	Map scmap=new HashMap<>();
	    	scmap.put("ids", one.getScProblemIds());
			List<MikuMineScProblemItemDO> list=mikuMineScProblemItemDOMapper.selectscOptionsByIDs(scmap);
			String str="";
			for(int j=0;j<list.size();j++){
				str+=(list.get(j).getScProblemName()+" ");
			}
			one.setScProblemIds(str);
	    	
	    	
	    	map.put("record", one);
	    	if("1".equals(flag)){
	    		 map.put("type","1");
	    		 //根据的ids的集合：查找对应的选择项的内容
	    		 List<QuestionAndOptionsModel> options=mikuMineCqQuestionByChangeService.getOneQuestionDataById(scProblemIds);
	    		 map.put("data",options);
	    	}else{
	    		 map.put("type","0");
	    	}
	    	welinkVO.setResult(map);
	    	welinkVO.setStatus(1);
	        return JSON.toJSONString(welinkVO);
	    }
	  
	    
	    
	    
	    //进行提交对应的选填答案
	    @RequestMapping(value = {"/api/m/1.0/insertOneOtherCRecordByParams.json", "/api/h/1.0/insertOneOtherCRecordByParams.json"}, produces = "application/json;charset=utf-8")
	    public String insertOneOtherCRecordByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):0+""));
	    	//qid1$id1;id2;id3|qid2$id11;id12;id13
	        //1$1|23$66
	    	String answer=request.getParameter("answer");
	    	Long recordId= Long.parseLong((String) (request.getParameter("recordId")!=null?request.getParameter("recordId"):0+""));
	    	//进行插入细化的问题
	    	mikuMineCqQuestionByChangeService.InsertMoreDataMytargetContent(answer,userId,recordId);
	    	Map mymap= mikuMineCqQuestionByChangeService.getOneAllRecordsData(recordId);
	    	//然后再进行拿出对应的报告值
	    	WelinkVO welinkVO = new WelinkVO();
	    	welinkVO.setStatus(1);
	    	welinkVO.setResult(mymap);
	        return JSON.toJSONString(welinkVO);
	    }
    
    
	    
	    //判断专家是否答题完成
	    //以下情况：
	    //1.完全是请求第一道题
	    //2.用户已经答完基本必答题
	    //3.生成对应的调查报告
	    @RequestMapping(value = {"/api/m/1.0/judgeByZjFlag.json", "/api/h/1.0/judgeByZjFlag.json"}, produces = "application/json;charset=utf-8")
	    public String judgeByZjFlag(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):0+""));
	    	List<MikuMineQuestionnaireRecordsDO> allrecords=mikuMineCqQuestionByChangeService.selectAllRecordsByUserId(userId);
	    	Map map=new HashMap<>();
	    	WelinkVO welinkVO = new WelinkVO();
	    	welinkVO.setStatus(1);
	    	 //返回选项  
	  		  List<MikuMineScProblemItemDO> options= mikuMineCqQuestionByChangeService.getAllProblemItems();
	  		  map.put("options", options);
	    	//专家全做
	    	if(allrecords.size()==0){
	  		  MikuMineQuestionnaireRecordsDO one=mikuMineCqQuestionByChangeService.insertOneData(userId);
	  	      map.put("record", one);
	  	      map.put("type", "0");
	    	}
	    	//专家帮填
	    	else if(allrecords.size()>0){
	    		MikuMineQuestionnaireRecordsDO finalrecord=allrecords.get(allrecords.size()-1);
	    		map.put("record", finalrecord);
	    		map.put("type", "0");
	    		//判断专家没有帮填
	    		if(finalrecord.getScProblemIds()!=null && finalrecord.getDetailedProblemRecordsId()==null){
	    			 //根据的ids的集合：查找对应的选择项的内容
		    		 map.put("data",options);
		    		 map.put("type", "1");
	    		}
	    		//生成记录问题
	    		else if(finalrecord.getScProblemIds()!=null && finalrecord.getDetailedProblemRecordsId()!=null){
	    			 map= mikuMineCqQuestionByChangeService.getOneAllRecordsData(finalrecord.getId());
	    			 map.put("type", "2");
	    		}
	    	}
	    	welinkVO.setResult(map);
	        return JSON.toJSONString(welinkVO);
	    }
    
    
    
    
		//需求分析:入口一用户进入调查问卷
	    @RequestMapping(value = {"/api/m/1.0/selectQuestionsByUserId.json", "/api/h/1.0/selectQuestionsByUserId.json"}, produces = "application/json;charset=utf-8")
	    public String selectQuestionsByUserId(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):0+""));
	    	List<MikuMineQuestionnaireRecordsDO> allrecords=mikuMineCqQuestionByChangeService.selectAllRecordsByUserId(userId);
	    	//根据userId来获取对应所有的
	    	WelinkVO welinkVO = new WelinkVO();
	    	welinkVO.setStatus(1);
	    	Map map=new HashMap<>();
	    	//返回选项  
	  		List<MikuMineScProblemItemDO> options= mikuMineCqQuestionByChangeService.getAllProblemItems();
	  		map.put("options", options);
	  		MikuMineQuestionnaireRecordsDO one=new MikuMineQuestionnaireRecordsDO();
	    	//用户没有做的
	  		map.put("type", "0");
	    	if(allrecords.size()==0){
	    		  one=mikuMineCqQuestionByChangeService.insertOneData(userId);
	    	}else{
	    		one=allrecords.get(allrecords.size()-1);
	    		Map scmap=new HashMap<>();
	    		if(one.getScProblemIds()!=null){
	    			map.put("ids", one.getScProblemIds());
			    	scmap.put("ids", one.getScProblemIds());
					List<MikuMineScProblemItemDO> list=mikuMineScProblemItemDOMapper.selectscOptionsByIDs(scmap);
					String str="";
					for(int j=0;j<list.size();j++){
						str+=(list.get(j).getScProblemName()+" ");
					}
					one.setScProblemIds(str);
					map.put("type", "1");
	    		}
	    	}
	    	map.put("record", one);
	    	welinkVO.setResult(map);
	        return JSON.toJSONString(welinkVO);
	    }
	    
	    
	    
	    
	    //App端这边展示的是
	    //专家端这边判断的标示
	    @RequestMapping(value = {"/api/m/1.0/judgeByZjFlagAndApp.json", "/api/h/1.0/judgeByZjFlagAndApp.json"}, produces = "application/json;charset=utf-8")
	    public String judgeByZjFlagAndApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):0+""));
	    	List<MikuMineQuestionnaireRecordsDO> allrecords=mikuMineCqQuestionByChangeService.selectAllRecordsByUserId(userId);
	    	Map map=new HashMap<>();
	    	WelinkVO welinkVO = new WelinkVO();
	    	welinkVO.setStatus(1);
	    	map.put("type", "0");
	    	//专家帮填
	    	if(allrecords.size()>0){
	    		MikuMineQuestionnaireRecordsDO finalrecord=allrecords.get(allrecords.size()-1);
	    		map.put("record", finalrecord);
	    		//判断专家没有帮填
	    		if(finalrecord.getScProblemIds()!=null && finalrecord.getDetailedProblemRecordsId()==null){
	    			 //根据的ids的集合：查找对应的选择项的内容
		    		 map.put("type", "1");
	    		}
	    		//生成记录问题
	    		else if(finalrecord.getScProblemIds()!=null && finalrecord.getDetailedProblemRecordsId()!=null){
	    			 map= mikuMineCqQuestionByChangeService.getOneAllRecordsData(finalrecord.getId());
	    			 map.put("type", "2");
	    		}
	    	}
	    	welinkVO.setResult(map);
	        return JSON.toJSONString(welinkVO);
	    }
	    
    
    
    
	    
		//插入一个报告
	    //更新与插入操作
	    @RequestMapping(value = {"/api/m/1.0/insertOneReportByUserId.json", "/api/h/1.0/insertOneReportByUserId.json"}, produces = "application/json;charset=utf-8")
	    public String insertOneReportByUserId(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	String recordId=request.getParameter("recordId");
	    	String userInfo=request.getParameter("userInfo");
	    	String picUrls=request.getParameter("picUrls");
	    	String suggestionInfo=request.getParameter("suggestionInfo");
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):1+""));
	    	Long serviceId= Long.parseLong((String) (request.getParameter("serviceId")!=null?request.getParameter("serviceId"):1+""));
	    	String flag=request.getParameter("flag")!=null?"1":"0";
	    	Long rid= Long.parseLong((String) (request.getParameter("rid")!=null?request.getParameter("rid"):1+""));
	        Map map=mikuMineCqQuestionByChangeService.InsertOneReportData(recordId,userInfo,picUrls,suggestionInfo,userId,serviceId,flag,rid);
	        WelinkVO welinkVO = new WelinkVO();
	    	welinkVO.setResult(map);
	    	welinkVO.setStatus(1);
	    	return JSON.toJSONString(welinkVO);
	    }
	    
	    
	    
//	    long profileId = -1;
//    	WelinkVO welinkVO = new WelinkVO();
//    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
//    	Session session = currentUser.getSession();
//    	profileId = (long) session.getAttribute("profileId");
//    	if (profileId < 0) {
//            welinkVO.setStatus(0);
//            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
//            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
//            return JSON.toJSONString(welinkVO);
//        }
	    
	    
	    //查找对应用户的调查报告
	    @RequestMapping(value = {"/api/m/1.0/getOneUserReports.json", "/api/h/1.0/getOneUserReports.json"}, produces = "application/json;charset=utf-8")
	    public String getOneUserReports(HttpServletRequest request, HttpServletResponse response) throws Exception {
//	    	long profileId = -1;
	    	WelinkVO welinkVO = new WelinkVO();
//	    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
//	    	Session session = currentUser.getSession();
//	    	profileId = (long) session.getAttribute("profileId");
//	    	if (profileId < 0) {
//	            welinkVO.setStatus(0);
//	            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
//	            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
//	            return JSON.toJSONString(welinkVO);
//	        }
	    	Long userId= Long.parseLong((String) (request.getParameter("userId")!=null?request.getParameter("userId"):1+""));
	    	List<MikuMineDetectReport> list= mikuMineCqQuestionByChangeService.getDataByUid(userId);
	    	Map map=new HashMap<>();
	    	if(list.size()>0){
	    		 MikuMineDetectReport report=list.get(list.size()-1);
	    		 map= mikuMineCqQuestionByChangeService.getOneAllRecordsData(report.getQuestionnaireRecordsId());
    			 map.put("falg", "1");
	    	}else{
	    		map.put("falg", "0");
	    	}
	    	welinkVO.setResult(map);
	    	welinkVO.setStatus(1);
	    	return JSON.toJSONString(welinkVO);
	    }
	    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//	  //需求分析:对查询的问题与选项进行全部进行缓存的操作[从缓存当中进行拿出全部的数据]
//	    @RequestMapping(value = {"/api/m/1.0/selectQuestionsByOptions.json", "/api/h/1.0/selectQuestionsByOptions.json"}, produces = "application/json;charset=utf-8")
//	    public String selectQuestionsByOptions(HttpServletRequest request, HttpServletResponse response) throws Exception {
//	    	WelinkVO welinkVO = new WelinkVO();
//	    	Map map=mikuMineCqQuestionByChangeService.getAllData();
//	    	welinkVO.setResult(map);
//	    	welinkVO.setStatus(1);
//	        return JSON.toJSONString(welinkVO);
//	    }
	    
    
    
    
    
//	//需求分析:对查询的问题与选项进行全部进行缓存的操作[永久的缓存]
//    @RequestMapping(value = {"/api/m/1.0/ineserAllDataToMemcach.json", "/api/h/1.0/ineserAllDataToMemcach.json"}, produces = "application/json;charset=utf-8")
//    public String ineserAllDataToMemcach(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	//永久的缓存
//    	
//        return "";
//    }
//    
//    
//    
//    
//    
//    //需求分析:对查询的问题与选项进行全部进行缓存的操作[从缓存当中进行拿出全部的数据]
//    @RequestMapping(value = {"/api/m/1.0/selectQuestionsByMemcach.json", "/api/h/1.0/selectQuestionsByMemcach.json"}, produces = "application/json;charset=utf-8")
//    public String selectQuestionsByMemcach(HttpServletRequest request, HttpServletResponse response) throws Exception {
//
//        return "";
//    }
//    
//    
//    
//   //需求分析:对查询的问题与选项进行全部进行缓存的操作[从缓存当中进行拿出全部的数据]
//    @RequestMapping(value = {"/api/m/1.0/insertOneRecordsByAllQuestionAndOptions.json", "/api/h/1.0/insertOneRecordsByAllQuestionAndOptions.json"}, produces = "application/json;charset=utf-8")
//    public String insertOneRecordsByAllQuestionAndOptions(HttpServletRequest request, HttpServletResponse response) throws Exception {
//
//        return "";
//    }
    
   
	


    //=============================================测试接口进行书写=============================================================
    //作答并获取的是题目的获取
//    @RequestMapping(value = {"/api/m/1.0/ineserAllDataToMemcachTest.json", "/api/h/1.0/ineserAllDataToMemcachTest.json"}, produces = "application/json;charset=utf-8")
//    public String ineserAllDataToMemcachTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	
//        return "";
//    }
    
   
        
}
