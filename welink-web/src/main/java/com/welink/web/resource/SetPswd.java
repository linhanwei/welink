package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.cache.CheckNOValidator;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.*;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.web.common.constants.ResponseMSGConstans;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 这里是找回密码，调皮的把set和find密码命名个性化了哈哈
 * set new pass word . previous step is check verification code is valid
 * Created by daniel on 14-9-11.
 */
@RestController
public class SetPswd {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SetPswd.class);

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private UserService userService;

    @Resource
    private CheckNOValidator checkNOValidator;

    @RequestMapping(value = {"/api/m/1.0/setPswd.json", "/api/h/1.0/setPswd.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //para
        String pswd = request.getParameter("pswd");
        String hpswd = request.getParameter("hp");
        String mobile = request.getParameter("mobile");
        String code = request.getParameter("checkNum");
        WelinkVO welinkVO = new WelinkVO();
        ResponseResult result = new ResponseResult();
        if (StringUtils.isBlank(code)) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //判断用户是否存在
        ProfileDO profileDO1 = userService.fetchProfileByMobile(mobile);
        if (null == profileDO1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        //check check code
        boolean checkNOValid = checkNOValidator.checkNOisValid(code, mobile);
        //TODO:
        //checkNOValid = true;
        if (!checkNOValid) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        boolean isH5 = false;
        if (org.apache.commons.lang.StringUtils.isBlank(pswd)) {
            if (org.apache.commons.lang.StringUtils.isNotBlank(hpswd)) {
                pswd = hpswd;
                byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
                pswd = new String(pswdArray);
                isH5 = true;
            } else {
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                log.error("password param is blank...  mobile:" + mobile);
                return JSON.toJSONString(welinkVO);
            }
        	/*welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            log.error("password param is blank...  mobile:" + mobile);
            return JSON.toJSONString(welinkVO);*/
        }
        //set new password
        String depswd = PasswordParser.parserPlanPswd(pswd, null, isH5);
        /*String dehpswd = PasswordParser.parserPlanPswd(hpswd, null, isH5);
        if(!StringUtils.isBlank(depswd) && !pswd.equals(dehpswd)){
        	welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PASSWORD_NEQ.getCode());
            welinkVO.setMsg(BizErrorEnum.PASSWORD_NEQ.getMsg());
            return JSON.toJSONString(welinkVO);
        }*/
        
        String tostorePswd = BCrypt.hashpw(depswd, BCrypt.gensalt());

        ProfileDO profileDO = new ProfileDO();
        profileDO.setPassword(tostorePswd);
        profileDO.setMobile(mobile);
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile);
        int updatePswd = profileDOMapper.updateByExampleSelective(profileDO, profileDOExample);
        if (updatePswd > 0) {
            //登录
            welinkVO.setStatus(1);
            welinkVO.setMsg(ResponseMSGConstans.CHANGE_PASSWORD_SUCCESS);
            doLogin(mobile, depswd);
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
        }
        return JSON.toJSONString(welinkVO);
    }

    private void doLogin(String mobile, String depswd) {
        //执行登陆操作
        UsernamePasswordToken token = new UsernamePasswordToken(mobile, Md5.MD5Encode(depswd));
        token.setRememberMe(true);
        //2. 获取当前Subject：
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();

        Session session = currentUser.getSession();//boolean create
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);//millis
    }

    public void setProfileDOMapper(ProfileDOMapper profileDOMapper) {
        this.profileDOMapper = profileDOMapper;
    }
}
