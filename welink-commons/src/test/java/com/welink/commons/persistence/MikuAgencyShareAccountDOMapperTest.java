/**
 * Project Name:welink-web
 * File Name:MikuAgencyShareAccountDOMapperTest.java
 * Package Name:com.welink.web.test
 * Date:2015年11月4日下午10:54:45
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;

import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CategoryDO;
import com.welink.commons.domain.CategoryDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.utils.BigDecimalUtils;
import com.welink.commons.vo.MikuAgencyShareAccountVO;
import com.welink.commons.vo.MikuSalesRecordVO;

/**
 * ClassName:MikuAgencyShareAccountDOMapperTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月4日 下午10:54:45 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("test")
public class MikuAgencyShareAccountDOMapperTest {
	@Resource
    private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
    private CategoryDOMapper categoryDOMapper;
	
	@Resource
    private TradeMapper tradeMapper;
	
	@Resource
    private ProfileDOMapper profileDOMapper;
	
	@Resource
    private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Before
    public void setUp() throws Exception {
        MatcherAssert.assertThat(mikuAgencyShareAccountDOMapper, notNullValue());
    }

    @After
    public void tearDown() throws Exception {

    }
	
	@Test
	public void selectAllyByParam(){
		System.out.println("----------------------------------------------");
		List<MikuAgencyShareAccountVO> mikuAgencyShareAccountVOList = mikuAgencyShareAccountDOMapper.selectAllyByParam(2, 69577L, null,null, " contactsLevel asc ",100,0);	//69357L
		
		
		/*List<MikuAgencyShareAccountVO> mikuAgencyShareAccountVOList 
    	= mikuAgencyShareAccountDOMapper.selectAllyByParam(type, profileId, nickName,null,sz,startRow);*/
	    for(int i=0; i<mikuAgencyShareAccountVOList.size(); i++){
	    	setFee(mikuAgencyShareAccountVOList.get(i));	//设置金额分为元
	    }
	    
	    for(MikuAgencyShareAccountVO vo : mikuAgencyShareAccountVOList){
	    	System.out.println(vo.getId()+"....SalesFee."+vo.getpSalesFee() +"....ContactsLevel:"+vo.getContactsLevel());
	    }
			
		System.out.println("111111111111111111111111");
	}
	
	@Test
	public void selectCategory(){
		System.out.println("----------------------------------------------");
		CategoryDOExample cde = new CategoryDOExample();
		cde.createCriteria().andStatusEqualTo((byte) 1);
		cde.setOrderByClause("is_parent DESC, weight DESC");
		//cde.setOrderByClause("is_parent DESC");
        List<CategoryDO> categoryDOs = categoryDOMapper.selectByExample(cde);
        for(CategoryDO cate : categoryDOs){
        	System.out.println("id:"+cate.getId()+"..Name:"+cate.getName()+"..IsParent:"+cate.getIsParent()+"..Weight:"+cate.getWeight());
        }
		System.out.println("111111111111111111111111");
	}
	
	@Test
	public void sumByBuyer(){
		System.out.println("----------------------------------------------");
		Map paramsMap = new HashMap();
        paramsMap.put("profileId", 69377L);
        paramsMap.put("status", 7);	//一完成的订单
		//Map<String, Object> map = tradeMapper.sumByBuyer(69377L);
        Map<String, Object> map = tradeMapper.sumByBuyer(paramsMap);
		System.out.println("price..............................."+map.get("price")+"...."+map.get("totalFee"));
		System.out.println("111111111111111111111111");
	}
	
	//设置金额分为元
	private void setFee(MikuAgencyShareAccountVO mikuAgencyShareAccountVO){
		mikuAgencyShareAccountVO.setDirectSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getDirectSalesFee()));
		mikuAgencyShareAccountVO.setDirectShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getDirectShareFee()));
		mikuAgencyShareAccountVO.setIndirectSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getIndirectSalesFee()));
		mikuAgencyShareAccountVO.setIndirectShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getIndirectShareFee()));
		mikuAgencyShareAccountVO.setGetpayingFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getGetpayingFee()));
		mikuAgencyShareAccountVO.setNoGetpayFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getNoGetpayFee()));
		mikuAgencyShareAccountVO.setTotalGotpayFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getTotalGotpayFee()));
		mikuAgencyShareAccountVO.setpSalesFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getpSalesFee()));
		mikuAgencyShareAccountVO.setTotalShareFee(BigDecimalUtils.divFee100(mikuAgencyShareAccountVO.getTotalShareFee()));
	}
	
	@Test
	public void selectSalesRecordList(){
		System.out.println("----------------------------------------------");
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("profileId", 70833L);			//代理商id
		//paramMap.put("isGetpay", (byte)-1);	//未提现
		//paramMap.put("timeoutActionTime", new Date());	//交易过期时间
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		//paramMap.put("tradeStatus", (byte)20);	//交易完成
		paramMap.put("isCanGetPay", 1);	//提现 (0=不可提现;1=可提现)
		List<MikuSalesRecordDO> mikuSalesRecordDOList = mikuSalesRecordDOMapper.selectSalesRecordList(paramMap);	//可提现分润
		
		System.out.println("mikuSalesRecordDOList.size..............................."+mikuSalesRecordDOList.size());
		System.out.println("111111111111111111111111");
		Long share = 0L;
		for(MikuSalesRecordDO vo : mikuSalesRecordDOList){
			System.out.println("tradeId:"+vo.getTradeId()+"--------------"+vo.getShareFee());
			share += vo.getShareFee();
		}
		System.out.println("--------ShareFee-"+share);
	}
	
	@Test
	public void selectSalesRecordVOList(){
		System.out.println("----------------------------------------------");
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("profileId", 71950L);			//代理商id
		//paramMap.put("isGetpay", (byte)-1);	//未提现
		//paramMap.put("timeoutActionTime", new Date());	//交易过期时间
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		//paramMap.put("tradeStatus", (byte)20);	//交易完成
		paramMap.put("isCanGetPay", 1);	//提现 (0=不可提现包括退货的;1=可提现;2=余额中的不可提现;3余额)
		List<MikuSalesRecordVO> mikuSalesRecordVOList = mikuSalesRecordDOMapper.selectSalesRecordVOList(paramMap);	//可提现分润
		
		System.out.println("mikuSalesRecordDOList.size..............................."+mikuSalesRecordVOList.size());
		System.out.println("111111111111111111111111");
		Long share = 0L;
		//不可提现((tr.status != 20  and it.isrefund=1) or o.return_status = 5 or msr.is_getpay = 1 or tr.crowdfund_refund_status != 0)
		//可提现(tr.status = 20 or it.isrefund=0) and msr.is_getpay != 1 and o.return_status != 5 and tr.crowdfund_refund_status = 0
		Long canGetShare = 0L;
		Long noCanGetShare = 0L;
		Long hasGetShare = 0L;
		//1:订单未完成；2：订单完成；3：已提现；4
		/*for(MikuSalesRecordVO vo : mikuSalesRecordVOList){
			//Constants.ReturnGoodsStatus.FINISHED.getStatusId() 已退货:5
			//Constants.GetPayStatus.GETPAYED.getStatusId()	未提现:-1
			if((!vo.getTradeStatus().equals((byte)20) && vo.getIsrefund().equals((byte)1))
					|| vo.getOrderReturnStatus().equals((byte)5)
					|| vo.getIsGetpay().equals((byte)-1)
					|| !vo.getCrowdfundRefundStatus().equals((byte)0)){
				//不可提现
				vo.setIsCanGetPay(0);
				noCanGetShare += Long.valueOf(null == vo.getShareFee() ? "0" : vo.getShareFee());
			}else if((vo.getTradeStatus().equals((byte)20) || vo.getIsrefund().equals((byte)0))
					&& !vo.getOrderReturnStatus().equals((byte)5)
					&& !vo.getIsGetpay().equals((byte)-1)
					&& !vo.getCrowdfundRefundStatus().equals((byte)0)){
				//可提现
				vo.setIsCanGetPay(1);
				canGetShare += Long.valueOf(null == vo.getShareFee() ? "0" : vo.getShareFee());
			}
			if(vo.getIsGetpay().equals((byte)1)){
				hasGetShare += Long.valueOf(null == vo.getShareFee() ? "0" : vo.getShareFee());
			}
			//share += Long.valueOf(null == vo.getShareFee() ? "0" : vo.getShareFee());
		}*/
		for(MikuSalesRecordVO vo : mikuSalesRecordVOList){
			canGetShare += Long.valueOf(vo.getShareFee());
			System.out.println("tradeId:"+vo.getTradeId()+".....ItemName:"+vo.getItemName()+"--------------"+vo.getShareFee());
		}
		System.out.println("----------------------------------------------------canGetShare---:"+canGetShare);
	}
	
	@Test
	public void selectSalesRecordGroupByTradeList(){
		System.out.println("----------------------------------------------");
		
		List<Byte> isGetpaysList = new ArrayList<Byte>();
		isGetpaysList.add((byte)-1);
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("profileId", 71429L);			//代理商id
		//paramMap.put("isGetpay", Constants.GetPayStatus.NOGETPAY.getStatusId());	//未提现
		//paramMap.put("isGetpays", isGetpaysList);	//提现状态
		/*paramMap.put("isCrowdfundRefundStatus", 0);	//是否众筹退款	//0=不用众筹退款;1=众筹退款的
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		//paramMap.put("tradeStatus", Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());	//交易完成
		paramMap.put("tradeReturnStatus", (byte)0);	//交易退货状态*/	
	
		paramMap.put("isCrowdfundRefundStatus", 0);	//是否众筹退款	//0=不用众筹退款;1=众筹退款的
		paramMap.put("isReturnGood", 0);			//是否退货	//0=没退货;1=已退货
		//paramMap.put("tradeReturnStatus", (byte)0);	//交易退货状态
		paramMap.put("isCanGetPay", 0);	//isCanGetPay(0=不可提现;1=可提现)
		
		List<MikuSalesRecordVO> mikuSalesRecordVOList = mikuSalesRecordDOMapper.selectSalesRecordGroupByTradeList(paramMap);
		System.out.println("mikuSalesRecordDOList.size..............................."+mikuSalesRecordVOList.size());
		MikuSalesRecordVO mikuSalesRecordVO = null;
		for(int i=0; i<mikuSalesRecordVOList.size(); i++){
			if(null != mikuSalesRecordVOList.get(i)){
				//BeanUtils.copyProperties(mikuSalesRecordDOList.get(i), mikuSalesRecordVO);
				mikuSalesRecordVO = mikuSalesRecordVOList.get(i);
				String price = mikuSalesRecordVOList.get(i).getPrice();
				mikuSalesRecordVO.setPrice(BigDecimalUtils.divFee100(
						mikuSalesRecordVOList.get(i).getPrice()));	//设置金额分为元
				mikuSalesRecordVO.setAmount(BigDecimalUtils.divFee100(
						mikuSalesRecordVOList.get(i).getAmount()));	//设置金额分为元
				mikuSalesRecordVO.setShareFee(BigDecimalUtils.divFee100(
						mikuSalesRecordVOList.get(i).getShareFee()));	//设置金额分为元
				mikuSalesRecordVOList.add(mikuSalesRecordVO);
			}else{
				mikuSalesRecordVOList.remove(mikuSalesRecordVOList.get(i));
			}
		}
		
		System.out.println("111111111111111111111111");
	}
	
	@Test
	public void agencyShareAccountSelectAllyByParam(){
		System.out.println("----------------------------------------------");
		List<MikuAgencyShareAccountVO> mikuAgencyShareAccountVOList 
			= mikuAgencyShareAccountDOMapper.selectAllyByParam(2, 70468L, null, null, " contactsLevel asc,asa.date_created asc ",300,0);
		for(int i=0; i<mikuAgencyShareAccountVOList.size(); i++){
        	setFee(mikuAgencyShareAccountVOList.get(i));	//设置金额分为元
        	System.out.println(".................................ContactsLevel:"+mikuAgencyShareAccountVOList.get(i).getContactsLevel()
        			+"-------------DateCreated:"+mikuAgencyShareAccountVOList.get(i).getDateCreated());
        }
		System.out.println("-------------------mikuAgencyShareAccountVOList:"+mikuAgencyShareAccountVOList.size());
	}
	
	@Test
	public void getReturnSaleRecordVOList(){
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("profileId", 78860L);			//代理商id
		paramMap.put("orderByClause", " sr.return_date DESC ");
		paramMap.put("offset", 0);
		paramMap.put("limit", 50);
		List<MikuSalesRecordVO> returnSaleRecordVOList = mikuSalesRecordDOMapper.getReturnSaleRecordVOList(paramMap);
		Long shareFee = 0L;
		System.out.println("-------------------mikuAgencyShareAccountVOList:"+returnSaleRecordVOList.size());
		for(MikuSalesRecordVO vo : returnSaleRecordVOList){
			shareFee += Long.valueOf(vo.getShareFee());
			System.out.println("-----"+vo.getItemPicUrls());
		}
		System.out.println("-------------------shareFee:"+shareFee);	//1900
	}
	
	@Test
	public void sumReturnSaleRecord(){
		Map<String,Object> sumReturnSaleRecordParamMap = new HashMap<String, Object>();
        sumReturnSaleRecordParamMap.put("profileId", 78860L);
        Long sumReturnSaleRecord = mikuSalesRecordDOMapper.sumReturnSaleRecord(sumReturnSaleRecordParamMap);
        String sumReturnSaleRecordStr = "0.00";
        if(null != sumReturnSaleRecord && sumReturnSaleRecord > 0L){
        	sumReturnSaleRecordStr = BigDecimalUtils.divFee100(sumReturnSaleRecord);
        }
        if(sumReturnSaleRecord > 0L){
        	System.out.println("=====================sumReturnSaleRecord(=="+sumReturnSaleRecord);
        	System.out.println("=====================ShareFee(=="+sumReturnSaleRecordStr);
        }else{
        	System.out.println("---------------------sumReturnSaleRecord------------------------");
        }
	}
	
	@Test
	public void updateProfile(){
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(78887L);
		profileDO.setNickname("111115287");
		profileDOMapper.updateByPrimaryKeySelective(profileDO);
	}
	
	
}

