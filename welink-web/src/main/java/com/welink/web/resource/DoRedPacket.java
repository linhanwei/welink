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
public class DoRedPacket {

	@Resource
    private MemcachedClient memcachedClient;
	
	@Resource
	private RedPacketService redPacketService;
	
	@Resource
	private MikuRedPackSettingDOMapper mikuRedPackSettingDOMapper;
	
	private final static int MCTime=86400;
	
    @RequestMapping(value = {"/api/m/1.0/doredpacket.json", "/api/h/1.0/doredpacket.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	long profileId = -1;
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        WelinkVO welinkVO = new WelinkVO();
//        不是对应的用户
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //对应的返回的信息
        RedPacket redPacket=new RedPacket();
        //进行开始时间与结束时间   红包的个数   金额 
        //删除缓存[调试使用的]
//        memcachedClient.delete("rpendTime");
//        memcachedClient.delete("rpbeginTime");
        String endTime=(String) memcachedClient.get("rpendTime");
        String beginTime=(String) memcachedClient.get("rpbeginTime");
        Long rprealNum=(Long) memcachedClient.get("rpnum");
        //2种情况:活动时间已经过去   活动时间还没有开始    
        if(beginTime==null && endTime==null){
        	MikuRedPackSettingDOExample example=new MikuRedPackSettingDOExample();
        	example.createCriteria().andIdEqualTo(1L);
        	List<MikuRedPackSettingDO> mikuRedPackSettingDOList=mikuRedPackSettingDOMapper.selectByExample(example);
        	if(mikuRedPackSettingDOList.size()>0){
        		MikuRedPackSettingDO mikuRedPackSettingDO=mikuRedPackSettingDOList.get(0);
        		Long nbprice=mikuRedPackSettingDO.getPrice();
        		Long nbmax=mikuRedPackSettingDO.getRpmax();
        		int  nbnum=Integer.parseInt(mikuRedPackSettingDO.getNum().toString());
        		memcachedClient.set("rpbeginTime", MCTime,sdf.format(mikuRedPackSettingDO.getBegintime()));
        		memcachedClient.set("rpendTime", MCTime, sdf.format(mikuRedPackSettingDO.getEndtime()));
        		memcachedClient.set("rpprice",MCTime,mikuRedPackSettingDO.getPrice());
        		memcachedClient.set("rponepercent",MCTime,mikuRedPackSettingDO.getOnepercent());
        		memcachedClient.set("rpnum",MCTime,mikuRedPackSettingDO.getNum());
        		memcachedClient.set("rponepackstr",MCTime,mikuRedPackSettingDO.getOnepackstr());
        		if(nbprice>=(nbmax*nbnum)){
        			redPacket.setInfo("对应参数设置有误...");
            		redPacket.setBeginTime("-1");
            		redPacket.setEndTime("-1");
            		redPacket.setTimeInfo("-1");
            		redPacket.setFlag("-2");
            		redPacket.setNum(0L);
            		return JSON.toJSONString(redPacket);
        		}
        		memcachedClient.set("rpalltotalArr",MCTime,redPacketService.generate(mikuRedPackSettingDO.getPrice(), Integer.parseInt(mikuRedPackSettingDO.getNum().toString()), mikuRedPackSettingDO.getRpmax(), 1));
        		memcachedClient.set("rpindex", MCTime, 0);
        		endTime=sdf.format(mikuRedPackSettingDO.getEndtime());
        		beginTime=sdf.format(mikuRedPackSettingDO.getBegintime());
        		rprealNum=mikuRedPackSettingDO.getNum();
        	}else{
        		redPacket.setInfo("对应摇摇拿红包没有进行设置...");
        		redPacket.setBeginTime("-1");
        		redPacket.setEndTime("-1");
        		redPacket.setTimeInfo("-1");
        		redPacket.setFlag("-1");
        		redPacket.setNum(0L);
        		return JSON.toJSONString(redPacket);
        	}
        }
       //如果是mache里面的具有值的话，进行比较时间[1.在开始时间之前   2.在结束时间之后  3.开始时间与结束时间之间]
       int timeFlag=redPacketService.getTimeFlag(beginTime, endTime);
       String info="";
       String timeinfo="-1";
       String flag="";
       if(timeFlag==1){
    	   timeinfo=redPacketService.compare_date(beginTime);
    	   info="时间在摇红包之前.";
    	   flag="1";
       }
       else if(timeFlag==2){
    	   info="时间在正在摇红包当中.";
    	   flag="2";
       }
       else if(timeFlag==3){
    	   info="时间已经过了摇红包的时间.";
    	   flag="3";
       }
      redPacket.setInfo(info); 
      redPacket.setBeginTime(beginTime);
	  redPacket.setEndTime(endTime);
      redPacket.setFlag(flag);
      redPacket.setTimeInfo(timeinfo);
      redPacket.setNum(rprealNum);
      System.out.println(JSON.toJSONString(redPacket));
      long[] logarr=(long[]) memcachedClient.get("rpalltotalArr");
      System.out.println(redPacketService.dolongarrToStr(logarr));
      return redPacketService.rObjStr(redPacket);
    }
}
