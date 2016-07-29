package com.welink.web.test;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.api.WxMpService;
import com.google.common.collect.Lists;
import com.welink.biz.common.MSG.PushApiService;
import com.welink.biz.common.model.*;
import com.welink.biz.service.*;
import com.welink.biz.util.OpenSearchType;
import com.welink.biz.util.UserUtils;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.service.CategoryService;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.commons.TradeUtil;
import com.welink.commons.domain.CategoryDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.MikuScratchCardDO;
import com.welink.commons.domain.MikuScratchCardDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.*;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.ons.events.TradeEventType;
import com.welink.web.resource.AlipayCallBack;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.notNullValue;


/**
 * Created by daniel on 14-9-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("test")
public class ServiceTest {

    @Resource
    private AnnouceService annouceService;
    @Resource
    private AppointmentTradeService appointmentTradeService;

    @Resource
    private PushService pushService;

    @Resource
    private UserService userService;

    @Resource
    private ComplainService complainService;

    @Resource
    private AroundNumberService aroundNumberService;

    @Resource
    private ConversationService conversationService;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private UserUtils userUtils;

    @Resource
    private AnnouceDOMapper annouceDOMapper;

    @Resource
    private MessageSummaryDOMapper messageSummaryDOMapper;

    @Resource
    private BizMessageMapper bizMessageMapper;

    @Resource
    private CategoryDOMapper categoryDOMapper;

    @Resource
    private CommunityDOMapper communityDOMapper;

    @Resource
    private CategoryService categoryService;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private PushApiService pushApiService;

    @Resource
    private CommunityService communityService;


    @Resource
    private ItemService itemService;

    public String url;

    @Resource
    private WxMpService wxMpService;

    @Resource
    private ItemSearchService itemSearchService;
    
    @Resource
    private AlipayCallBack alipayCallBack;
    
    @Resource
    private MikuScratchCardDOMapper mikuScratchCardDOMapper;
    
    @Resource
    private OrderMapper orderMapper;

    @Before
    public void setup() {
        MatcherAssert.assertThat(annouceService, notNullValue());
    }


    @Test
    public void testAccuracy() throws IOException {


        SearchResult<Item> itemSearchResult = null;
        itemSearchResult = itemSearchService.defaultSearch(1003l, null, 20000025l, null, 0, 100, OpenSearchType.RECOMMEND_DESC, null, true);

        itemSearchResult = itemSearchService.defaultSearch(1003l, null, 20000026l, null, 0, 100, OpenSearchType.RECOMMEND_DESC, null, true);

        itemSearchResult = itemSearchService.defaultSearch(1003l, null, 20000027l, null, 0, 100, OpenSearchType.RECOMMEND_DESC, null, true);


        WelinkVO welinkVO = new WelinkVO();
        long shopId = 1003l;
        List<CategoryDO> categoryDOList = categoryService.fetchFreshCatesWithCache();
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        List<CategoryViewDO> categoryViewDOs = new ArrayList<>();
        if (null != categoryDOList && categoryDOList.size() > 0) {
            for (CategoryDO c : categoryDOList) {
                CategoryViewDO cvDO = ViewDOCopy.buildCategoryViewDO(c);
                List<Banner> banners = new ArrayList<>();
                Banner banner = new Banner();
                banner.setAct(1);
                banner.setType(1);
                banner.setUr("http://welinklife.b0.upaiyun.com/1/LTE=/SVRFTS1QVUJMSVNI/MA==/20141125/vNTT-0-1416917291815.jpg");
                banner.setRedirectUrl("http://welinklife.b0.upaiyun.com/1/LTE=/SVRFTS1QVUJMSVNI/MA==/20141125/vNTT-0-1416917291815.jpg");
                banners.add(banner);
                cvDO.setBanners(banners);
                List<com.welink.commons.domain.Item> items = itemService.searchOpenSearchItems(shopId, null, c.getId(), null, 0, 1000, OpenSearchType.RECOMMEND_DESC.getType(), null, true);

                itemSearchResult = itemSearchService.defaultSearch(shopId, null, c.getId(), null, 0, 1000, OpenSearchType.RECOMMEND_DESC, null, true);
                if (null != itemSearchResult && itemSearchResult.isSuccess()) {
                    cvDO.setCount(itemSearchResult.getResult().getTotal());
                }
                categoryViewDOs.add(cvDO);
            }
        }

        resultMap.put("cates", categoryViewDOs);
        resultMap.put("suggest", "草莓");
        welinkVO.setResult(resultMap);
        System.out.println("============================");
        System.out.println(JSON.toJSON(welinkVO));
        System.out.println("============================");
        MatcherAssert.assertThat(annouceService, notNullValue());
    }
    
    @Test
    public void appointServiceDeal() throws IOException {
    	Trade trade2 = tradeMapper.selectByPrimaryKey(6558L); 
    	 //获取参数
        	
        	if(null != trade2 && null != trade2.getBuyerId() && null != trade2.getType()
        			&& trade2.getType().equals(Constants.TradeType.scratch_card.getTradeTypeId())){
        		//如果是刮刮卡订单
        		MikuScratchCardDO mikuScratchCardDO = null;
        		MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
        		mikuScratchCardDOExample.createCriteria().andUserIdEqualTo(trade2.getBuyerId())
        			.andTradeIdEqualTo(trade2.getTradeId());
        		List<MikuScratchCardDO> mikuScratchCardDOList = mikuScratchCardDOMapper.selectByExample(mikuScratchCardDOExample);
            	if(!mikuScratchCardDOList.isEmpty()){
            		mikuScratchCardDO = mikuScratchCardDOList.get(0);
            	}
        		if(null != mikuScratchCardDO){
            		mikuScratchCardDO.setStatus(Constants.ScratchCardStatus.PAYED.getStatusId());
            		mikuScratchCardDO.setLastUpdated(new Date());
            		if(mikuScratchCardDOMapper.updateByPrimaryKeySelective(mikuScratchCardDO) < 1){	//更新刮刮卡状态为未付款
            			System.out.println("刮刮卡更新失败------------------------------------");
            		}
            	}
        	}
        	
            //设置订单状态
            Trade trade = new Trade();
            trade.setPayType(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
            trade.setLastUpdated(new Date());
            trade.setPayTime(new Date());
            if(null != trade2 && null != trade2.getType() 
            		&& trade2.getType() == Constants.TradeType.one_buy_type.getTradeTypeId()){
            	trade.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
            }else{
            	trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
            }
            trade.setCodStatus(Constants.CodStatus.SIGN_IN.getCodStatusId());
            trade.setVersion(trade2.getVersion() + 1l);
            TradeExample tExample = new TradeExample();
            List<Byte> toUpdateStatus = new ArrayList<>();
            toUpdateStatus.add((byte) 2);
            toUpdateStatus.add((byte) 9);
            tExample.createCriteria().andTradeIdEqualTo(Long.valueOf(trade2.getTradeId())).andVersionEqualTo(trade2.getVersion())
                    .andStatusIn(toUpdateStatus);
            if (tradeMapper.updateByExampleSelective(trade, tExample) < 1) {
                System.out.println("更新订单失败------------------------------------");
            } else {
                
            }
            //设置order状态
            List<Order> orders = new ArrayList<Order>();
            if (trade2.getOrders().length() > 0) {
                for (String id : trade2.getOrders().split(";")) {
                    OrderExample orderExample = new OrderExample();
                    orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                    List<Order> tempOrders = orderMapper.selectByExample(orderExample);
                    if (null != orders && tempOrders.size() > 0) {
                        orders.add(tempOrders.get(0));
                    }
                }
                for (Order order : orders) {
                    Order tmpOrder = new Order();
                    tmpOrder.setLastUpdated(new Date());
                    tmpOrder.setPayment(order.getTotalFee());
                        //tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                        if(null != trade2 && null != trade2.getType() 
                        		&& trade2.getType() == Constants.TradeType.one_buy_type.getTradeTypeId()){
                        	tmpOrder.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
                        }else{
                        	tmpOrder.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                        }
                        tmpOrder.setVersion(order.getVersion() + 1l);
                        OrderExample oExample = new OrderExample();
                        oExample.createCriteria().andIdEqualTo(order.getId()).andStatusEqualTo((byte) 2);
                        if(orderMapper.updateByExampleSelective(tmpOrder, oExample) < 1){
                        	System.out.println("order更新失败-----------------------------------------------------------------");
                        }
                        //更新库存和销售量 针对非物流订单
                        //updateStock(order, transactionStatus);

                }
            }
    }

    public static void main(String[] args){
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();

        //fetch message summary

        MessageSumViewDO messageSumViewDO = new MessageSumViewDO();
        messageSumViewDO.setBizName(BizConstants.MessageBizTypeEnum.TO_ITEM.getName());
        messageSumViewDO.setBizType(BizConstants.MessageBizTypeEnum.TO_ITEM.getBizType());
        messageSumViewDO.setDateCreated(new Date().getTime());
        messageSumViewDO.setMsgType(BizConstants.MessageTypeEnum.TRADE_STATUS_CHANGE.getBizType());
        messageSumViewDO.setMsgTypeName(BizConstants.MessageTypeEnum.TRADE_STATUS_CHANGE.getName());
        messageSumViewDO.setProfileId(29l);
        messageSumViewDO.setStatus((byte)1);
        messageSumViewDO.setTarget(123L);

        List<MessageSumViewDO> messageSummaryDOs = Lists.newArrayList();
        messageSummaryDOs.add(messageSumViewDO);
        resultMap.put("hasNext", false);
        resultMap.put("hasNext", false);
        resultMap.put("messages", messageSumViewDO);
        welinkVO.setResult(resultMap);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        System.out.println("------------");
        System.out.println(JSON.toJSONString(welinkVO));
    }

}
