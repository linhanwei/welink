package com.welink.web.resource.customized;

import java.util.ArrayList;
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
import com.welink.biz.service.MikuMineScBoxService;
import com.welink.biz.service.UserService;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.domain.MikuMineCourseDO;
import com.welink.commons.domain.MikuMineCourseStepDO;
import com.welink.commons.domain.MikuMineCourseStepDOExample;
import com.welink.commons.domain.MikuMineDetectReport;
import com.welink.commons.domain.MikuMineScProductDO;
import com.welink.commons.domain.MikuMineScProductDOExample;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.MikuMineCourseDOMapper;
import com.welink.commons.persistence.MikuMineCourseStepDOMapper;
import com.welink.commons.persistence.MikuMineDetectReportMapper;
import com.welink.commons.persistence.MikuMineExpertDbDOMapper;
import com.welink.commons.persistence.MikuMineQuestionnaireRecordsDOMapper;
import com.welink.commons.persistence.MikuMineScBoxDOMapper;
import com.welink.commons.persistence.MikuMineScProductDOMapper;
import com.welink.commons.vo.DetectReportTradeVO;
import com.welink.commons.vo.MikuCsadVO;
import com.welink.commons.vo.MikuMineRecentlycontactLogVO;
import com.welink.commons.vo.MikuMineScBoxVO;
import com.welink.web.common.constants.ResponseStatusEnum;

@RestController
public class MikuMineScBoxAction {
	
	@Resource
	private UserService userService;
	
	@Resource
	private MikuMineScBoxService mikuMineScBoxService;
	
	@Resource
	private MikuMineDetectReportMapper mikuMineDetectReportMapper;
	
	@Resource
	private MikuMineExpertDbDOMapper mikuMineExpertDbDOMapper;
	
	@Resource
	private MikuMineScBoxDOMapper mikuMineScBoxDOMapper;
	
	@Resource
	private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
	private MikuMineScProductDOMapper mikuMineScProductDOMapper;
	
	@Resource
	private MikuMineCourseDOMapper mikuMineCourseDOMapper;
	
	@Resource
	private MikuMineCourseStepDOMapper mikuMineCourseStepDOMapper;
	
	@Resource
	private MikuMineQuestionnaireRecordsDOMapper mikuMineQuestionnaireRecordsDOMapper;

