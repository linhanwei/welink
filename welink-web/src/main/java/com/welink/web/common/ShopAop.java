package com.welink.web.common;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedShop;
import com.welink.biz.common.security.SessionType;
import com.welink.commons.commons.BizConstants;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * Created by daniel on 15-4-1.
 */
@Component
@Aspect
public class ShopAop {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ShopAop.class);

    @Around(value = "@annotation(com.welink.biz.common.security.NeedShop)")
    public Object aroundManager(ProceedingJoinPoint pj) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Object[] args = pj.getArgs();//参数获取
        SessionType type = this.getSessionType(pj);
        if (type == null) {
            throw new Exception("The value of NeedSession is must.");
        }
        HttpServletResponse response = (HttpServletResponse) args[1];
        if (hasShop(session)) {
            try {
                return pj.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                log.error("session aop failed. exp:" + throwable.getMessage(), throwable);
            }
        } else {
            WelinkVO welinkVO = new WelinkVO();
            welinkVO.setStatus(0);
            response.setContentType("application/json;charset=utf-8");
            welinkVO.setCode(BizErrorEnum.NO_SHOP_IN_SESSION.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SHOP_IN_SESSION.getMsg());
            response.getWriter().write(JSON.toJSONString(welinkVO));
        }
        return null;
    }

    private boolean hasShop(Session session) {
        if (null != session && null != session.getAttribute(BizConstants.SHOP_ID)) {
            return true;
        }
        return false;
    }

    private SessionType getSessionType(ProceedingJoinPoint pj) {
        // 获取切入的 Method
        MethodSignature joinPointObject = (MethodSignature) pj.getSignature();
        Method method = joinPointObject.getMethod();
        boolean flag = method.isAnnotationPresent(NeedShop.class);
        if (flag) {
            NeedShop annotation = method.getAnnotation(NeedShop.class);
            return annotation.value();
        }
        return null;
    }
}
