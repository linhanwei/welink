package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.MSG.SMSUtils;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipayNotify;
import com.welink.biz.service.GivePresentService;
import com.welink.biz.service.MikuOneBuyService;
import com.welink.biz.service.UsePromotionService;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.TimeUtil;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.commons.TradeUtil;
import com.welink.commons.domain.*;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.*;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.ons.TradeMessageProcessFacade;
import com.welink.web.ons.events.TradeEventType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by daniel on 14-10-27.
 */
@RestController
public class AlipayRefundCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AlipayRefundCallBack.class);

    private static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISH = "TRADE_FINISHED";
    
    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private TradeMapper tradeMapper;
    
    @Resource
    private MikuReturnGoodsDOMapper mikuReturnGoodsDOMapper;
    
    /**
     * 支付宝消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";

   
    
    @RequestMapping(value = {"/api/m/1.0/alipayRefundCallBack.json", "/api/m/1.0/alipayRefundCallBack.htm", "/api/h/1.0/alipayRefundCallBack.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 Map paramMap = request.getParameterMap();
         Map<String, String> map = new HashMap<>();
         String result = "failed";
         log.info("支付宝退款的操作的开始....");
         log.info("返回的参数如下:");
         for (Object s : paramMap.keySet()) {
             String v = ((String[]) paramMap.get((String) s))[0];
             map.put((String) s, v);
             log.info((String) s+"--------------》"+v);
         }
         //添加写死的数据
//         map.put("notify_type","batch_refund_notify");
//         map.put("notify_id","7556s4add26555sd");
//         map.put("sign_type","MD5");
//         map.put("sign","8979das7dasdihqhewqh909ewrewrewryew8ry8y90dufwhfhdfh");
//         map.put("batch_no","9999665545545");
//         map.put("success_num","1");
//         map.put("result_details","2016021821001004970203597282^0.01^SUCCESS");
         AlipayRefundBackDO apAlipayRefundBackDO=buildCallBackDO(map);
         if("".equals(apAlipayRefundBackDO.getNotifyId()) || apAlipayRefundBackDO.getNotifyId()==null){
        	 //连支付宝的响应都不能构成对应的参数，故返回的是失败
        	 result = "failed";
        	 log.info("连支付宝的响应都不能构成对应的参数，故返回的是失败");
         }
         else{
        	 //校验notifyId
        	 if("true".equals(getNotifyIdInfo(apAlipayRefundBackDO.getNotifyId()))){
        		 //进行的是一笔一笔的退款
            	 //开始校验的是:Sign
            	 //条件为:交易号^退款金额^处理结果
            	 if(apAlipayRefundBackDO.getResultDetails().indexOf("SUCCESS")>-1){
            		 	String[] arr=(apAlipayRefundBackDO.getResultDetails()).split("\\^");
            		 	//获取对应的交易号
            		 	String tradeNo=arr[0];
            		 	//找到对应的alipay表中的数据
            		 	AlipayBackDO alipayBackDO=getAlipayContent(tradeNo);
            		 	if("".equals(alipayBackDO) || alipayBackDO==null){
            		 		result = "failed";
            		 		log.info("回调找不到对应的支付信息");
            		 	}else{
            		 		//找到aplipay数据再进行改变对应的状态
            		 		alipayBackDO.setSign(apAlipayRefundBackDO.getSign());
            		 		alipayBackDO.setNotifyId(apAlipayRefundBackDO.getNotifyId());
            		 		alipayBackDO.setSellerId(apAlipayRefundBackDO.getBatchNo());
            		 		alipayBackDO.setNotifyTime(apAlipayRefundBackDO.getNotifyTime());
            		 		alipayBackDO.setNotifyType(apAlipayRefundBackDO.getNotifyType());
            		 		alipayBackDO.setIsTotalFeeAdjust("Y");
            		 		alipayBackDO.setBuyerId(apAlipayRefundBackDO.getBatchNo());
            		 		alipayBackDO.setSubject("退款完成");
            		 		alipayBackDOMapper.updateByPrimaryKey(alipayBackDO);
            		 		//进行对订单的状态也进行修改
            		 		TradeExample tradeExample = new TradeExample();
            	            tradeExample.createCriteria().andTradeIdEqualTo(Long.parseLong(alipayBackDO.getOutTradeNo()));
            	            List<Trade> trades = tradeMapper.selectByExample(tradeExample);
            	            if(trades.size()>0){
            	            	//把对应的订单进行的关闭
            	            	Trade trade=trades.get(0);
            	            	trade.setCrowdfundRefundStatus((byte)2); //已退货
            	            	trade.setStatus((byte) 8);
            	            	tradeMapper.updateByPrimaryKey(trade);
            	            	
            	            	//进行分润信息的修改
            	            	//进行order表的操作
            	            	List<Order> orderList=OrderSelectByOrderId(trade.getTradeId());
            	            	if(orderList.size()>0){
            	            		for(int i=0;i<orderList.size();i++){
            	            			Order order=orderList.get(i);
            	            		    order.setReturnStatus((byte)5);
            	            		    orderMapper.updateByPrimaryKey(order);
            	            		}
            	            	}
            	            	//退货信息表
            	            	List<MikuReturnGoodsDO> mrtgdlist=mikuReturnGoodsById(trade.getTradeId());
            	            	if(mrtgdlist.size()>0){
            	            		for(int j=0;j<mrtgdlist.size();j++){
            	            			MikuReturnGoodsDO onemikugood=mrtgdlist.get(j);
            	            			onemikugood.setStatus((byte)5);
            	            			onemikugood.setFinishTime(new Date());
            	            			mikuReturnGoodsDOMapper.updateByPrimaryKey(onemikugood);
            	            		}
            	            	}
            	            }
            	            result = "success";
            	            log.info("退款完成");
            		 	}
            	 }else{
            		 result = "failed";
            		 log.info("返回的不是退款完成的信息");
            	 }
        	 }else{
        		 result = "failed";
        		 log.info("返回是无效的支付宝信息");
        	 }
         }
    	return result;
    }
    private AlipayRefundBackDO buildCallBackDO(Map<String, String> map) {
//    	notify_time	通知时间	Date	通知发送的时间。格式为：yyyy-MM-dd HH:mm:ss。	不可空	2009-08-12 11:08:32
//    	notify_type	通知类型	String	通知的类型。	不可空	batch_refund_notify
//    	notify_id	通知校验ID	String	通知校验ID。	不可空	70fec0c2730b27528665af4517c27b95
//    	sign_type	签名方式	String	DSA、RSA、MD5三个值可选，必须大写。	不可空	MD5
//    	sign	签名	String	请参见签名验证。	不可空	b7baf9af3c91b37bef4261849aa76281
//    	batch_no	退款批次号	String	原请求退款批次号。	不可空	20060702001
//    	success_num	退款成功总数	String	退交易成功的笔数。0<= success_num<= 总退款笔数。	不可空	2
//    	result_details	退款结果明细	String	退款结果明细：
//    	退手续费结果返回格式：交易号^退款金额^处理结果$退费账号^退费账户ID^退费金额^处理结果；
//    	不退手续费结果返回格式：交易号^退款金额^处理结果。
//    	若退款申请提交成功，处理结果会返回“SUCCESS”。若提交失败，退款的处理结果中会有报错码，参见即时到账批量退款业务错误码。	可空	2010031906272929^80^SUCCESS$jax_chuanhang@alipay.com^2088101003147483^0.01^SUCCESS
    	AlipayRefundBackDO apAlipayRefundBackDO=new AlipayRefundBackDO();
        for (String s : map.keySet()) {
            String v = map.get(s);
            if (StringUtils.equals("notify_time", s)) {
                try {
                	apAlipayRefundBackDO.setNotifyTime(TimeUtil.str2DateTime(v));
                } catch (Exception e) {
                    log.error("alipay call back parse notify_time error. notify_time:" + v);
                }
            }
            if (StringUtils.equals("notify_type", s)) {
            	apAlipayRefundBackDO.setNotifyType(v);
            }
            if (StringUtils.equals("notify_id", s)) {
            	apAlipayRefundBackDO.setNotifyId(v);
            }
            if (StringUtils.equals("sign_type", s)) {
            	apAlipayRefundBackDO.setSignType(v);
            }
            if (StringUtils.equals("sign", s)) {
            	apAlipayRefundBackDO.setSign(v);
            }
            if(StringUtils.equals("batch_no", s)){
            	apAlipayRefundBackDO.setBatchNo(v);
            }
            if(StringUtils.equals("success_num", s)){
            	apAlipayRefundBackDO.setSuccessNum(v);
            }
            if(StringUtils.equals("result_details", s)){
            	apAlipayRefundBackDO.setResultDetails(v);
            }
        }
        return apAlipayRefundBackDO;
    }
    
    
   
    public AlipayBackDO getAlipayContent(String tradeNo){
    	AlipayBackDO alipayBackDO =new AlipayBackDO();
    	AlipayBackDOExample alipayBackDOExample=new AlipayBackDOExample();
    	alipayBackDOExample.createCriteria().andTrade_noEqualTo(tradeNo).andSignTypeEqualTo("MD5").andBodyEqualTo("退款").andTradeStatusEqualTo("TRADE_REFOUND");
    	List<AlipayBackDO> list=alipayBackDOMapper.selectByExample(alipayBackDOExample);
    	if(list.size()>0){
    		alipayBackDO=list.get(0);
    	}
    	return alipayBackDO;
    }
    
    
  //根据ID查找:Order的集合数据
    public List<Order> OrderSelectByOrderId(Long orderId){
    	OrderExample orderExample=new OrderExample();
    	orderExample.createCriteria().andTradeIdEqualTo(orderId);
    	List<Order> list=orderMapper.selectByExample(orderExample);
    	return list;
    }
    
    
    public List<MikuReturnGoodsDO>  mikuReturnGoodsById(Long tradeId){
    	MikuReturnGoodsDOExample mikuReturnGoodsDOExample=new MikuReturnGoodsDOExample();
    	mikuReturnGoodsDOExample.createCriteria().andTradeIdEqualTo(tradeId);
    	List<MikuReturnGoodsDO> list=mikuReturnGoodsDOMapper.selectByExample(mikuReturnGoodsDOExample);
    	return list;
    }
    
    
    
    
    
    
    
    
  //校验notifyid是否有效
    public String getNotifyIdInfo(String notify_id){
    	 String veryfy_url = HTTPS_VERIFY_URL + "partner=" + AlipayConfig.partner + "&notify_id=" + notify_id;
         return checkUrl(veryfy_url);
    }
    
    /**
     * 获取远程服务器ATN结果
     *
     * @param urlvalue 指定URL路径地址
     * @return 服务器ATN结果
     * 验证结果集：
     * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空
     * true 返回正确信息
     * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
     */
    private static String checkUrl(String urlvalue) {
        String inputLine = "";

        try {
            URL url = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                    .getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception e) {
            e.printStackTrace();
            inputLine = "";
        }

        return inputLine;
    }
}