	/**
	 * 生成盒子
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/createMineScBox.json", "/api/h/1.0/createMineScBox.json"}, produces = "application/json;charset=utf-8")
    public String createMineScBox(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="detectReportId", required = true) Long detectReportId) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        //查询报告
        MikuMineDetectReport mikuMineDetectReport = mikuMineDetectReportMapper.selectByPrimaryKey(detectReportId);
        
        if(null == mikuMineDetectReport){
        	welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~报表不能为空~");
			return JSON.toJSONString(welinkVO);
        }
        
        MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
        mikuCsadDOExample.createCriteria().andUserIdEqualTo(profileId);
        if(mikuCsadDOMapper.countByExample(mikuCsadDOExample) < 1){
        	welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~您不是专家不能生成盒子~");
			return JSON.toJSONString(welinkVO);
        }
        return JSON.toJSONString(mikuMineScBoxService.createMineScBox(mikuMineDetectReport));
    }
	
	/**
	 * 获取私人专家
	 * @param request
	 * @param response
	 * @param pg
	 * @param sz
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = {"/api/m/1.0/getPrivateExpertList.json", "/api/h/1.0/getPrivateExpertList.json"}, produces = "application/json;charset=utf-8")
    public String getPrivateExpertList(HttpServletRequest request, HttpServletResponse response, 
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
    	int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("clientUserId", profileId);
        List<MikuCsadVO> mikuCsadVOList = mikuCsadDOMapper.getPrivateExpertList(paramMap);
        boolean hasNext = true;
        if (null != mikuCsadVOList && mikuCsadVOList.size() < size) {
            hasNext = false;
        }else if(null == mikuCsadVOList){
            hasNext = false;
        }else{
        	hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuCsadVOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
    }
	
	/**
	 * 获取盒子订单
	 * @param request
	 * @param response
	 * @param type (0=客户；1=专家)
	 * @param hasTrade 是否有订单(0=全部；1=有订单) 默认0
	 * @param allTrade //是否查询全部订单(0=没取消的订单；1=全部订单)
	 * @param pg
	 * @param sz
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/getMineScBoxTradeList.json", "/api/h/1.0/getMineScBoxTradeList.json"}, produces = "application/json;charset=utf-8")
    public String getDetectReportTrades(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type,
    		@RequestParam(value="hasTrade", required = false, defaultValue = "0") Integer hasTrade,
    		@RequestParam(value="allTrade", required = false, defaultValue = "0") Integer allTrade,
    		@RequestParam(value="boxId", required = false) Long boxId,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
		
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if(null != type && type == 0){
        	paramMap.put("userId", profileId);
        }else{
        	paramMap.put("serviceId", profileId);
        }
        if(null != boxId){
        	paramMap.put("boxId", boxId);
        }
        if(null != hasTrade && hasTrade == 1){
        	paramMap.put("hasTrade", 1);
        }
        if(null != allTrade && allTrade == 0){
        	paramMap.put("allTrade", 0);	//是否查询全部订单(0=没取消的订单；1=全部订单)
        }
        paramMap.put("orderByClause", "msb.date_created DESC");
        paramMap.put("limit", size);
        paramMap.put("offset", startRow);
        
        List<MikuMineScBoxVO> boxTrades = mikuMineScBoxDOMapper.getMineScBoxTradeList(paramMap);
        if(!boxTrades.isEmpty()){
        	for(MikuMineScBoxVO mikuMineScBoxVO : boxTrades){
        		if(null != mikuMineScBoxVO.getCourseId()){
        			MikuMineCourseDO mikuMineCourseDO = mikuMineCourseDOMapper.selectByPrimaryKey(mikuMineScBoxVO.getCourseId());
        			mikuMineScBoxVO.setMineCourse(mikuMineCourseDO);
        		}
        		String scProductIds = mikuMineScBoxVO.getScProductIds();
        		if(null != scProductIds){
        			List<Long> scProductIdsList = new ArrayList<Long>();
        			String[] scProductIdsArr = scProductIds.split(";");
        			if(null != scProductIds && scProductIdsArr.length > 0){
		        		for(String str : scProductIdsArr){
		        			scProductIdsList.add(Long.valueOf(str));
		        		}
		        	}
        			if(scProductIdsList.size() > 0){
        				MikuMineScProductDOExample mikuMineScProductDOExample = new MikuMineScProductDOExample();
        				mikuMineScProductDOExample.createCriteria().andIdIn(scProductIdsList);
        				List<MikuMineScProductDO> mikuMineScProductDOList = mikuMineScProductDOMapper.selectByExample(mikuMineScProductDOExample);
        				mikuMineScBoxVO.setProductList(mikuMineScProductDOList);
        			}
        		}
        	}
        }
        boolean hasNext = true;
        if (null != boxTrades && boxTrades.size() < size) {
            hasNext = false;
        }else if(null == boxTrades){
            hasNext = false;
        }else{
        	hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", boxTrades);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 获取课程步骤
	 * @param request
	 * @param response
	 * @param courseId	课程id
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/getMineCourseSetp.json", "/api/h/1.0/getMineCourseSetp.json"}, produces = "application/json;charset=utf-8")
    public String getMineCourseSetp(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="courseId", required = true) Long courseId) throws Exception {
        long profileId = -1;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        
		MikuMineCourseDO mikuMineCourseDO = mikuMineCourseDOMapper.selectByPrimaryKey(courseId);
		resultMap.put("mineCourse", mikuMineCourseDO);
		
		MikuMineCourseStepDOExample mikuMineCourseStepDOExample = new MikuMineCourseStepDOExample();
		mikuMineCourseStepDOExample.createCriteria().andCourseIdEqualTo(courseId);
		mikuMineCourseStepDOExample.setOrderByClause("step_order asc");
		List<MikuMineCourseStepDO> mikuMineCourseStepDOList = mikuMineCourseStepDOMapper.selectByExample(mikuMineCourseStepDOExample);
		resultMap.put("stepList", mikuMineCourseStepDOList);
		
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
}
