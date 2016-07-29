package com.welink.web.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.tencent.common.MD5;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.domain.CallbackLogInfo;
import com.welink.commons.domain.DyReturnInfo;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.LinitData;
import com.welink.commons.domain.LogisticsDO;
import com.welink.commons.domain.LogisticsDOExample;
import com.welink.commons.domain.MikuOrdersLogisticsDO;
import com.welink.commons.domain.MikuOrdersLogisticsDOExample;
import com.welink.commons.domain.OneOrderLogistic;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.SearchLogisticBean;
import com.welink.commons.domain.TaskRequest;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.data;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.LogisticsDOMapper;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuGetpayDOMapper;
import com.welink.commons.persistence.MikuOrdersLogisticsDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.MikuShareGetpayDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * Created by daniel on 15-03-31.
 */
@RestController
public class CallBackLogisticsInfo {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CallBackLogisticsInfo.class);

   
    @Resource
    private PlatformTransactionManager transactionManager;

    
    @Resource
    private MikuOrdersLogisticsDOMapper mikuOrdersLogisticsDOMapper;
    
    
   
    
    @RequestMapping(value = {"/api/m/1.0/callBackLogisticsInfo.json", "/api/m/1.0/callBackLogisticsInfo.htm", "/api/h/1.0/callBackLogisticsInfo.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	//就是callbackLogInfo的实体对象
    	String param=request.getParameter("param");
//    	param="{\"status\":\"polling\",\"billstatus\":\"got\",\"message\":\"\",\"lastResult\":{\"message\":\"ok\",\"state\":\"0\",\"status\":\"200\",\"condition\":\"F00\",\"ischeck\":\"0\",\"com\":\"yuantong\",\"nu\":\"201603241022\",\"data\":[{\"context\":\"上海分拨中心/装件入车扫描 \",\"time\":\"2012-08-28 16:33:19\",\"ftime\":\"2012-08-28 16:33:19\",\"status\":\"在途\",\"areaCode\":\"310000000000\",\"areaName\":\"上海市\"},{\"context\":\"上海分拨中心/下车扫描 \",\"time\":\"2012-08-27 23:22:42\",\"ftime\":\"2012-08-27 23:22:42\",\"status\":\"在途\",\"areaCode\":\"310000000000\",\"areaName\":\"上海市\"}]}}";
    	CallbackLogInfo callbackLogInfo=new CallbackLogInfo();
    	if(param!=null && !("".equals(param))){
    		callbackLogInfo=JSON.parseObject(param,CallbackLogInfo.class);
    		SearchLogisticBean searchLogisticBean=callbackLogInfo.getLastResult();
    		if(searchLogisticBean!=null && !("".equals(searchLogisticBean.getNu()))){
    			String nu=searchLogisticBean.getNu();
    			List<MikuOrdersLogisticsDO> list=getMkLogByNu(nu);
    			for(int i=0;i<list.size();i++){
    				MikuOrdersLogisticsDO mm=list.get(i);
    				mm.setDyStatus(callbackLogInfo.getStatus());
    				mm.setDyInfo(getCallBackStatusInfo(callbackLogInfo.getStatus()));
    				mm.setLastUpdated(new Date());
    				mm.setLogicSpecificAddr(JSON.toJSONString(searchLogisticBean.getData()));
    				mm.setIstsflag((byte)1);
    				mikuOrdersLogisticsDOMapper.updateByPrimaryKeySelective(mm);
    			}
    		}
    	}
    	return "success";
    }
    
    
    
    //对应的服务的信息回调信息的说明
    public String getCallBackStatusInfo(String info){
    	 Map<String,String> map=new HashMap<String,String>();
         map.put("polling", "监控中");
         map.put("shutdown", "结束");
         map.put("abort", "中止");
         map.put("updateall", "重新推送");
         String str="";
         str=map.get(info);
         if("".equals(str)){
        	 str="未知信息";
         }
         return str;
    }
    
    
    
    
    
    //进行更新物流单号的信息
    public void updateOnemkldata(MikuOrdersLogisticsDO mikuOrdersLogisticsDO,SearchLogisticBean bean){
    		mikuOrdersLogisticsDO.setState(Byte.parseByte(bean.getState()));
			mikuOrdersLogisticsDO.setLastUpdated(new Date());
			mikuOrdersLogisticsDO.setStatus((byte)1);
			List<data> dlist=bean.getData();
			System.out.println(JSON.toJSONString(dlist));
			mikuOrdersLogisticsDO.setLogicSpecificAddr(JSON.toJSONString(dlist));
			mikuOrdersLogisticsDOMapper.updateByPrimaryKeySelective(mikuOrdersLogisticsDO);
    }
    
    
    
    //通过的wlnumber来查找对应的物流订单信息
    public List<MikuOrdersLogisticsDO> getMkLogByNu(String nu){
    	MikuOrdersLogisticsDOExample mikuOrdersLogisticsDOExample=new MikuOrdersLogisticsDOExample();
    	mikuOrdersLogisticsDOExample.createCriteria().andWlnumberLike(nu);
    	List<MikuOrdersLogisticsDO> list=mikuOrdersLogisticsDOMapper.selectByExampleWithBLOBs(mikuOrdersLogisticsDOExample);
    	return list;
    }
    
   
    
    //根据突然地Id来获取对应的MikuOrderLogistic的多条信息
    public List<MikuOrdersLogisticsDO> getOrdersLogisticsInfo(Long tradeId){
    	MikuOrdersLogisticsDOExample mikuOrdersLogisticsDOExample=new MikuOrdersLogisticsDOExample();
    	mikuOrdersLogisticsDOExample.createCriteria().andTradeIdEqualTo(tradeId);
    	List<MikuOrdersLogisticsDO> list=mikuOrdersLogisticsDOMapper.selectByExampleWithBLOBs(mikuOrdersLogisticsDOExample);
    	return list;
    }
    
    
    
   
    
    //根据信息来进行查找对应的信息
    public String getLogicsInfo(Map<String,String> map){
    	 String info="";
    	 for (String key : map.keySet()) {
    		 info+=(key+"="+map.get(key));
    		 info+="&";
         }
    	return info;
    }
    
   
  
}
