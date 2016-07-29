package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.EvalService;
import com.welink.commons.domain.OrderEvaluateDO;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-11-14.
 */
@RestController
public class FetchEval {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FetchEval.class);

    @Resource
    private EvalService evalService;

    @RequestMapping(value = {"/api/m/1.0/fetchEval.json", "/api/h/1.0/fetchEval.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        long itemId = ParameterUtil.getParameterAslongForSpringMVC(request, "itemId", -1l);
        int page = ParameterUtil.getParameterAsIntForSpringMVC(request, "pg");
        int size = ParameterUtil.getParameterAsIntForSpringMVC(request, "sz");
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        if (itemId <= 0) {
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            return JSON.toJSONString(welinkVO);
        }
        List<OrderEvaluateDO> orderEvaluateDOList = evalService.fetchEvalsByPage(size, startRow, itemId);
        resultMap.put("itemEvals", orderEvaluateDOList);
        if (null != orderEvaluateDOList && orderEvaluateDOList.size() < size) {
            resultMap.put("hasNext", false);
        } else {
            resultMap.put("hasNext", true);
        }
        welinkVO.setResult(resultMap);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }
}
