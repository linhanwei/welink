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
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.MikuCsadService;
import com.welink.biz.service.UserService;
import com.welink.commons.domain.MikuCsadClientsDO;
import com.welink.commons.domain.MikuCsadClientsDOExample;
import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.MikuCsadClientsDOMapper;
import com.welink.commons.persistence.MikuCsadDOMapper;
import com.welink.commons.persistence.MikuCsadGroupDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.vo.ImUserVO;
import com.welink.commons.vo.MikuGroupCsadsVO;
import com.welink.web.common.constants.ResponseStatusEnum;

@RestController
public class MikuCsadAction {
	
	@Resource
	private UserService userService;
	
	@Resource
	private MikuCsadService mikuCsadService;
	
	@Resource
	private MikuCsadDOMapper mikuCsadDOMapper;
	
	@Resource
	private MikuCsadClientsDOMapper mikuCsadClientsDOMapper;
	
	@Resource
	private MikuCsadGroupDOMapper mikuCsadGroupDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;

	/**
	 * 查询客服分组列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/getGroupCsadList.json", "/api/h/1.0/getGroupCsadList.json"}, produces = "application/json;charset=utf-8")
    public String getGroupCsadList(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
		ImUserVO imUserVO = new ImUserVO();
		imUserVO.setUserName(null);
		imUserVO.setNickName(null);
		imUserVO.setPassword(null);
		resultMap.put("imUser", imUserVO);	//用户环信信息
		List<MikuGroupCsadsVO> mikuGroupCsadsVOList = mikuCsadService.getMikuGroupCsadsVOList();
		resultMap.put("list", mikuGroupCsadsVOList);
		
        /*ProfileDO profileDO = userService.fetchProfileById(profileId);
		if (null != profileDO) {
			IMUserBody userBody = new IMUserBody(profileDO.getEmUserName(),
					profileDO.getEmUserPw(), profileDO.getEmUserName());
			ImUserVO imUserVO = new ImUserVO();
			imUserVO.setUserName(userBody.getUserName());
			imUserVO.setNickName(userBody.getNickName());
			imUserVO.setPassword(userBody.getPassword());
			resultMap.put("imUser", imUserVO);	//用户环信信息
			List<MikuGroupCsadsVO> mikuGroupCsadsVOList = mikuCsadService.getMikuGroupCsadsVOList();
			resultMap.put("list", mikuGroupCsadsVOList);
		}else{
			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
		}*/
        
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
	
	/**
	 * 自动分配一个专家给会员分配
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/getOneCsad.json", "/api/h/1.0/getOneCsad.json"}, produces = "application/json;charset=utf-8")
	public String getOneCsad(HttpServletRequest request, HttpServletResponse response)throws Exception {
		
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
        
        MikuCsadClientsDOExample mikuCsadClientsDOExample = new MikuCsadClientsDOExample();
        mikuCsadClientsDOExample.createCriteria().andClientUserIdEqualTo(profileId);
        List<MikuCsadClientsDO> userRelaList = mikuCsadClientsDOMapper.selectByExample(mikuCsadClientsDOExample);
        
        MikuCsadDO csadInfo;
        
        
        MikuCsadDOExample mikuCsadDOExample = new MikuCsadDOExample();
        
        //如果该会员已经有专家返回该专家,没有就随机一个人数数的专家给他
        if(userRelaList.size() > 0){
        	MikuCsadClientsDO userRelaInfo = userRelaList.get(0);
        	mikuCsadDOExample.createCriteria().andIdEqualTo(userRelaInfo.getCsadId());
        	List<MikuCsadDO> csadList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
        	csadInfo = csadList.get(0);
        	
        	resultMap.put("info", csadInfo);
            
        }else{
        	mikuCsadDOExample.setOrderByClause("csad_clients asc");
        	mikuCsadDOExample.setOffset(0);
        	mikuCsadDOExample.setLimit(1);
        	List<MikuCsadDO> csadList = mikuCsadDOMapper.selectByExample(mikuCsadDOExample);
        	csadInfo = csadList.get(0);
        	
        	//添加专家与用户的关系
        	MikuCsadClientsDO record = new MikuCsadClientsDO();
        	
        	record.setCsadId(csadInfo.getId());
        	record.setCsadUserId(csadInfo.getUserId());
        	record.setClientUserId(profileId);
        	record.setClientGainTime(new Date());
        	record.setDateCreated(new Date());
        	record.setLastUpdated(new Date());
        	record.setVersion(1L);
        	int csadRelaId = mikuCsadClientsDOMapper.insert(record);
        	
        	
        	///插入成功,更新专家管理的总人数
        	if(csadRelaId > 0){
        		int csadClients = csadInfo.getCsadClients() != null ? csadInfo.getCsadClients() : 0;
        		csadInfo.setCsadClients(csadClients+1);
        		mikuCsadDOMapper.updateByPrimaryKeySelective(csadInfo);
        	}
        	
        	resultMap.put("info", csadInfo);
        	
        }
        
        //获取环信id
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(csadInfo.getUserId());
        List<ProfileDO> profileList = profileDOMapper.selectByExample(profileDOExample);
        ProfileDO  profileInfo = profileList.get(0);
        
        resultMap.put("csad_em_user_name", profileInfo.getEmUserName());
        
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setResult(resultMap);
		
		return JSON.toJSONString(welinkVO);
		
	}
	
}
