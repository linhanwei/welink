package com.welink.web.resource;

import java.text.SimpleDateFormat;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.BCrypt;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.service.UserService;
import com.welink.biz.service.RedPacketService;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.TimeUtil;
import com.welink.commons.domain.MikuLogsDO;
import com.welink.commons.domain.MikuLogsDOExample;
import com.welink.commons.domain.MikuRedPackSettingDO;
import com.welink.commons.domain.MikuRedPackSettingDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.RedPacket;
import com.welink.commons.persistence.MikuLogsDOMapper;
import com.welink.commons.persistence.MikuRedPackSettingDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.ProfileWeChatDOMapper;
import com.welink.commons.persistence.WeChatProfileDOMapper;
import com.welink.web.common.constants.ResponseMSGConstans;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class DoRedPacketTest {

	@Resource
    private MemcachedClient memcachedClient;
	
	@Resource
	private RedPacketService redPacketService;
	
	@Resource
	private MikuRedPackSettingDOMapper mikuRedPackSettingDOMapper;
	
	@Resource
    private MikuLogsDOMapper mikuLogsDOMapper;
	
	
    @RequestMapping(value = {"/api/m/1.0/doredpackettest.json", "/api/h/1.0/doredpackettest.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//去掉全部的缓存
    	memcachedClient.delete("rpendTime");
        memcachedClient.delete("rpbeginTime");
        //查出对应的全部的profile id
        MikuLogsDOExample mikuLogsDOExample=new MikuLogsDOExample();
        mikuLogsDOExample.createCriteria();
        List<MikuLogsDO> mikuList=mikuLogsDOMapper.selectByExample(mikuLogsDOExample);
        for(int i=0;i<mikuList.size();i++){
        	MikuLogsDO mikuLogsDO=mikuList.get(i);
        	memcachedClient.delete("rpid"+mikuLogsDO.getUserid());
    	}
        System.out.println("ok");
        return "清除全部的缓存";
    }
}
