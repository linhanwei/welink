package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.PointViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.PointService;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.utils.BaseResult;
import com.welink.commons.Env;
import com.welink.commons.domain.PointRecordDO;
import com.welink.web.common.util.ParameterUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-3-11.
 */
@RestController
public class FetchPointList {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchPointList.class);

    @Resource
    private PointService pointService;

    @Resource
    private Env env;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchPointList.json", "/api/h/1.0/fetchPointList.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        int page = ParameterUtil.getParameterAsIntForSpringMVC(request, "pg");
        int size = ParameterUtil.getParameterAsIntForSpringMVC(request, "sz");
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = (page) * size;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        List<PointViewDO> pointViewDOs = new ArrayList<PointViewDO>();
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        List<PointRecordDO> pointRecordDOList = null;
        BaseResult<List<PointRecordDO>> pointServicePointList = pointService.findPointList(profileId, startRow, size);
        if (null != pointServicePointList && pointServicePointList.isSuccess()) {
            pointRecordDOList = pointServicePointList.getResult();
        }

        if (null != pointRecordDOList && pointRecordDOList.size() < size) {
            resultMap.put("hasNext", false);
        } else {
            resultMap.put("hasNext", true);
        }

        for (PointRecordDO pointRecordDO : pointRecordDOList) {
            PointViewDO pointViewDO = ViewDOCopy.buildPointViewDO(pointRecordDO);
            pointViewDOs.add(pointViewDO);
        }

        String pointRule = "http://h5.unesmall.com/h5/html/point-guide.html";
        if (env.isProd()) {
            pointRule = "http://h5.unesmall.com/h5/html/point-guide.html";
        } else {
            pointRule = "http://h5.unesmall.com/h5/html/point-guide.html";
        }
        resultMap.put("pointRuleUrl",pointRule);

        welinkVO.setStatus(1);
        resultMap.put("pointList", pointViewDOs);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
