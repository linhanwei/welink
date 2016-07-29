package com.welink.web.resource;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.mysql.fabric.xmlrpc.base.Params;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.BCrypt;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.service.DoMikuMeasureLogService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.MikuMineSkinQuestionService;
import com.welink.biz.service.UserService;
import com.welink.biz.service.RedPacketService;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.TimeUtil;
import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuInstrumentMeasureLogDO;
import com.welink.commons.domain.MikuMineCqQuestions;
import com.welink.commons.domain.MikuMineDetectReport;
import com.welink.commons.domain.MikuMineQuestionOptions;
import com.welink.commons.domain.MikuMineQuestionRecords;
import com.welink.commons.domain.MikuMineSkincareSuggestion;
import com.welink.commons.domain.MikuOperMeaureData;
import com.welink.commons.domain.MikuOperMeaureDetail;
import com.welink.commons.domain.MikuOperMeaureListData;
import com.welink.commons.domain.MikuRedPackSettingDO;
import com.welink.commons.domain.MikuRedPackSettingDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.RedPacket;
import com.welink.commons.persistence.MikuInstrumentMeasureLogDOMapper;
import com.welink.commons.persistence.MikuLogsDOMapper;
import com.welink.commons.persistence.MikuRedPackSettingDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.ProfileWeChatDOMapper;
import com.welink.commons.persistence.WeChatProfileDOMapper;
import com.welink.web.common.constants.ResponseMSGConstans;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class MikuMineSkinQuestions {

	//操作的是单表
	@Resource
	private MikuInstrumentMeasureLogDOMapper mikuInstrumentMeasureLogDOMapper;
	
	@Resource
	private DoMikuMeasureLogService doMikuMeasureLogService;
	
	@Resource
	private MikuMineSkinQuestionService mikuMineSkinQuestionService;
	
	@Resource
	private ItemService itemService;
	
	//==========================================开发=================================================================
	 //作答并获取的是题目的获取
    @RequestMapping(value = {"/api/m/1.0/selectByNextquestion.json", "/api/h/1.0/selectByNextquestion.json"}, produces = "application/json;charset=utf-8")
    public String selectByNextquestion(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = -1;
    	WelinkVO welinkVO = new WelinkVO();
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
    	Session session = currentUser.getSession();
    	profileId = (long) session.getAttribute("profileId");
    	if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
    	//2个参数:时间日期【Y M W D】
    	String timetype = request.getParameter("timetype");
    	List<MikuOperMeaureData> list=doMikuMeasureLogService.getOperaterListByAvgParams(profileId, timetype);
    	Map map=new HashMap();
    	map.put("list", list);
    	welinkVO.setResult(map);
        return JSON.toJSONString(welinkVO);
    }
    
	
	 @RequestMapping(value = {"/api/m/1.0/selectByOneskinResult.json", "/api/h/1.0/selectByOneskinResult.json"}, produces = "application/json;charset=utf-8")
	    public String selectByOneskinResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	long profileId = -1;
	    	WelinkVO welinkVO = new WelinkVO();
	    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
	    	Session session = currentUser.getSession();
	    	profileId = (long) session.getAttribute("profileId");
	    	if (profileId < 0) {
	            welinkVO.setStatus(0);
	            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
	            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
	            return JSON.toJSONString(welinkVO);
	        }
	    	//类型   题目的类型
	    	Long index= Long.parseLong(request.getParameter("index"));
	    	//根据是否是第一题来判断
	    	//是第一题的话则直接查出对应的题目  否则是插入对应的答案然后再进行插入对应的答案
	    	Byte onetype=Byte.parseByte((String) ((request.getParameter("type")) != null?request.getParameter("type"):1+""));
	    	//如果不是接收第一道题的话则需要传对应答案过来，并且给答案的id
	    	Long optionId= Long.parseLong(request.getParameter("optionId"));
	    	Long questionId= Long.parseLong(request.getParameter("questionId"));
	    	String value=request.getParameter("value");
	     	String flag=request.getParameter("flag");
	     	Map map=mikuMineSkinQuestionService.getQuestionByIndexTest(index,optionId,questionId,1L,onetype,value,flag);
	    	welinkVO.setResult(map);
	        return JSON.toJSONString(welinkVO);
	    }
	    
	
    //=============================================测试接口进行书写=============================================================
    //作答并获取的是题目的获取
    @RequestMapping(value = {"/api/m/1.0/selectByNextquestionTest.json", "/api/h/1.0/selectByNextquestionTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByNextquestionTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//类型   题目的类型
    	Long index= Long.parseLong(request.getParameter("index"));
    	//根据是否是第一题来判断
    	//是第一题的话则直接查出对应的题目  否则是插入对应的答案然后再进行插入对应的答案
    	Byte onetype=Byte.parseByte((String) ((request.getParameter("type")) != null?request.getParameter("type"):1+""));
    	//如果不是接收第一道题的话则需要传对应答案过来，并且给答案的id
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String value=request.getParameter("value");
    	String flag=request.getParameter("flag");
    	Map map=mikuMineSkinQuestionService.getQuestionByIndexTest(index,optionId,questionId,1L,onetype,value,flag);
        return JSON.toJSONString(map);
    }
    
    
    
    
    
    //作答并获取的是题目的获取
    @RequestMapping(value = {"/api/m/1.0/selectByOneskinResultTest.json", "/api/h/1.0/selectByOneskinResultTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByOneskinResultTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//类型   题目的类型
//    	Long index= Long.parseLong(request.getParameter("index"));
//    	//根据是否是第一题来判断
//    	//是第一题的话则直接查出对应的题目  否则是插入对应的答案然后再进行插入对应的答案
//    	Byte onetype=Byte.parseByte((String) ((request.getParameter("type")) != null?request.getParameter("type"):1+""));
//    	//如果不是接收第一道题的话则需要传对应答案过来，并且给答案的id
//    	MikuMineCqQuestions model=mikuMineSkinQuestionService.getQuestionByIndex(index);
//    	List<MikuMineQuestionOptions> optionList=mikuMineSkinQuestionService.getOptionsByQuestionsId(model.getId());
//    	Map map=new HashMap<>();
//    	map.put("question", model);
//    	map.put("option", optionList);
//      return JSON.toJSONString(map);
    	return "";
    }
    
    
    
    
    
    
    //作答并获取的是题目的获取
    @RequestMapping(value = {"/api/m/1.0/selectByQuestionsOrderTest.json", "/api/h/1.0/selectByQuestionsOrderTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByQuestionsOrderTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//类型   题目的类型
    	return mikuMineSkinQuestionService.getReadyData((byte) 1);
    }
    
    
    
    
    //作答并获取的是题目的获取
//    @RequestMapping(value = {"/api/m/1.0/selectByAllQuestionsByOptionId.json", "/api/h/1.0/selectByAllQuestionsByOptionId.json"}, produces = "application/json;charset=utf-8")
//    public String selectByAllQuestionsByOptionId(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
//    	return mikuMineSkinQuestionService.getMoreQuestion(optionId);
//    }
//    
//    
//    
//    
//    //作答并获取的是题目的获取【获取的是前一次的optionId与questionId  后一次optionId】
//    @RequestMapping(value = {"/api/m/1.0/selectByoneQuqstionByLastQuestionAndOptionIds.json", "/api/h/1.0/selectByoneQuqstionByLastQuestionAndOptionIds.json"}, produces = "application/json;charset=utf-8")
//    public String selectByoneQuqstionByLastQuestionAndOptionIds(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
//    	Long lastoptionId= Long.parseLong((String) (request.getParameter("lastoptionId")!=null?request.getParameter("lastoptionId"):0+""));
//    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
//    	String uuid=request.getParameter("uuid");
//    	return mikuMineSkinQuestionService.getMoreQuestion(optionId);
//    }
    
    
    //测试
    //作答并获取的是题目的获取
    @RequestMapping(value = {"/api/m/1.0/finalselectByQuestionsTest.json", "/api/h/1.0/finalselectByQuestionsTest.json"}, produces = "application/json;charset=utf-8")
    public String finalselectByQuestionsTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String optionIds=request.getParameter("optionIds");
    	String flag=request.getParameter("flag");
    	String uuid=request.getParameter("uuid");
       	String deleteContent=request.getParameter("deleteContent");
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.selectByoneQuqstionByLastQuestionAndOptionIds(questionId,1L,(byte)1,optionIds,flag,uuid,deleteContent);
    	WelinkVO welinkVO = new WelinkVO();
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    //开发
    //作答并获取的是题目的获取
    @RequestMapping(value = {"/api/m/1.0/finalselectBydevQuestions.json", "/api/h/1.0/finalselectBydevQuestions.json"}, produces = "application/json;charset=utf-8")
    public String finalselectBydevQuestions(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId =Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):"-1"));
    	WelinkVO welinkVO = new WelinkVO();
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
    	Session session = currentUser.getSession();
    	if(profileId == (-1L)){
    		profileId = (long) session.getAttribute("profileId");
        	if (profileId < 0) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
    	}
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String optionIds=request.getParameter("optionIds");
    	String flag=request.getParameter("flag");
    	String uuid=request.getParameter("uuid");
    	String deleteContent=request.getParameter("deleteContent");
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.selectByoneQuqstionByLastQuestionAndOptionIds(questionId,profileId,(byte)1,optionIds,flag,uuid,deleteContent);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    //专家帮填的记录题目
    @RequestMapping(value = {"/api/m/1.0/finalselectByprofiessdevQuestions.json", "/api/h/1.0/finalselectByprofiessdevQuestions.json"}, produces = "application/json;charset=utf-8")
    public String finalselectByprofiessdevQuestions(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	Long profileId= Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):0+""));
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String optionIds=request.getParameter("optionIds");
    	String flag=request.getParameter("flag");
    	String uuid=request.getParameter("uuid");
    	String deleteContent=request.getParameter("deleteContent");
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.selectByoneQuqstionByLastQuestionAndOptionIds(questionId,profileId,(byte)1,optionIds,flag,uuid,deleteContent);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    
    //专家帮填提交最后一道题目
    @RequestMapping(value = {"/api/m/1.0/finalsubmitByprofiessdevQuestions.json", "/api/h/1.0/finalsubmitByprofiessdevQuestions.json"}, produces = "application/json;charset=utf-8")
    public String finalsubmitByprofiessdevQuestions(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	Long profileId= Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):0+""));
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String optionIds=request.getParameter("optionIds");
    	String flag=request.getParameter("flag");
    	String uuid=request.getParameter("uuid");
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.insertFinalAndPrintReort(questionId,profileId,(byte)1,optionIds,flag,uuid);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    
    
    
    
    
    
    
    //测试
    //作为最后的一道题提交并得出对应的结论
    @RequestMapping(value = {"/api/m/1.0/finalsubmitByQuestionsTest.json", "/api/h/1.0/finalsubmitByQuestionsTest.json"}, produces = "application/json;charset=utf-8")
    public String finalsubmitByQuestionsTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String optionIds=request.getParameter("optionIds");
    	String flag=request.getParameter("flag");
    	String uuid=request.getParameter("uuid");
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.insertFinalAndPrintReort(questionId,1L,(byte)1,optionIds,flag,uuid);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    //开发
    //作为最后的一道题提交并得出对应的结论
    @RequestMapping(value = {"/api/m/1.0/finalsubmitBydevQuestions.json", "/api/h/1.0/finalsubmitBydevQuestions.json"}, produces = "application/json;charset=utf-8")
    public String finalsubmitBydevQuestions(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId =Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):"-1"));
    	WelinkVO welinkVO = new WelinkVO();
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
    	Session session = currentUser.getSession();
    	if(profileId == (-1L)){
    		profileId = (long) session.getAttribute("profileId");
        	if (profileId < 0) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
    	}
    	Long optionId= Long.parseLong((String) (request.getParameter("optionId")!=null?request.getParameter("optionId"):0+""));
    	Long questionId= Long.parseLong((String) (request.getParameter("questionId")!=null?request.getParameter("questionId"):0+""));
    	String optionIds=request.getParameter("optionIds");
    	String flag=request.getParameter("flag");
    	String uuid=request.getParameter("uuid");
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.insertFinalAndPrintReort(questionId,profileId,(byte)1,optionIds,flag,uuid);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    
    
    //测试
    @RequestMapping(value = {"/api/m/1.0/getoneSkinAndItemsTest.json", "/api/h/1.0/getoneSkinAndItemsTest.json"}, produces = "application/json;charset=utf-8")
    public String getoneSkinAndItemsTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	//类型   题目的类型
    	Map map=mikuMineSkinQuestionService.getoneSkinRecord("内容0","内容");
    	map.put("itemlist",itemService.combineItemTags(mikuMineSkinQuestionService.getItemList()));
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    
    //开发
    @RequestMapping(value = {"/api/m/1.0/getoneSkinAndItems.json", "/api/h/1.0/getoneSkinAndItems.json"}, produces = "application/json;charset=utf-8")
    public String getoneSkinAndItems(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = -1;
    	WelinkVO welinkVO = new WelinkVO();
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
    	Session session = currentUser.getSession();
    	profileId = (long) session.getAttribute("profileId");
    	if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
    	Map map=mikuMineSkinQuestionService.getoneSkinRecord("内容0","内容");
    	map.put("itemlist",itemService.combineItemTags(mikuMineSkinQuestionService.getItemList()));
//    	map.put("itemlist", getItemList());
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    //根据对应的uuid来进行获取对应的结论
    @RequestMapping(value = {"/api/m/1.0/getoneSkinDataByUuidTest.json", "/api/h/1.0/getoneSkinDataByUuidTest.json"}, produces = "application/json;charset=utf-8")
    public String getoneSkinDataByUuidTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	String uuid=request.getParameter("uuid");
    	Map map=mikuMineSkinQuestionService.getFinalRecord(uuid);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    @RequestMapping(value = {"/api/m/1.0/getoneSkinDataByUuid.json", "/api/h/1.0/getoneSkinDataByUuid.json"}, produces = "application/json;charset=utf-8")
    public String getoneSkinDataByUuid(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = -1;
    	WelinkVO welinkVO = new WelinkVO();
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
    	Session session = currentUser.getSession();
    	profileId = (long) session.getAttribute("profileId");
    	if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
    	String uuid=request.getParameter("uuid");
    	Map map=mikuMineSkinQuestionService.getFinalRecord(uuid);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    
    //根据对应的用户的信息来获取uuid
    //List<String> selectFinalRecordByUid(Long uid,Long type)
    //根据对应的uid查出对应的用户最近的调查问卷的内容值
    @RequestMapping(value = {"/api/m/1.0/getoneUserSkinDataByUid.json", "/api/h/1.0/getoneUserSkinDataByUid.json"}, produces = "application/json;charset=utf-8")
    public String getoneUserSkinDataByUid(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
    	Long uid= Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):0+""));
    	Long type= Long.parseLong((String) (request.getParameter("type")!=null?request.getParameter("type"):1+""));
    	//根据对应的用户的信息来获取uuid
    	List<MikuMineQuestionRecords>  list=mikuMineSkinQuestionService.selectFinalRecordByUid(uid,type);
        Map map=new HashMap<>();
        map.put("uuid", "");
        map.put("flag", "0");
        if(list.size()>0){
        	 map.put("uuid", list.get(list.size()-1).getUuid());
             map.put("flag", "1");
        }
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	return JSON.toJSONString(welinkVO);
    }
    
    //==================================报告生成===================================================
    //根据专家帮填用户的信息插入信息[测试]
