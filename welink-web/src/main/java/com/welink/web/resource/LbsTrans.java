package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.common.util.StringUtils;
import com.google.common.base.Optional;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.AmapTransformService;
import com.welink.web.common.util.ParameterUtil;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 15-1-21.
 */
@RestController
public class LbsTrans {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(LbsTrans.class);

    @Resource
    private AmapTransformService amapTransformService;

    @RequestMapping(value = {"/api/m/1.0/lbsTrans.json", "/api/h/1.0/lbsTrans.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String longitude = ParameterUtil.getParameter(request, "lg");
        String latitude = ParameterUtil.getParameter(request, "lt");
        WelinkVO welinkVO = new WelinkVO();
        if (StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        String point = longitude + "," + latitude;
        Optional<String> lbs = amapTransformService.pointTransform(point);
        if (lbs.isPresent()) {
            welinkVO.setStatus(1);
            Map resultMap = new HashMap();
            if (org.apache.commons.lang.StringUtils.isNotBlank(lbs.get()) && lbs.get().split(",").length > 1) {
                resultMap.put("lg", lbs.get().split(",")[0]);
                resultMap.put("lt", lbs.get().split(",")[1]);
            }
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.LBS_TRANSFORM_FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.LBS_TRANSFORM_FAILED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
    }
}
