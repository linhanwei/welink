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
public class AlipayBatchTransCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AlipayBatchTransCallBack.class);

    @Resource
    private AlipayBackDOMapper alipayBackDOMapper;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private ItemMapper itemMapper;

    @Resource
    private TradeMapper tradeMapper;
    
    @Resource
    private MikuGetpayDOMapper mikuGetpayDOMapper;
    
    @Resource
    private MikuShareGetpayDOMapper mikuShareGetpayDOMapper;
    
    @Resource
    private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
    
    @Resource
    private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
    
    /**
     * 支付宝消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";
   
   
    
    @RequestMapping(value = {"/api/m/1.0/alipayBatchTransCallBack.json", "/api/m/1.0/alipayBatchTransCallBack.htm", "/api/h/1.0/alipayBatchTransCallBack.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 Map paramMap = request.getParameterMap();
         Map<String, String> map = new HashMap<>();
         String result = "failed";
         log.info("支付宝企业付款的操作的开始....");
         log.info("返回的参数如下:");
         for (Object s : paramMap.keySet()) {
             String v = ((String[]) paramMap.get((String) s))[0];
             map.put((String) s, v);
             log.info((String) s+"--------------》"+v);
         }
         //添加写死的数据
//         map.put("notify_type","batch_trans_notify");
//         map.put("notify_id","7556s4add26555sd");
//         map.put("sign_type","MD5");
//         map.put("sign","8979das7dasdihqhewqh909ewrewrewryew8ry8y90dufwhfhdfh");
//         map.put("batch_no","9999665545545");
//         map.put("pay_user_id","2088002464631181");
//         map.put("pay_user_name","service@unescn.com");
//         map.put("pay_account_no","service@unescn.com");
//         map.put("success_details","310^15549463480^刘婷婷^1.25^S^null^200810248427067^20081024143652");
//         map.put("success_details","314^15549463480^刘婷婷^1.25^S^null^200810248427067^20081024143652|316^15549463480^刘婷婷^1.25^S^null^2008102454348427067^2008102414365244");
//         map.put("fail_details","318^15549463480^刘婷婷^1.25^F^TXN_RESULT_TRANSFER_OUT_CAN_NOT_EQUAL_IN^200810248427065^200810878724143651|310^15549463480^刘婷婷^1.25^F^TXN_RESULT_TRANSFER_OUT_CAN_NOT_EQUAL_IN^20081024842704545565^20081024143651");
         AlipayBatchTransBackDO apAlipayRefundBackDO=buildCallBackDO(map);
         if("".equals(apAlipayRefundBackDO.getNotifyId()) || apAlipayRefundBackDO.getNotifyId()==null){
        	 //连支付宝的响应都不能构成对应的参数，故返回的是失败
        	 result = "failed";
        	 log.info("连支付宝的响应都不能构成对应的参数，故返回的是失败");
         }
         else{
        	 //验证其notifyId的有效性
        	 //需要注意的是:当我们返回的success返回给支付宝的时候，则这个的notifyId就已经无效了~
        	 if("true".equals(getNotifyIdInfo(apAlipayRefundBackDO.getNotifyId()))){
//        		 if("true".equals(getNotifyIdInfo(apAlipayRefundBackDO.getNotifyId())) || true){
            	 //进行多笔的数量企业付款操作
            	 if((apAlipayRefundBackDO.getSuccessDetails()!=null && !("".equals(apAlipayRefundBackDO.getSuccessDetails()))) || (apAlipayRefundBackDO.getFailDetails()!=null && !("".equals(apAlipayRefundBackDO.getFailDetails())))){
            		 if(apAlipayRefundBackDO.getSuccessDetails()!=null && !("".equals(apAlipayRefundBackDO.getSuccessDetails()))){
            			//操作成功付款订单
             		 	String[] successArr=(apAlipayRefundBackDO.getSuccessDetails()).split("\\|");
             		 	log.info("=====================================================");
             		 	log.info(apAlipayRefundBackDO.getSuccessDetails());
             		 	log.info("=====================================================");
             		 	if(successArr.length>0){
             		 		for(int i=0;i<successArr.length;i++){
                     		 	String[] oneDetail=successArr[i].split("\\^");
                     		 	//获取对应的流水号
                     		 	String tradeNo=oneDetail[0];
                     		 	//金额
                     		 	Double num=Double.parseDouble(oneDetail[3])*100;
                     		 	Long price=new Double(num).longValue();
                     		 	//支付宝的交易号
                     		 	String alipayTradeNo=oneDetail[6];
                     		 	AlipayBackDO alipayBackDO=getAlipayContent(tradeNo);
                     		 	if("".equals(alipayBackDO) || alipayBackDO==null){
                     		 		result = "failed";
                     		 		log.info("回调找不到对应企业付款基本信息");
                     		 	}else{
                     		 		//验证其重复性
                     		 		if(!("Y".equals(alipayBackDO.getIsTotalFeeAdjust()))){
                     		 		//操作3张表
                         		 		//需要对4个表进行：MikuAgencyShareAccount  ProFitGetPay  MikuSalesRecord  AlipayBack
                         		 		//AlipayBack
                         		 		alipayBackDO.setSubject("提现完成.");
                         		 		alipayBackDO.setNotifyId(apAlipayRefundBackDO.getNotifyId());
                         		 		alipayBackDO.setTradeStatus("BATCH_TRANS_SUCCESS");
                         		 		alipayBackDO.setIsTotalFeeAdjust("Y");
                         		 		alipayBackDO.setUseCoupon("Y");
                         		 		alipayBackDO.setBuyerId(alipayTradeNo);
                         		 		alipayBackDO.setGmtPayment(new Date());
                         		 		
                         		 		//ProFitGetPay
                         		 		MikuGetpayDO mikuGetpayDO=mikuGetpaySelectById(alipayBackDO.getTrade_no()).get(0);
                         		 		mikuGetpayDO.setStatus((byte) 1);
                         		 		mikuGetpayDO.setLastUpated(new Date());
                         		 		mikuGetpayDOMapper.updateByPrimaryKey(mikuGetpayDO);
                         		 		//MikuAgencyShareAccount
                         		 		List<MikuShareGetpayDO> sharegetPayList=mikuShareGetpaySelectByGetpayId(Long.parseLong(tradeNo));
                         		 		MikuAgencyShareAccountDO mikuAgencyShareAccountDO=mikuAgencyShareAccountSelectById(mikuGetpayDO.getAgencyId()).get(0);
                         		 		Long successNum=0L;
                         		 		for(int j=0;j<sharegetPayList.size();j++){
                         		 			MikuShareGetpayDO oneshareData=sharegetPayList.get(j);
                         		 			//查找对应的Trade数据的状态值
                         		 			MikuSalesRecordDO onemsrecord=mikuSalesRecordSelectById(oneshareData.getSalesRecordId()).get(0);
                         		 			Trade trade=TradeSelectByTradeId(onemsrecord.getTradeId()).get(0);
                         		 			//进行对应状态的修改
                         		 			boolean nfalg=false;
                         		 			String orders=trade.getOrders();
                         		 			String[] arr=orders.split(";");
                         		 			for(int z=0;z<arr.length;z++){
                         		 				Long myid=Long.parseLong(arr[z]);
                         		 				if(myid>0L){
                         		 					Order order=OrderSelectByOrderId(myid).get(0);
                         		 					if(order!=null && !("运费".equals(order.getTitle()))){
                         		 						Item item=ItemSelectByItemId(order.getArtificialId()).get(0);
                         		 						if(item!=null && item.getIsrefund()==(byte)1 && trade.getStatus()!=(byte)20){
                         		 							nfalg=true;
                         		 						}
                         		 					}
                         		 				}
                         		 			}
                         		 			
                         		 			if (nfalg){
                         		 				if(trade.getStatus()==(byte)20){
                             		 				onemsrecord.setIsGetpay((byte) 1);
                             		 				successNum+=onemsrecord.getShareFee();
                             		 			}
                             		 			else if(trade.getStatus()==(byte)8){
                             		 				onemsrecord.setIsGetpay((byte) 2);
                             		 			}else{
                             		 				onemsrecord.setIsGetpay((byte)-1);
                             		 			}
                         		 			}else{
                         		 				successNum+=onemsrecord.getShareFee();
                         		 				onemsrecord.setIsGetpay((byte) 1);
                         		 			}
                         		 			
                         		 			mikuSalesRecordDOMapper.updateByPrimaryKey(onemsrecord);
                         		 		}
                         		 		if(successNum==price){
                         		 			log.info("再次核对是正确的....");
                         		 			alipayBackDO.setSign("核对正确");
                         		 		}
                         		 		else{
                         		 			alipayBackDO.setSign("核对不正确"+"审核的成功的是："+successNum+"提现的金额是:"+price);
                         		 		}
                         		 		alipayBackDOMapper.updateByPrimaryKey(alipayBackDO);
                         		 		//未提现金额
                         		 		mikuAgencyShareAccountDO.setNoGetpayFee(mikuAgencyShareAccountDO.getNoGetpayFee()-price);
                         		 		//已提现
                         		 		mikuAgencyShareAccountDO.setTotalGotpayFee(
                         		 				(null==mikuAgencyShareAccountDO.getTotalGotpayFee() ? 0L : mikuAgencyShareAccountDO.getTotalGotpayFee())+price);
                         		 		//提现中的数据
                         		 		mikuAgencyShareAccountDO.setGetpayingFee(mikuAgencyShareAccountDO.getGetpayingFee()-price);
                         		 		mikuAgencyShareAccountDO.setLastUpdated(new Date());
                         		 		mikuAgencyShareAccountDOMapper.updateByPrimaryKey(mikuAgencyShareAccountDO);
                     		 		}
                     		 	}
             		 		}
             		 	}
            		 }
            		 
            		 if((apAlipayRefundBackDO.getFailDetails()!=null && !("".equals(apAlipayRefundBackDO.getFailDetails())))){
         		 		//操作失败付款订单
             		 	String[] failArr=(apAlipayRefundBackDO.getFailDetails()).split("\\|");
             		 	if(failArr.length>0){
             		 		for(int z=0;z<failArr.length;z++){
             		 			String[] oneDetail=failArr[z].split("\\^");
                     		 	//获取对应的流水号
                     		 	String tradeNo=oneDetail[0];
                     		 	//失败原因
                     		 	String reason=oneDetail[5];
                     		 	//金额
                      		 	Double num=Double.parseDouble(oneDetail[3])*100;
                      		 	Long price=new Double(num).longValue();
                     		 	AlipayBackDO alipayBackDO=getAlipayContent(tradeNo);
                     		 	//支付宝的交易号
                     		 	String alipayTradeNo=oneDetail[6];
                     		 	if("".equals(alipayBackDO) || alipayBackDO==null){
                     		 		result = "failed";
                     		 		log.info("回调找不到对应企业付款基本信息");
                     		 	}else{
                     		 		alipayBackDO.setSubject("提现失败");
                     		 		alipayBackDO.setTradeStatus("BATCH_TRANS_Fail");
                     		 		alipayBackDO.setBuyerId(alipayTradeNo);
                     		 		alipayBackDO.setGmtPayment(new Date());
                     		 		alipayBackDO.setBody(reason);
                     		 		//进行对提现分润记录的处理
                     		 		String errorInfo=getErrorInfo(reason);
                      		 		//验证其重复性
                      		 		if(!("Y".equals(alipayBackDO.getIsTotalFeeAdjust()))){
                      		 			//ProFitGetPay
                          		 		MikuGetpayDO mikuGetpayDO=mikuGetpaySelectById(tradeNo).get(0);
                          		 		mikuGetpayDO.setStatus((byte) 2);
                          		 		mikuGetpayDO.setLastUpated(new Date());
                          		 		mikuGetpayDO.setErrorMemo(errorInfo);
                          		 		mikuGetpayDOMapper.updateByPrimaryKey(mikuGetpayDO);
//                      		 		if(true){
                      		 			//防止逻辑
                          		 		if(mikuAgencyShareAccountSelectById(mikuGetpayDO.getAgencyId()).size()>0){
                          		 			MikuAgencyShareAccountDO mikuAgencyShareAccountDO=mikuAgencyShareAccountSelectById(mikuGetpayDO.getAgencyId()).get(0);
                          		 			//提现中的数据
                              		 		mikuAgencyShareAccountDO.setGetpayingFee(mikuAgencyShareAccountDO.getGetpayingFee()-price);
                              		 		mikuAgencyShareAccountDO.setLastUpdated(new Date());
                          		 			mikuAgencyShareAccountDOMapper.updateByPrimaryKey(mikuAgencyShareAccountDO);
                          		 			alipayBackDO.setIsTotalFeeAdjust("Y");
                          		 		}
                          		 		List<MikuShareGetpayDO> sharegetPayList=mikuShareGetpaySelectByGetpayId(Long.parseLong(tradeNo));
                          		 		for(int j=0;j<sharegetPayList.size();j++){
                          		 			MikuShareGetpayDO oneshareData=sharegetPayList.get(j);
                          		 			//查找对应的Trade数据的状态值
                          		 			MikuSalesRecordDO onemsrecord=mikuSalesRecordSelectById(oneshareData.getSalesRecordId()).get(0);
                          		 			onemsrecord.setIsGetpay((byte) 2);
                          		 			onemsrecord.setLastUpdated(new Date());
                          		 			mikuSalesRecordDOMapper.updateByPrimaryKey(onemsrecord);
                          		 		}
                      		 		}
                      		 		alipayBackDOMapper.updateByPrimaryKey(alipayBackDO);
                     		 	}
             		 		}
             		 	}
         		 	}
            		 	result = "success";
        	 }else{
        		 result = "failed";
        		 log.info("不合格的企业付款的信息"); 
        	 }
        	 }else{
        		 result = "failed";
        		 log.info("返回的不是企业付款的信息");
        	 }
         }
    	return result;
    }
    private AlipayBatchTransBackDO buildCallBackDO(Map<String, String> map) {
//    	notify_time	通知时间	Date	通知发送的时间。格式为：yyyy-MM-dd HH:mm:ss。	不可空	2009-08-12 11:08:32
//    	notify_type	通知类型	String	通知的类型。	不可空	batch_refund_notify
//    	notify_id	通知校验ID	String	通知校验ID。	不可空	70fec0c2730b27528665af4517c27b95
//    	sign_type	签名方式	String	DSA、RSA、MD5三个值可选，必须大写。	不可空	MD5
//    	sign	签名	String	请参见签名验证。	不可空	b7baf9af3c91b37bef4261849aa76281
//    	batch_no	退款批次号	String	原请求退款批次号。	不可空	20060702001
//    	pay_user_id	付款账号ID	String	付款的支付宝账号对应的支付宝唯一用户号。 以2088开头的16位纯数字组成。	不可空	2088002464631181
//    	pay_user_name	付款账号姓名	String	付款账号姓名。	不可空	毛毛
//    	pay_account_no	付款账号	String	付款账号。	不可空	20880024646311810156
//    	success_details	转账成功的详细信息	String	批量付款中成功付款的信息。 格式为：流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^支付宝内部流水号^完成时间。 每条记录以“|”间隔。	可空	0315001^gonglei1@handsome.com.cn^龚本林^20.00^S^null^200810248427067^20081024143652|
//    	fail_details	转账失败的详细信息	String	批量付款中未成功付款的信息。 格式为：流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^支付宝内部流水号^完成时间。 每条记录以“|”间隔。	可空	0315006^xinjie_xj@163.com^星辰公司1^20.00^F^TXN_RESULT_TRANSFER_OUT_CAN_NOT_EQUAL_IN^200810248427065^20081024143651
//    	退手续费结果返回格式：交易号^退款金额^处理结果$退费账号^退费账户ID^退费金额^处理结果；
//    	不退手续费结果返回格式：交易号^退款金额^处理结果。
//    	若退款申请提交成功，处理结果会返回“SUCCESS”。若提交失败，退款的处理结果中会有报错码，参见即时到账批量退款业务错误码。	可空	2010031906272929^80^SUCCESS$jax_chuanhang@alipay.com^2088101003147483^0.01^SUCCESS
    	AlipayBatchTransBackDO apAlipayRefundBackDO=new AlipayBatchTransBackDO();
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
            if(StringUtils.equals("pay_user_id", s)){
            	apAlipayRefundBackDO.setPayUserId(v);
            }
            if(StringUtils.equals("pay_user_name", s)){
            	apAlipayRefundBackDO.setPayUserName(v);
            }
            if(StringUtils.equals("pay_account_no", s)){
            	apAlipayRefundBackDO.setPayAccountNo(v);
            }
            if(StringUtils.equals("success_details", s)){
            	apAlipayRefundBackDO.setSuccessDetails(v);
            }
            if(StringUtils.equals("fail_details", s)){
            	apAlipayRefundBackDO.setFailDetails(v);
            }
        }
        return apAlipayRefundBackDO;
    }
   
    
    //查询出一条回调的支付的信息
    public AlipayBackDO getAlipayContent(String tradeNo){
    	AlipayBackDO alipayBackDO =new AlipayBackDO();
    	AlipayBackDOExample alipayBackDOExample=new AlipayBackDOExample();
    	alipayBackDOExample.createCriteria().andTrade_noEqualTo(tradeNo).andSignTypeEqualTo("MD5").andBodyEqualTo("提现分润");
    	List<AlipayBackDO> list=alipayBackDOMapper.selectByExample(alipayBackDOExample);
    	if(list.size()>0){
    		alipayBackDO=list.get(0);
    	}
    	return alipayBackDO;
    }
   
   
    //根据ID进行查找的:miku_getpay的集合数据
    public List<MikuGetpayDO> mikuGetpaySelectById(String tradeNo){
    	MikuGetpayDOExample mikuGetpayDOExample=new MikuGetpayDOExample();
    	mikuGetpayDOExample.createCriteria().andIdEqualTo(Long.parseLong(tradeNo));
    	List<MikuGetpayDO> list=mikuGetpayDOMapper.selectByExample(mikuGetpayDOExample);
    	return list;
    }
    
    
    
    //根据代理ID进行查找:miku_agency_share_account的集合数据
    public List<MikuAgencyShareAccountDO> mikuAgencyShareAccountSelectById(Long id){
    	MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample=new MikuAgencyShareAccountDOExample();
		mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(id);
		List<MikuAgencyShareAccountDO> getpayList=mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
    	return getpayList;
    }
    
    
    //根据ID查找:miku_sales_record的集合数据
    public List<MikuSalesRecordDO> mikuSalesRecordSelectById(Long id){
    	MikuSalesRecordDOExample mikuSalesRecordDOExample=new MikuSalesRecordDOExample();
    	mikuSalesRecordDOExample.createCriteria().andIdEqualTo(id);
    	List<MikuSalesRecordDO> mikusalesList=mikuSalesRecordDOMapper.selectByExample(mikuSalesRecordDOExample);
    	return mikusalesList;
    }
    
    //根据ID查找:miku_share_get_pay的集合数据
    public List<MikuShareGetpayDO> mikuShareGetpaySelectByGetpayId(Long id){
    	MikuShareGetpayDOExample mikuShareGetpayDOExample=new MikuShareGetpayDOExample();
    	mikuShareGetpayDOExample.createCriteria().andGetpayIdEqualTo(id);
    	List<MikuShareGetpayDO> list=mikuShareGetpayDOMapper.selectByExample(mikuShareGetpayDOExample);
    	return list;
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
    
    //根据返回的错误编码来返回对应的错误信息
    public String getErrorInfo(String info){
    	Map<String,String> map=new HashMap<String,String>();
		map.put("UPLOAD_ISCONFIRM_ERROR", "复核参数错误");
		map.put("EMAIL_ACCOUNT_LOCKED", "您暂时无法使用此功能，请立即补全您的认证信息");
		map.put("BATCH_OUT_BIZ_NO_LIMIT_ERROR", "抱歉，上传文件的批次号必须为11~32位的数字、字母或数字与字母的组合");
		map.put("AMOUNT_FORMAT_ERROR", "抱歉，您上传的文件中，第二行第五列的金额不正确。格式必须为半角的数字，最高精确到分，金额必须大于0");
		map.put("PAYER_FORMAT_ERROR", "您上传的文件中付款账户格式错误");
		map.put("DAILY_QUOTA_LIMIT_EXCEED", "日限额超限");
		map.put("DETAIL_OUT_BIZ_NO_REPEATE", "同一批次中商户流水号重复");
		map.put("PAYER_ACCOUNT_IS_RELEASED", "付款账户名与他人重复，无法进行收付款。为保障资金安全，建议及时修改账户名");
		map.put("PAYEE_ACCOUNT_IS_RELEASED", "收款账户名与他人重复，无法进行收付款");
		map.put("BATCH_ID_NULL", "批次明细查询时批次ID为空");
		map.put("PARSE_DATE_ERROR", "到账户批次查询日期格式错误");
		map.put("ERROR_ACCESS_DATA", "无权访问该数据");
		map.put("ERROR_BALANCE_NULL", "用户余额不存在");
		map.put("ERROR_USER_INFO_NULL", "用户信息为空");
		map.put("ERROR_USER_ID_NULL", "用户名为空");
		map.put("ERROR_BATCH_ID_NULL", "批次ID为空");
		map.put("ERROR_BATCH_NO_NULL", "批次号为空");
		map.put("STATUS_NOT_VALID", "请等待该批次明细校验完成后再下载");
		map.put("USER_SERIAL_NO_ERROR", "商户流水号的长度不正确，不能为空或必须小于等于32个字符");
		map.put("USER_SERIAL_NO_REPEATE", "同一批次中商户流水号重复");
		map.put("RECEIVE_EMAIL_ERROR", "收款人EMAIL的长度不正确，不能为空或必须小于等于100个字符");
		map.put("RECEIVE_NAME_ERROR", "收款人姓名的长度不正确，不能为空或必须小于等于128个字符");
		map.put("RECEIVE_REASON_ERROR", "付款理由的长度不正确，不能为空或必须小于等于100个字符");
		map.put("RECEIVE_MONEY_ERROR", "收款金额格式必须为半角的数字，最高精确到分，金额必须大于0");
		map.put("RECEIVE_ACCOUNT_ERROR", "收款账户有误或不存在");
		map.put("RECEIVE_SINGLE_MONEY_ERROR", "收款金额超限");
		map.put("LINE_LENGTH_ERROR", "流水列数不正确，流水必须等于5列");
		map.put("SYSTEM_DISUSE_FILE", "用户逾期15天未复核，批次失败");
		map.put("MERCHANT_DISUSE_FILE", "用户复核不通过，批次失败");
		map.put("TRANSFER_AMOUNT_NOT_ENOUGH", "转账余额不足，批次失败");
		map.put("RECEIVE_USER_NOT_EXIST", "收款用户不存在");
		map.put("ILLEGAL_USER_STATUS", "用户状态不正确");
		map.put("ACCOUN_NAME_NOT_MATCH", "用户姓名和收款名称不匹配");
		map.put("ERROR_OTHER_CERTIFY_LEVEL_LIMIT", "收款账户实名认证信息不完整，无法收款");
		map.put("ERROR_OTHER_NOT_REALNAMED", "收款账户尚未实名认证，无法收款");
		map.put("用户撤销", "用户撤销");
		map.put("USER_NOT_EXIST", "用户不存在");
		map.put("RECEIVE_EMAIL_NAME_NOT_MATCH", "收款方email账号与姓名不匹配");
		map.put("SYSTEM_ERROR", "支付宝系统异常");
		String errorInfo=map.get(info);
		if(errorInfo== null || "".equals(errorInfo)){
			errorInfo="支付的不明错误";
		}
		return errorInfo;
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
