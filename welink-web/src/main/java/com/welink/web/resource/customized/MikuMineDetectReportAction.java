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
import com.welink.commons.persistence.MikuMineDetectReportDOMapper;
import com.welink.commons.persistence.MikuMineQuestionRecordsMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.vo.DetectReportTradeVO;


@RestController
public class MikuMineDetectReportAction {
	
	@Resource
	private TradeMapper tradeMapper;
	
	@Resource
	private MikuMineDetectReportDOMapper mikuMineDetectReportDOMapper;
	
	
	/**
	 * 获取报告订单
	 * @param request
	 * @param response
	 * @param type (0=客户；1=专家)
	 * @param hasTrade 是否有订单(0=全部；1=有订单) 默认0
	 * @param pg
	 * @param sz
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/getDetectReportTrades.json", "/api/h/1.0/getDetectReportTrades.json"}, produces = "application/json;charset=utf-8")
    public String getDetectReportTrades(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="type", required = false, defaultValue = "0") Integer type,
    		@RequestParam(value="hasTrade", required = false, defaultValue = "0") Integer hasTrade,
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
        if(null != hasTrade && hasTrade == 1){
        	paramMap.put("hasTrade", 1);
        }
        paramMap.put("orderByClause", "mdr.date_created DESC");
        paramMap.put("limit", size);
        paramMap.put("offset", startRow);
        
        List<DetectReportTradeVO> detectReportTrades = mikuMineDetectReportDOMapper.getDetectReportTrades(paramMap);
        boolean hasNext = true;
        if (null != detectReportTrades && detectReportTrades.size() < size) {
            hasNext = false;
        }else if(null == detectReportTrades){
            hasNext = false;
        }else{
        	hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", detectReportTrades);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
        
}
