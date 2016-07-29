package com.welink.web.resource;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.tencent.common.Configure;
import com.tencent.common.HttpsRequest;
import com.tencent.common.Signature;
import com.tencent.common.XMLParser;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.BCrypt;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.service.UserService;
import com.welink.biz.service.RedPacketService;
import com.welink.biz.util.UserUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.MD5Util;
import com.welink.biz.wx.tenpay.util.PayCommonUtil;
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
import com.welink.web.common.model.ReportTransferData;

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
public class DoRedPacketGetPay {

	@Resource
    private MemcachedClient memcachedClient;
	
	@Resource
	private RedPacketService redPacketService;
	
	@Resource
	private MikuRedPackSettingDOMapper mikuRedPackSettingDOMapper;
	
	@Resource
    private MikuLogsDOMapper mikuLogsDOMapper;
	
	private final static int MCTime=1800;
	private final static String PAYURL="https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	
    @RequestMapping(value = {"/api/m/1.0/doredpacketgetpay.json", "/api/h/1.0/doredpacketgetpay.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	long profileId = -1;
    	org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        WelinkVO welinkVO = new WelinkVO();
        Map map=new HashMap();
        //不是对应的用户
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //回传过来的price 与 openid
//        System.out.println(request.getRealPath("/")+"==================================");
//        String strpath=this.getClass().getResource("").getPath();
//        String path2=getClass().getClassLoader().getResource("apiclient_cert.p12").getPath();
//        System.out.println("=================================="+strpath+"==================================");
//        System.out.println("=================================="+path2+"==================================");
//        String p3=this.getClass().getClassLoader().getResource("").getPath();
//        System.out.println("=================================="+p3+"==================================");
        String openid = request.getParameter("openid");
//        if(openid==null){
//        	openid="oKY0xsyug_QMDwFkAkrx06qHfNLo";
//        }
        String price = request.getParameter("price");
//        if(price==null){
//        	price="1";
//        }
        //进行提现的处理--->查对应的logs表进行比对
        boolean flag=getoneLogsObj(openid,profileId,price);
        if(flag){
        	int priceNum=Integer.parseInt(price)*100;
        	String rcode=dogetPayInfo(openid,priceNum);
        	if("SUCCESS".equals(rcode)){
            	welinkVO.setMsg("提现成功");
            	welinkVO.setStatus(2);
            	map.put("status", 2);
            	map.put("msg", "提现成功");
        	}
        	else{
        		welinkVO.setMsg("提现失败");
            	welinkVO.setStatus(3);
            	map.put("status", 3);
            	map.put("msg", "提现失败");
        	}
        }else{
        	welinkVO.setMsg("后台数据找不到,需要跟维护人员进行联系.");
        	welinkVO.setStatus(1);
        	map.put("status", 1);
        	map.put("msg", "需要跟维护人员进行联系.");
        }
        Long nowTime=(System.currentTimeMillis());
 		map.put("nowTime", nowTime.toString());
        welinkVO.setResult(map);
        welinkVO.setStatus(1);
        System.out.println(JSON.toJSONString(welinkVO));
        return JSON.toJSONString(welinkVO);
    }
    
    
    public boolean getoneLogsObj(String openid,Long profileid,String price){
    	MikuLogsDOExample mikuLogsDOExample=new MikuLogsDOExample();
    	mikuLogsDOExample.createCriteria().andOpenidEqualTo(openid).andUseridEqualTo(profileid);
    	List<MikuLogsDO> mikuList=mikuLogsDOMapper.selectByExample(mikuLogsDOExample);
    	Long nprice=Long.parseLong(price);
    	boolean flag=false;
    	for(int i=0;i<mikuList.size();i++){
    		if(nprice==mikuList.get(i).getPrice()){
    			flag=true;
    			break;
    		}
    	}
    	return flag;
    }
    
    public static String createSign(String characterEncoding, SortedMap<Object, Object> parameters, String key) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + key);
        String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }
    
    public String dogetPayInfo(String openid,int price) throws Exception, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException{
    	String path=getClass().getClassLoader().getResource("apiclient_cert.p12").getPath();
//    	path=path.substring(1,path.length());
    	System.out.println(path);
    	Configure configure=new Configure();
		Configure.setAppID("wx82d4b04a531ac1a3");
		Configure.setCertPassword("1242526802");
		//相对路径
		Configure.setCertLocalPath(path);
		//windows
//		Configure.setCertLocalPath("D:/cert/apiclient_cert.p12");
		//linux
//		Configure.setCertLocalPath("/opt/project/miku/deploy/apiclient_cert.p12");
		Configure.setIp("121.26.217.212");
		Configure.setKey("741cfd9021b2d0e7f4c883027513f153");
		//商务号
		Configure.setMchID("1242526802");
		Configure.setSubMchID("");
		
		ReportTransferData  reportTransferData=new ReportTransferData(UUID.randomUUID().toString(), openid,price);
//		  SortedMap<Object, Object> params = new TreeMap<Object, Object>();
//        params.put("mch_appid", "wx82d4b04a531ac1a3");
//        params.put("mchid", "miku");
//        params.put("nonceStr", reportTransferData.getNonce_str());
//        params.put("partner_trade_no", reportTransferData.getPartner_trade_no());
//        params.put("openid", openid);
//        params.put("amount", 100);
//        params.put("desc", "desc");
//        params.put("spbill_create_ip", "121.26.217.212");
//        params.put("check_name", "NO_CHECK");
//        String paySign = PayCommonUtil.createSign("UTF-8", params, "741cfd9021b2d0e7f4c883027513f153");
//        System.out.println(paySign);
//        System.out.println(reportTransferData.getSign());

		HttpsRequest httpsRequest=new HttpsRequest();
		String str=httpsRequest.sendPost(PAYURL, reportTransferData);
		System.out.println(str);
		if(str.indexOf("SUCCESS")>-1){
			return "SUCCESS";
		}else{
			return "FAIL";
		}
//		Map<String,Object> xmlObj=XMLParser.getMapFromXML(str);
//		String rcode=(String) xmlObj.get("return_code");
		
    }
}
