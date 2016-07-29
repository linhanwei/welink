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
import com.welink.biz.service.UserService;
import com.welink.biz.service.RedPacketService;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.TimeUtil;
import com.welink.commons.domain.MikuInstrumentMeasureLogDO;
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
public class DoMikuMeasureLog {

	//操作的是单表
	@Resource
	private MikuInstrumentMeasureLogDOMapper mikuInstrumentMeasureLogDOMapper;
	
	@Resource
	private DoMikuMeasureLogService doMikuMeasureLogService;
	@Resource
	private ItemService itemService;
	
	
	//进行单单操作的测肤表[增加的操作]
    @RequestMapping(value = {"/api/m/1.0/insertmkmlog.json", "/api/h/1.0/insertmkmlog.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
    	//5个参数
    	String measureValue = request.getParameter("measureValue");
    	String moistureValue = request.getParameter("moistureValue");
    	String oilValue = request.getParameter("oilValue");
    	String resilienceValue = request.getParameter("resilienceValue");
    	String senilityValue = request.getParameter("senilityValue");
    	Byte mytest=Byte.parseByte(request.getParameter("testPosition"));
    	//产生对应的随机数
//    	measureValue =String.valueOf((Math.random()*25));
//    	moistureValue = String.valueOf((Math.random()*25));
//    	oilValue = String.valueOf((Math.random()*25));
//    	resilienceValue =String.valueOf((Math.random()*25));
//    	senilityValue = String.valueOf((Math.random()*25));
    	//默认是测试类型
    	Byte measureType=(byte)1;
    	Byte instrumentType=(byte)1;
    	Byte testPostion=mytest;
    	MikuInstrumentMeasureLogDO flag=doMikuMeasureLogService.insertOneDataByparams(measureValue, moistureValue, oilValue, resilienceValue, senilityValue, profileId,measureType,instrumentType,testPostion);
    	welinkVO.setStatus(1);
        welinkVO.setMsg("success");
        Map map=new HashMap();
    	map.put("bean", flag);
    	welinkVO.setResult(map);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    
    
    //多参数进行添加测试数据
    //进行单单操作的测肤表[增加的操作]
    @RequestMapping(value = {"/api/m/1.0/insertmkmlogTest.json", "/api/h/1.0/insertmkmlogTest.json"}, produces = "application/json;charset=utf-8")
    public String insertTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String measureValue = request.getParameter("measureValue");
    	String moistureValue = request.getParameter("moistureValue");
    	String oilValue = request.getParameter("oilValue");
    	String resilienceValue = request.getParameter("resilienceValue");
    	String senilityValue = request.getParameter("senilityValue");
    	Byte mytest=Byte.parseByte(request.getParameter("testPostion"));
    	Long profiledId=Long.parseLong(request.getParameter("id"));
    	int year=Integer.parseInt(request.getParameter("year"));
    	int month=Integer.parseInt(request.getParameter("month"));
    	int day=Integer.parseInt(request.getParameter("day"));
    	int hour=Integer.parseInt(request.getParameter("hour"));
    	//产生对应的随机数
    	measureValue =String.valueOf((Math.random()*50));
    	moistureValue = String.valueOf((Math.random()*50));
    	oilValue = String.valueOf((Math.random()*50));
    	resilienceValue =String.valueOf((Math.random()*50));
    	senilityValue = String.valueOf((Math.random()*50));
    	//默认是测试类型
    	Byte measureType=(byte)1;
    	Byte instrumentType=(byte)1;
    	Byte testPostion=mytest;
    	MikuInstrumentMeasureLogDO flag=doMikuMeasureLogService.insertOneDataByparamsTest(measureValue, moistureValue, oilValue, resilienceValue, senilityValue, profiledId,measureType,instrumentType,testPostion,year,month,day,hour);
        return JSON.toJSONString(flag);
    }
    
    
    
    
   
    
    
    
   
    
    
    //查出当前测试的结果
    @RequestMapping(value = {"/api/m/1.0/selectBynowParams.json", "/api/h/1.0/selectBynowParams.json"}, produces = "application/json;charset=utf-8")
    public String selectBynowParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = 1;
    	Long id= Long.parseLong(request.getParameter("id"));
    	//2个参数:时间日期【Y M W D】
    	String timetype = request.getParameter("timetype");
    	Byte mytest=Byte.parseByte(request.getParameter("testPostion"));
    	List<MikuOperMeaureDetail> list=doMikuMeasureLogService.getAllListData(profileId, timetype,mytest);
        return JSON.toJSONString(list);
    }
    
    
    //=============================================开发 与  测试 分开2个接口进行书写=============================================================
    