//    @RequestMapping(value = {"/api/m/1.0/insertOneSkinReportTest.json", "/api/h/1.0/insertOneSkinReportTest.json"}, produces = "application/json;charset=utf-8")
//    public String insertOneSkinReportTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	String recordId=request.getParameter("uuid");
//    	String userInfo=request.getParameter("userInfo");
//    	String picUrls=request.getParameter("picUrls");
//    	String skinInfo=request.getParameter("skinInfo");
//    	String suggestionInfo=request.getParameter("suggestionInfo");
//    	Long uid= Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):1+""));
//    	Long serviceId= Long.parseLong((String) (request.getParameter("serviceid")!=null?request.getParameter("serviceid"):1+""));
//    	Long questionnaireId= Long.parseLong((String) (request.getParameter("questionnaireId")!=null?request.getParameter("questionnaireId"):1+""));
//    	String money=request.getParameter("money");
//    	String flag=request.getParameter("flag")!=null?"1":"0";
//    	Long rid= Long.parseLong((String) (request.getParameter("rid")!=null?request.getParameter("rid"):1+""));
//        Map map=mikuMineSkinQuestionService.InsertOneReportData(recordId,userInfo,picUrls,skinInfo,suggestionInfo,uid,serviceId,money,questionnaireId,flag,rid);
//        WelinkVO welinkVO = new WelinkVO();
//    	welinkVO.setResult(map);
//    	welinkVO.setStatus(1);
//    	return JSON.toJSONString(welinkVO);
//    }
    
    //根据专家帮填用户的信息插入信息
