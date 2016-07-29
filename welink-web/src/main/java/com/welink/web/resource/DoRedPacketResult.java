package com.welink.web.resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import com.welink.commons.domain.MikuRedPackSettingDO;
import com.welink.commons.domain.MikuRedPackSettingDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.commons.domain.ProfileWeChatDOExample;
import com.welink.commons.domain.RedPacket;
import com.welink.commons.domain.RedPacketResult;
import com.welink.commons.domain.WeChatProfileDO;
import com.welink.commons.domain.WeChatProfileDOExample;
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
public class DoRedPacketResult {

	@Resource
    private MemcachedClient memcachedClient;
	
	@Resource
	private RedPacketService redPacketService;
	
	@Resource
	private MikuRedPackSettingDOMapper mikuRedPackSettingDOMapper;
	
	@Resource
    private WeChatProfileDOMapper weChatProfileDOMapper;
    
    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;
    
    @Resource
    private MikuLogsDOMapper mikuLogsDOMapper;
	private final static int MCTime=86400;
	
    @RequestMapping(value = {"/api/m/1.0/doredpacketresult.json", "/api/h/1.0/doredpacketresult.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	long profileId = -1;
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        WelinkVO welinkVO = new WelinkVO();
        //不是对应的用户
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //删除缓存[调试使用的]
//        memcachedClient.delete("rpid"+profileId);
//        memcachedClient.delete("rpnum");
        //对应的返回的信息
        RedPacketResult redPacketResult=new RedPacketResult();
        //进行开始时间与结束时间   红包的个数   金额 
        String endTime=(String) memcachedClient.get("rpendTime");
        String beginTime=(String) memcachedClient.get("rpbeginTime");
        Long price= (Long) memcachedClient.get("rpprice");
        Long onepercent=(Long) memcachedClient.get("rponepercent");
        Long num= (Long) memcachedClient.get("rpnum");
        String onepackstr=(String) memcachedClient.get("rponepackstr");
        //全部的面值存入的数组
        long[] logarr=(long[]) memcachedClient.get("rpalltotalArr");
        int index=(int) memcachedClient.get("rpindex");
        String flag="";
        //2种情况:活动时间已经过去   活动时间还没有开始    
        if(beginTime==null && endTime==null){
        	MikuRedPackSettingDOExample example=new MikuRedPackSettingDOExample();
        	example.createCriteria().andIdEqualTo(1L);
        	List<MikuRedPackSettingDO> mikuRedPackSettingDOList=mikuRedPackSettingDOMapper.selectByExample(example);
        	if(mikuRedPackSettingDOList.size()>0){
        		MikuRedPackSettingDO mikuRedPackSettingDO=mikuRedPackSettingDOList.get(0);
        		memcachedClient.set("rpbeginTime", MCTime,sdf.format(mikuRedPackSettingDO.getBegintime()));
        		memcachedClient.set("rpendTime", MCTime, sdf.format(mikuRedPackSettingDO.getEndtime()));
        		memcachedClient.set("rpprice",MCTime,mikuRedPackSettingDO.getPrice());
        		memcachedClient.set("rponepercent",MCTime,mikuRedPackSettingDO.getOnepercent());
        		memcachedClient.set("rpnum",MCTime,mikuRedPackSettingDO.getNum());
        		memcachedClient.set("rponepackstr",MCTime,mikuRedPackSettingDO.getOnepackstr());
        		memcachedClient.set("rpalltotalArr",MCTime,redPacketService.generate(mikuRedPackSettingDO.getPrice(), Integer.parseInt(mikuRedPackSettingDO.getNum().toString()), mikuRedPackSettingDO.getRpmax(), 1));
        		memcachedClient.set("rpindex", MCTime, 0);
        		endTime=sdf.format(mikuRedPackSettingDO.getEndtime());
        		beginTime=sdf.format(mikuRedPackSettingDO.getBegintime());
        		num=mikuRedPackSettingDO.getNum();
        		price=mikuRedPackSettingDO.getPrice();
        		onepercent=mikuRedPackSettingDO.getOnepercent();
        		onepackstr=mikuRedPackSettingDO.getOnepackstr();
        		//数组加标示
        		logarr=(long[]) memcachedClient.get("rpalltotalArr");
        	    index=(int) memcachedClient.get("rpindex");
        	}else{
        		redPacketResult.setInfo("对应摇摇拿红包没有进行设置...");
        		redPacketResult.setBeginTime("-1");
        		redPacketResult.setEndTime("-1");
        		redPacketResult.setTimeInfo("-1");
        		redPacketResult.setFlag("-1");
        		redPacketResult.setNum(0L);
        		return JSON.toJSONString(redPacketResult);
        	}
        }
       //如果是mache里面的具有值的话，进行比较时间[1.在开始时间之前   2.在结束时间之后  3.开始时间与结束时间之间]
       int timeFlag=redPacketService.getTimeFlag(beginTime, endTime);
       String info="";
       String timeinfo="-1";
       String result="-1";
       if(timeFlag==1){
    	   timeinfo=redPacketService.compare_date(endTime);
    	   info="活动还没有开始.";
    	   flag="1";
       }
       else if(timeFlag==3){
    	   info="活动已经结束了.";
    	   num=0L;
    	   flag="3";
       }
       else if(timeFlag==2){
    	   flag="2";
    	   //开始概率事件的计算
    	   //第一次操作[之前需要判断总数num>0 || 是不是第一次抽中  || 还有一种情况就是钱没有了]
    	   String profilegetrpbag=(String) memcachedClient.get("rpid"+profileId);
		   if(num>0){
			  if("".equals(profilegetrpbag) || profilegetrpbag==null){
				  //有对应红包的数量但没有钱
				   if(price>0){
					   boolean bgflag=redPacketService.bingorp(onepercent);
		        	   if(bgflag){
		        		   //中奖事件
		        		   //Long oneprice=(long) redPacketService.getPrice(onepackstr);
		        		   Long oneprice=logarr[index];
		        		   //进行查找对应的用户的微信的信息
		        		   //这个是openid##price集合
		        		   String[] strarr=doTwoTables(profileId,oneprice).split("##");
		        		   String openid=strarr[0];
		        		   redPacketResult.setPrice(strarr[1]);
		        		   redPacketResult.setOpenid(openid);
		        		   //在memcached保存其记录
		        		   memcachedClient.set("rpid"+profileId, MCTime, "rpid"+profileId);
		        		   //总数减去对应的
		        		   memcachedClient.set("rpprice",MCTime,price-Long.parseLong(strarr[1]));
		        		   System.out.println("old --->  index:"+index);
		        		   index++;
		        		   memcachedClient.set("rpindex", MCTime, index);
		        		   System.out.println("now --->  index:"+index);
		        		   result="1";
		        		   info="抽中了一个红包";
		        		   //这里才是真正的减
		        		   num--;
		    			   memcachedClient.set("rpnum", MCTime, num);
		        	   }else{
		        		   //没有中奖
		        		   redPacketResult.setPrice("0");
		        		   result="0";
		        		   info="没有中奖";
		        	   }
				   }
				   else{
					   //已经没有钱了
					   memcachedClient.set("rpprice",MCTime,0L);
					   //对应的数量也应该变成0才对
					   memcachedClient.set("rpnum",MCTime,0L);
					   num=0L;
					   info="没有钱了.";
				   }
			  }else{
		       //1个人只能抽奖1次【摇中之后才减一次红包】
	   		   //没有中奖
	   		   redPacketResult.setPrice("0");
	   		   result="0";
	   		   info="没有中奖"; 
			  }
	   }else{
		   //红包的数量不足
		   result="2";
		   info="全部红包已经没有.";
	   }
       }
       redPacketResult.setBeginTime(beginTime);
	   redPacketResult.setEndTime(endTime);
       redPacketResult.setResult(result);
       redPacketResult.setInfo(info); 
	   redPacketResult.setFlag(flag);
	   //个数一直传
	   System.out.println(redPacketService.dolongarrToStr(logarr));
	   redPacketResult.setNum(num);
	   redPacketResult.setTimeInfo(timeinfo);
	   System.out.println(JSON.toJSONString(redPacketResult));
       return redPacketService.reresultObjStr(redPacketResult);
    }
    
    
    //中奖之后进行对logs表与active表进行操作
  	public String  doTwoTables(Long profileid,Long price){
  		String openid="";
  		Long nprice=price;
  		WeChatProfileDOExample weChatProfileDOExample=new WeChatProfileDOExample();
  		weChatProfileDOExample.createCriteria().andProfileIdEqualTo(profileid);
  		List<WeChatProfileDO> list=weChatProfileDOMapper.selectByExample(weChatProfileDOExample);
  		if(list.size()>0)
  		{
  			WeChatProfileDO weChatProfileDO=list.get(0);
  			ProfileWeChatDOExample profileWeChatDOExample=new ProfileWeChatDOExample();
  			profileWeChatDOExample.createCriteria().andIdEqualTo(weChatProfileDO.getWechatId());
  			List<ProfileWeChatDO> plist=profileWeChatDOMapper.selectByExample(profileWeChatDOExample);
  			if(plist.size()>0){
  				ProfileWeChatDO profileWeChatDO=plist.get(0);
  				openid=profileWeChatDO.getOpenid();
  				//操作的active表
  				MikuRedPackSettingDOExample mikuRedPackSettingDOExample=new MikuRedPackSettingDOExample();
  				mikuRedPackSettingDOExample.createCriteria().andIdEqualTo(2L);
  				List<MikuRedPackSettingDO> rpsdlist=mikuRedPackSettingDOMapper.selectByExample(mikuRedPackSettingDOExample);
  				if(rpsdlist.size()>0){
  					MikuRedPackSettingDO mikuRedPackSettingDO=rpsdlist.get(0);
  					mikuRedPackSettingDO.setNum(mikuRedPackSettingDO.getNum()-1L);
  					//剩余的钱设置
  					Long oneprice=mikuRedPackSettingDO.getPrice();
  					if(oneprice>=price){
  						mikuRedPackSettingDO.setPrice(oneprice-price);
  					}
  					else if(oneprice<price)
  					{
  						//剩余的钱清零
  						mikuRedPackSettingDO.setPrice(0L);
  						nprice=oneprice;
  					}
  					mikuRedPackSettingDOMapper.updateByPrimaryKey(mikuRedPackSettingDO);
  				}
  				//操作logs表
  			    if(nprice>0L){
	  			  	MikuLogsDO mikuLogsDO=new MikuLogsDO();
	  				mikuLogsDO.setOpenid(profileWeChatDO.getOpenid());
	  				mikuLogsDO.setDateCreated(new Date());
	  				mikuLogsDO.setPrice(nprice);
	  				mikuLogsDO.setTemp("红包");
	  				mikuLogsDO.setUnionid(profileWeChatDO.getUnionId());
	  				mikuLogsDO.setUserid(profileid);
	  				mikuLogsDOMapper.insert(mikuLogsDO);
  			    }
  			}
  		}
  		return openid+"##"+nprice;
  	}
}
