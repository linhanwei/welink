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
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
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
public class SearchLogisticsCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SearchLogisticsCallBack.class);

   
    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private ItemMapper itemMapper;

    @Resource
    private TradeMapper tradeMapper;
   
   
    @Resource
    private LogisticsDOMapper logisticsDOMapper;
    
    @Resource
    private MikuOrdersLogisticsDOMapper mikuOrdersLogisticsDOMapper;
    
    @Resource
    private Env env;
    
    
   
    
    //企业账号
    private final static String CUSTOMER="2F7152CDDB966BC938B39D4F34C0E630";
    private final static String KEY="isbVJXmV1665";
    
    /**
     * 快递100的账号
     */
    //免费的URL
    private static final String HTTPS_VERIFY_URL = "http://api.kuaidi100.com/api";
    //企业URL
    private static final String HTTPS_CallBack_URL="http://poll.kuaidi100.com/poll/query.do";
    
    
    //订阅URL
    private static final String DY_URL="http://poll.kuaidi100.com/poll";
    //回调的URL
    //生产环境
    private static final String callBack_URL = "http://" + BizConstants.ONLINE_DOMAIN + "/api/m/1.0/callBackLogisticsInfo.json";
//    private static final String CallBack_URL="http://miku.unesmall.com/api/m/1.0/callBackLogisticsInfo.json";
    //测试环境
    private static final String callBack_URL_Test = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/m/1.0/callBackLogisticsInfo.json";
    
    
   
    
    @RequestMapping(value = {"/api/m/1.0/searchLogisticsCallBack.json", "/api/m/1.0/searchLogisticsCallBack.htm", "/api/h/1.0/searchLogisticsCallBack.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
     String id=request.getParameter("tradeId");
   	 long tradeId = Long.parseLong(id);
   	 List<Trade> list=TradeSelectByTradeId(tradeId);
   	 String info="没有对应物流信息";
   	 WelinkVO welinkVO = new WelinkVO();
   	 List<OneOrderLogistic> oolist=new ArrayList<OneOrderLogistic>();
   	 //查看对应的专门管理的物流表信息
   	 List<MikuOrdersLogisticsDO> wlnewlist= getOrdersLogisticsInfo(tradeId);
   	 //进行解析对应的物流信息列表
   	 for(int i=0;i<wlnewlist.size();i++){
   		MikuOrdersLogisticsDO mikuOrdersLogisticsDO=wlnewlist.get(i);
   		OneOrderLogistic oneOrderLogistic=new OneOrderLogistic(); 
   		String orders=mikuOrdersLogisticsDO.getOrderIds();
   		//物流公司名称
   		String wlcName=mikuOrdersLogisticsDO.getWlcompany();
   		//物流公司简码
   		String wlcompanysimpleName=mikuOrdersLogisticsDO.getWlsnumber();
   		//物流号
   		String wlhnum=mikuOrdersLogisticsDO.getWlnumber();
   		//判断是否是主流的物流
   		if("1".equals(mikuOrdersLogisticsDO.getIsmainc()))
   		{
   		   welinkVO.setMsg("主流物流公司");
   		   if(mikuOrdersLogisticsDO.getStatus()==(byte)0)
   		   {
   			   //调的收费的物流接口
   			   //说明没有进行调用过的
  				info=getMainReturnInfo(wlcompanysimpleName,wlhnum);
  				//对快递信息进行获取
 				SearchLogisticBean bean=JSON.parseObject(info,SearchLogisticBean.class);
// 				400: 提交的数据不完整，或者贵公司没授权
// 				500: 表示查询失败，或没有POST提交
// 				501: 服务器错误，快递100服务器压力过大或需要升级，暂停服务
// 				502: 服务器繁忙，详细说明见2.2《查询接口并发协议》
// 				503: 验证签名失败。
// 				200: 返回正确的数据
 				if("200".equals(bean.getStatus()))
 				{
 					oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
 					List<data> dlist=bean.getData();
  					oneOrderLogistic.setWllist(dlist);
  					oneOrderLogistic.setState(bean.getState());
  					//进行订阅功能
  					//当状态值不为3的时候，也就是不是揽件的时候:则进行对应的订阅功能
  					if(!("3".equals(bean.getState()))){
  						//进行订阅的功能
  						String salt=UUID.randomUUID().toString();
  						String cbackUrl="";
  						if (env.isProd()) {
  							cbackUrl = callBack_URL;
  						}else{
  							cbackUrl = callBack_URL_Test;
  						}
  						String dyinfo=getRequestTasInfo(wlcompanysimpleName,wlhnum,salt,cbackUrl);
  						DyReturnInfo dyReturnInfo=JSON.parseObject(dyinfo,DyReturnInfo.class);
//								result: true表示成功，false表示失败
// 								returnCode:
//  							200: 提交成功
//  							701: 拒绝订阅的快递公司
//  							700: 订阅方的订阅数据存在错误（如不支持的快递公司、单号为空、单号超长等）
//  							600: 您不是合法的订阅者（即授权Key出错）
//  							500: 服务器错误（即快递100的服务器出理间隙或临时性异常，有时如果因为不按规范提交请求，比如快递公司参数写错等，也会报此错误）
//  							501:重复订阅（请格外注意，501表示这张单已经订阅成功且目前还在跟踪过程中（即单号的status=polling），快递100的服务器会因此忽略您最新的此次订阅请求，从而返回501。一个运单号只要提交一次订阅即可，若要提交多次订阅，请在收到单号的status=abort或shutdown后隔半小时再提交订阅，详见本文档第13页“重要提醒”部份说明）
  						if("true".equals(dyReturnInfo.getResult()) && "200".equals(dyReturnInfo.getReturnCode())){
  							mikuOrdersLogisticsDO.setIstsflag((byte)1);
  							mikuOrdersLogisticsDO.setSaltCode(salt);
  							mikuOrdersLogisticsDO.setCallbackUrl(cbackUrl);
  						}
  					}
  					updateOnemkldata(mikuOrdersLogisticsDO,bean);
 				}
 				else
 				{
 					 oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
  				}
   		   }
   		   else
   		   {
   			    //成功的状态 3
   			    if(mikuOrdersLogisticsDO.getState()==3){
					oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
				}else{
					//判断是否已经订阅
					if(mikuOrdersLogisticsDO.getIstsflag() == (byte)1){
						oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
					}
					//没有订阅到物流信息：需要做到的是实时
					else{
						//调用的是收费的物流接口:实时的快递100会返回对应的信息，所以这里还是应该调用的是数据库的信息
						boolean flag=getMainTime(mikuOrdersLogisticsDO.getLastUpdated());
						if(flag){
							info=getMainReturnInfo(wlcompanysimpleName,wlhnum);
							SearchLogisticBean bean=JSON.parseObject(info,SearchLogisticBean.class);
							if("200".equals(bean.getStatus()))
			 				{
			 					oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
			 					List<data> dlist=bean.getData();
			  					oneOrderLogistic.setWllist(dlist);
			  					oneOrderLogistic.setState(bean.getState());
			  					updateOnemkldata(mikuOrdersLogisticsDO,bean);
			 				}
			 				else
			 				{
			 					 oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
			  				}
						}else{
							 oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
						}
					}
				}
   		   }
   		 
   		 
   		}
   		//非主流物流公司
   		else{
   			welinkVO.setMsg("非主流物流公司");	 
   			//判断其对应的状态[是否已经操作了]
   			//0代表的是没有操作的数据
   			if(mikuOrdersLogisticsDO.getStatus()==(byte)0)
   			{
   				//说明没有进行调用过的
   				info=getReturnInfo(wlcName,wlhnum);
   				//对快递信息进行获取
  				SearchLogisticBean bean=JSON.parseObject(info,SearchLogisticBean.class);
  				if("1".equals(bean.getStatus()))
  				{
  					oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
  					List<data> dlist=bean.getData();
   					oneOrderLogistic.setWllist(dlist);
   					oneOrderLogistic.setState(bean.getState());
   					updateOnemkldata(mikuOrdersLogisticsDO,bean);
  				}
   			}
   			//1代表的是已经操作过的数据来的
   			else if(mikuOrdersLogisticsDO.getStatus()==(byte)1)
   			{
   				//state为3则为已揽件
   				if(mikuOrdersLogisticsDO.getState()==3){
   					oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
   				}else{
   					boolean flag=getTimeMinte(mikuOrdersLogisticsDO.getLastUpdated());
   	   				if(flag){
   	   				//说明没有进行调用过的
   	   	   			info=getReturnInfo(wlcompanysimpleName,wlhnum);
   	   	   			//对快递信息进行获取
   	  				SearchLogisticBean bean=JSON.parseObject(info,SearchLogisticBean.class);
	   	  				if("1".equals(bean.getStatus()))
	   	  				{
		   	  				oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
		   	   	   			List<data> dlist=bean.getData();
		   	   	   			oneOrderLogistic.setWllist(dlist);
		   	   	   			updateOnemkldata(mikuOrdersLogisticsDO,bean);
	   	  				}
		   	  			else
		  				{
		   	  				 //如果请求快递100没有的话则查数据库
		   	  				 oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
		   				}
   	   				}else{
   	   					oneOrderLogistic=getOneOrderLogic(mikuOrdersLogisticsDO,orders);
   	   				}
   				}
   			}
   		}
   		oolist.add(oneOrderLogistic);
   	}
   	 System.out.println(JSON.toJSONString(oolist));
   	 //封装封装
   	 Map finmap=new HashMap();
   	 for(int i=0;i<oolist.size();i++){
   		finmap.put(i+"", oolist.get(i));
   	 }
   	 if(oolist.size()>0){
   		welinkVO.setStatus(1); 
   	 }else{
   		welinkVO.setStatus(0); 
   	 }
   	 welinkVO.setResult(finmap);
  	 return JSON.toJSONString(welinkVO);
    }
    
    
    
    public OneOrderLogistic getOneOrderLogic(MikuOrdersLogisticsDO mikuOrdersLogisticsDO,String orders){
    	OneOrderLogistic oneOrderLogistic=new OneOrderLogistic();
    	List<data> dlist=new ArrayList<data>();
    	if(!("".equals(mikuOrdersLogisticsDO.getLogicSpecificAddr()) || mikuOrdersLogisticsDO.getLogicSpecificAddr()==null)){
    		dlist=JSON.parseArray(mikuOrdersLogisticsDO.getLogicSpecificAddr(), data.class);
    	}
		List<Item> allItemlist=allListData(orders);
		oneOrderLogistic.setCompanyName(mikuOrdersLogisticsDO.getWlcompany());
		oneOrderLogistic.setCompanyNum(mikuOrdersLogisticsDO.getWlnumber());
		//进行封装数据
		oneOrderLogistic.setItemist(allItemlist);
		oneOrderLogistic.setWllist(dlist);
		oneOrderLogistic.setState(mikuOrdersLogisticsDO.getState().toString());
		return oneOrderLogistic;
    }
    
    
    
    //订阅服务的请求
    public String getRequestTasInfo(String wlcompanysimpleName,String wlhnum,String salt,String cbackUrl) throws Exception{
    	TaskRequest req = new TaskRequest();
    	req.setCompany(wlcompanysimpleName);
		req.setNumber(wlhnum.replace("\u00A0",""));
		req.getParameters().put("callbackurl", cbackUrl);
		req.getParameters().put("salt", salt);
		req.setKey(KEY);
		HashMap<String, String> map = new HashMap<String, String>(); 
		map.put("schema", "json");
		map.put("param", JSON.toJSONString(req));
		String info=post(DY_URL,map);
		info=new String(info.getBytes("ISO-8859-1"),"utf-8");
		System.out.println(info);
    	return info;
    	
    }
    
    
    
    //比较2个时间段的相差值[非主流的物流]
    //让用户2个小时之内只查一次
    public boolean getTimeMinte(Date oldDate){
    	long num=(new Date()).getTime()-oldDate.getTime();
    	long result=num/(1000* 60 * 60);
    	if(result>2){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    //比较2个时间段的相差值[主流的物流]
    //让用户30分钟之内只查一次
    public boolean getMainTime(Date oldDate){
    	long num=(new Date()).getTime()-oldDate.getTime();
    	long result=num/(1000* 60);
    	if(result>30){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
    
    
    //进行更新物流单号的信息
    public void updateOnemkldata(MikuOrdersLogisticsDO mikuOrdersLogisticsDO,SearchLogisticBean bean){
    		mikuOrdersLogisticsDO.setState(Byte.parseByte(bean.getState()));
			mikuOrdersLogisticsDO.setLastUpdated(new Date());
			mikuOrdersLogisticsDO.setStatus((byte)1);
			List<data> dlist=bean.getData();
			System.out.println(JSON.toJSONString(dlist));
			mikuOrdersLogisticsDO.setLogicSpecificAddr(JSON.toJSONString(dlist));
//			mikuOrdersLogisticsDO.setMemo(JSON.toJSONString(dlist));
//			mikuOrdersLogisticsDOMapper.updateByPrimaryKey(mikuOrdersLogisticsDO);
			mikuOrdersLogisticsDOMapper.updateByPrimaryKeySelective(mikuOrdersLogisticsDO);
    }
    
    
    
    //根据orderids来对应orders的集合
    public List<Item> allListData(String str){
    	List<Item> itemlist=new ArrayList<Item>();
    	String[] arr=str.split(";");
    	for(String onestr:arr){
    		Long orderId=Long.parseLong(onestr);
    		Order order=OrderSelectByOrderId(orderId).get(0);
    		//不需要运费的物品来的
    		if(!("运费".equals(order.getTitle()))){
    			Item item=ItemSelectByItemId(order.getArtificialId()).get(0);
    			item.setNum(order.getNum());
    			itemlist.add(item);
    		}
    	}
    	return itemlist;
    }
    
   
    //非主流的物流公司回调的基本信息
    public String getReturnInfo(String wlgsnum,String wlhnum){
    		 String info="";
    		 Map<String,String> map=new HashMap<String,String>();
			 map.put("id", "62bfee818ebab2fe");
			 map.put("com", wlgsnum);
			 map.put("muti", "1");
			 map.put("nu", wlhnum.replace("\u00A0",""));
			 map.put("order", "desc");
			 map.put("valicode", "");
			 map.put("show", "0");
			 info=post(HTTPS_VERIFY_URL,map);
			 return info;
    }
    
    //主流的物流公司回调的基本信息
    //传递的方式:md5进行加密各个customer+key+params
    public String getMainReturnInfo(String wlgsnum,String wlhnum) throws Exception{
    	 String info="";
    	 LinitData linitData=new LinitData(wlgsnum,wlhnum.replace("\u00A0",""));
    	 String params=JSON.toJSONString(linitData);
    	 String sign=MD5.MD5Encode(params+KEY+CUSTOMER).toUpperCase();
		 Map<String,String> map=new HashMap<String,String>();
		 map.put("param",params);
		 map.put("sign",sign);
		 map.put("customer",CUSTOMER);
		 info=post(HTTPS_CallBack_URL,map);
		 info=new String(info.getBytes("ISO-8859-1"),"utf-8");
		 System.err.println(info);
		 return info;
    }
    
    
    
    
    //根据突然地Id来获取对应的MikuOrderLogistic的多条信息
    public List<MikuOrdersLogisticsDO> getOrdersLogisticsInfo(Long tradeId){
    	MikuOrdersLogisticsDOExample mikuOrdersLogisticsDOExample=new MikuOrdersLogisticsDOExample();
    	mikuOrdersLogisticsDOExample.createCriteria().andTradeIdEqualTo(tradeId);
    	List<MikuOrdersLogisticsDO> list=mikuOrdersLogisticsDOMapper.selectByExampleWithBLOBs(mikuOrdersLogisticsDOExample);
    	return list;
    }
    
    
    
    //根据tradeId来获取对应的order值
    public List<Item> getListItemList(Trade trade){
    	List<Item> itemlist=new ArrayList<Item>();
    	String str=trade.getOrders();
    	String[] arr=str.split(";");
    	for(String onestr:arr){
    		Long orderId=Long.parseLong(onestr);
    		Order order=OrderSelectByOrderId(orderId).get(0);
    		//不需要运费的物品来的
    		if(!("运费".equals(order.getTitle()))){
    			Item item=ItemSelectByItemId(order.getArtificialId()).get(0);
    			item.setNum(order.getNum());
    			itemlist.add(item);
    		}
    	}
    	return itemlist;
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
    
    //根据ID查找:trade的集合数据
    public List<Trade> TradeSelectByTradeId(Long tradeId){
    	  TradeExample tradeExample = new TradeExample();
          tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
          List<Trade> list=tradeMapper.selectByExample(tradeExample);
          return list;
    }
    
    
    //根据ID查找:Order的集合数据
    public List<Order> OrderSelectByOrderId(Long orderId){
    	OrderExample orderExample=new OrderExample();
    	orderExample.createCriteria().andIdEqualTo(orderId);
    	List<Order> list=orderMapper.selectByExample(orderExample);
    	return list;
    }
    
    //根据ID查找:Item的集合数据
    public List<Item> ItemSelectByItemId(Long itemId){
    	ItemExample itemExample=new ItemExample();
    	itemExample.createCriteria().andIdEqualTo(itemId);
    	List<Item> lisy=itemMapper.selectByExample(itemExample);
    	return lisy;
    }
    
    //根据ID进行查找:Logistics的集合数据
    public List<LogisticsDO>  LogisticsByLogId(Long id){
    	LogisticsDOExample logisticsDOExample=new LogisticsDOExample();
    	logisticsDOExample.createCriteria().andIdEqualTo(id);
    	List<LogisticsDO> list=logisticsDOMapper.selectByExample(logisticsDOExample);
    	return list;
    }
  
    
    //请求的是Http的post请求
    public static String post(String url, Map<String, String> maps) {
        // 第一步，创建HttpPost对象
        HttpPost httpPost = new HttpPost(url);
        // 设置HTTP POST请求参数必须用NameValuePair对象
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (params != null) {
            Set<String> keys = maps.keySet();
            for (String key : keys) {
                System.out.println(maps.get(key));
                params.add(new BasicNameValuePair(key, maps.get(key)));
            }
        }
        HttpResponse httpResponse = null;
        try {
            // 设置httpPost请求参数
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            httpResponse = new DefaultHttpClient().execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // 第三步，使用getEntity方法活得返回结果
                String result = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("result:" + result);
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

  
}