//    @RequestMapping(value = {"/api/m/1.0/insertOneSkinReport.json", "/api/h/1.0/insertOneSkinReport.json"}, produces = "application/json;charset=utf-8")
//    public String insertOneSkinReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	long profileId = -1;
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
//    	String recordId=request.getParameter("uuid");
//    	String userInfo=request.getParameter("userInfo");
//    	String picUrls=request.getParameter("picUrls");
//    	String skinInfo=request.getParameter("skinInfo");
//    	String suggestionInfo=request.getParameter("suggestionInfo");
//    	Long uid= Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):1+""));
//    	Long questionnaireId= Long.parseLong((String) (request.getParameter("questionnaireId")!=null?request.getParameter("questionnaireId"):1+""));
//    	Long serviceId= profileId;
//    	String money=request.getParameter("money");
//    	//进行插入的操作
//		//进行更新的操作
//    	String flag=request.getParameter("flag")!=null?request.getParameter("flag"):"0";
//    	Long rid= Long.parseLong((String) (request.getParameter("rid")!=null?request.getParameter("rid"):1+""));
//    	Map map=mikuMineSkinQuestionService.InsertOneReportData(recordId,userInfo,picUrls,skinInfo,suggestionInfo,uid,serviceId,money,questionnaireId,flag,rid);
//    	welinkVO.setResult(map);
//    	welinkVO.setStatus(1);
//    	return JSON.toJSONString(welinkVO);
//    }
    
    
    