    //进行多条件查询【日月年操作】
    @RequestMapping(value = {"/api/m/1.0/selectByTimeType.json", "/api/h/1.0/selectByTimeType.json"}, produces = "application/json;charset=utf-8")
    public String selectByTimeType(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
//    	List<MikuOperMeaureData> list=doMikuMeasureLogService.getOperaterListByAvgParams(profileId, timetype);
    	List<MikuOperMeaureData> list=doMikuMeasureLogService.getNewDataListByParams(profileId, timetype);
    	Map map=new HashMap();
    	map.put("list", list);
    	welinkVO.setStatus(1);
    	welinkVO.setResult(map);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    //进行多条件查询【日月年操作】
    @RequestMapping(value = {"/api/m/1.0/selectByTimeTypeTest.json", "/api/h/1.0/selectByTimeTypeTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByTimeTypeTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = Long.parseLong((String) ((request.getParameter("profileId")) != null?request.getParameter("profileId"):1+""));
    	String timetype = request.getParameter("timetype");
//    	List<MikuOperMeaureData> list=doMikuMeasureLogService.getOperaterListByAvgParams(profileId, timetype);
    	List<MikuOperMeaureData> list=doMikuMeasureLogService.getNewDataListByParams(profileId, timetype);
        return JSON.toJSONString(list);
    }
    
    
    
    //进行详情的查询
    @RequestMapping(value = {"/api/m/1.0/selectByTimeTypeDetail.json", "/api/h/1.0/selectByTimeTypeDetail.json"}, produces = "application/json;charset=utf-8")
    public String selectByTimeTypeDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
    	Byte mytest=Byte.parseByte(request.getParameter("testPostion"));
    	List<MikuOperMeaureDetail> list=doMikuMeasureLogService.getAllListData(profileId, timetype,mytest);
    	Map map=new HashMap();
    	map.put("list", list);
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    //进行详情的查询============测试
    @RequestMapping(value = {"/api/m/1.0/selectByTimeTypeDetailTest.json", "/api/h/1.0/selectByTimeTypeDetailTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByTimeTypeDetailTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = 1;
    	WelinkVO welinkVO = new WelinkVO();
    	//2个参数:时间日期【Y M W D】
    	String timetype = request.getParameter("timetype");
    	Byte mytest=Byte.parseByte(request.getParameter("testPostion"));
    	Long id= Long.parseLong((String) (request.getParameter("id")!=null?request.getParameter("id"):1+""));
    	List<MikuOperMeaureDetail> list=doMikuMeasureLogService.getAllListData(id, timetype,mytest);
        return JSON.toJSONString(list);
    }
    
    
    //根据当前的结果来得出对应的结果值
    @RequestMapping(value = {"/api/m/1.0/selectByOneDataToResult.json", "/api/h/1.0/selectByOneDataToResult.json"}, produces = "application/json;charset=utf-8")
    public String selectByOneDataToResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
    	Long id= Long.parseLong(request.getParameter("id"));
    	Map map=doMikuMeasureLogService.getOperateDataById(id);
    	map.put("itemlist",itemService.combineItemTags(doMikuMeasureLogService.getItemList()));
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
    	System.out.println(JSON.toJSONString(welinkVO));
    	System.out.println(JSON.toJSONString(map));
    	JSON.toJSONString(map);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    //根据当前的结果来得出对应的结果值[测试]   加上对应的商品列表
    @RequestMapping(value = {"/api/m/1.0/selectByOneDataToResultTest.json", "/api/h/1.0/selectByOneDataToResultTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByOneDataToResultTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Long id= Long.parseLong(request.getParameter("id"));
    	Map map=doMikuMeasureLogService.getOperateDataById(id);
//    	finalmap.put("itemlist", getItemList());
    	map.put("itemlist",itemService.combineItemTags(doMikuMeasureLogService.getItemList()));
        return JSON.toJSONString(map);
    }
    
    
    //查看对应的字典
    @RequestMapping(value = {"/api/m/1.0/selectByOneDirectry.json", "/api/h/1.0/selectByOneDirectry.json"}, produces = "application/json;charset=utf-8")
    public String selectByOneDirectry(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map map=doMikuMeasureLogService.getAlldataByType();
    	WelinkVO welinkVO = new WelinkVO();
    	welinkVO.setResult(map);
    	welinkVO.setStatus(1);
        welinkVO.setMsg("success");
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    //===============================2016年05月10日   根据查询的所有的数据再进行查询出对应的平均值【日月年总平均值查询】===============================
    @RequestMapping(value = {"/api/m/1.0/selectByNewTimeTypeTest.json", "/api/h/1.0/selectByNewTimeTypeTest.json"}, produces = "application/json;charset=utf-8")
    public String selectByNewTimeTypeTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = Long.parseLong((String) ((request.getParameter("profileId")) != null?request.getParameter("profileId"):1+""));
    	String timetype = request.getParameter("timetype");
    	List<MikuOperMeaureData> list=doMikuMeasureLogService.getNewDataListByParams(profileId, timetype);
    	WelinkVO welinkVO = new WelinkVO();
    	Map map=new HashMap();
    	map.put("list", list);
    	welinkVO.setStatus(1);
    	welinkVO.setResult(map);
        return JSON.toJSONString(welinkVO);
    }
    
    
    //进行多条件查询【日月年操作】
    @RequestMapping(value = {"/api/m/1.0/selectByNewTimeType.json", "/api/h/1.0/selectByNewTimeType.json"}, produces = "application/json;charset=utf-8")
    public String selectByNewTimeType(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
    	welinkVO.setStatus(1);
    	welinkVO.setResult(map);
        return JSON.toJSONString(welinkVO);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
