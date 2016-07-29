package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.web.common.constants.ResponseStatusEnum;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 14-12-3.
 */
@RestController
public class FetchPub {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchPub.class);

    @RequestMapping(value = {"/api/m/1.0/fetchPub.json", "/api/h/1.0/fetchPub.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap<>();
        if (null == RSAEncrypt.getPublicKeys()) {
            PasswordParser.loadPublicKeyInCase();
        }
        if (null != RSAEncrypt.getPublicKeys()) {
            BigInteger e = RSAEncrypt.getPublicKeys().getPublicExponent();
            BigInteger m = RSAEncrypt.getPublicKeys().getModulus();
            resultMap.put("m", m.toString(16));
            resultMap.put("e", e.toString(16));
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }
}
