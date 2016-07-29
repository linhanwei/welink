package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.CompNoteViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ComplainService;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.domain.ComplainDO;
import com.welink.commons.domain.ComplainNoteDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
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
 * Created by daniel on 14-10-14.
 */
@RestController
public class FetchComp {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchComp.class);

    @Resource
    private ComplainService complainService;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @RequestMapping(value = {"/api/m/1.0/fetchComp.json", "/api/h/1.0/fetchComp.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        //para
        long profileId = -1;
        long complainId = ParameterUtil.getParameterAslongForSpringMVC(request, "complainId", -1l);
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        if (checkSession(welinkVO, session)) {
            return JSON.toJSONString(welinkVO);
        }

        profileId = (long) session.getAttribute("profileId");
        if (complainId < 0) {
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //fetch profile
        ProfileDOExample pExample = new ProfileDOExample();
        pExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profileDOs = profileDOMapper.selectByExample(pExample);
        if (null != profileDOs && profileDOs.size() > 0) {
            resultMap.put("headPic", profileDOs.get(0).getProfilePic());
        }
        //fetch complain
        List<ComplainDO> complainDOs = complainService.fetchComplain(complainId);
        if (null != complainDOs && complainDOs.size() > 0) {
            resultMap.put("complain", ViewDOCopy.buildCompViewDO(complainDOs.get(0)));
            //fetch complain notes
            List<ComplainNoteDO> complainNoteDOs = complainService.fetchComplainNotes(complainId);
            List<CompNoteViewDO> compNoteViewDOs = Lists.newArrayList();
            if (null != complainNoteDOs) {
                for (ComplainNoteDO complainNoteDO : complainNoteDOs) {
                    compNoteViewDOs.add(ViewDOCopy.buildCompNoteViewDO(complainNoteDO));
                }
            }
            resultMap.put("notes", compNoteViewDOs);

            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        }
        return JSON.toJSONString(welinkVO);
    }

    private boolean checkSession(WelinkVO welinkVO, Session session) {
        if (null == session) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
        if (null != session.getAttribute("profileId")) {
            return false;
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
    }
}