//    //查找对应用户的调查报告
//    @RequestMapping(value = {"/api/m/1.0/getOneUserReports.json", "/api/h/1.0/getOneUserReports.json"}, produces = "application/json;charset=utf-8")
//    public String getOneUserReports(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	long profileId = -1;
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
//    	Long uid= Long.parseLong((String) (request.getParameter("uid")!=null?request.getParameter("uid"):1+""));
//    	List<MikuMineDetectReport> list= mikuMineSkinQuestionService.getDataByUid(uid);
//    	Map map=new HashMap<>();
//    	map.put("data", list);
//    	if(list.size()>0){
//    		map.put("falg", "1");
//    	}else{
//    		map.put("falg", "0");
//    	}
//    	welinkVO.setResult(map);
//    	welinkVO.setStatus(1);
//    	return JSON.toJSONString(welinkVO);
//    }
    
    
    
    //根据reportid查出对应的报告记录
//    @RequestMapping(value = {"/api/m/1.0/getOneUserReportByrid.json", "/api/h/1.0/getOneUserReportByrid.json"}, produces = "application/json;charset=utf-8")
//    public String getOneUserReportByrid(HttpServletRequest request, HttpServletResponse response) throws Exception {
//    	long profileId = -1;
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
//    	Long rid= Long.parseLong((String) (request.getParameter("rid")!=null?request.getParameter("rid"):1+""));
//    	MikuMineDetectReport rdata= mikuMineSkinQuestionService.getDataByrid(rid);
//    	Map map=new HashMap<>();
//    	if(rdata!=null){
//    		map.put("report", rdata);
////    		List<MikuMineQuestionRecords> rlist=mikuMineSkinQuestionService.getOneUserRecordByuuid(rdata.getRecordId());
////    		map.put("list", mikuMineSkinQuestionService.getFinalQuestion(rlist));
//    		MikuCsadDO csad=mikuMineSkinQuestionService.getOneZjData(rdata.getServiceId());
//    		map.put("cdata", csad);
//    		welinkVO.setStatus(1);
//    	}else{
//    		welinkVO.setStatus(0);
//    	}
////    	map.put("data", list);
//    	welinkVO.setResult(map);
//    	return JSON.toJSONString(welinkVO);
//    }
    
    
    
    
}
