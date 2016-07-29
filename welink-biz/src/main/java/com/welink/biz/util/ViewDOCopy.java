package com.welink.biz.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.model.*;
import com.welink.commons.domain.*;
import com.welink.commons.vo.TagViewVO;
import com.welink.commons.vo.TradeCrowdfundVO;
import com.welink.promotion.PromotionType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-1-21.
 */
public class ViewDOCopy {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ViewDOCopy.class);

    /**
     * 构建商品VIEW DO
     *
     * @param item
     * @return
     */
    public static ItemViewDO buildItemViewDO(Item item) {
        ItemUtil itemUtil = new ItemUtil();
        ItemViewDO itemViewDO = new ItemViewDO();
        itemViewDO.setCateId(item.getCategoryId());
        if (null != item.getOnlineEndTime()) {
            itemViewDO.setEndTime(item.getOnlineEndTime());
        }
        itemViewDO.setItemId(item.getId());
        itemViewDO.setPics(item.getPicUrls());
        itemViewDO.setPrice(item.getPrice());
        //long lCnt = buildSoldCount(item.getId(), item.getSoldQuantity());
        long lCnt = (null == item.getSoldQuantity() ? 0L : item.getSoldQuantity());
        itemViewDO.setSoldCnt(lCnt);
        itemViewDO.setTitle(item.getTitle());
        itemViewDO.setRefPrice(itemUtil.getReferencePrice(item));
        itemViewDO.setAddr(item.getAddress());
        itemViewDO.setSpecification(item.getSpecification());
        itemViewDO.setItemNum(Long.valueOf(null == item.getNum() ? 0 : item.getNum()));
        if (null != item.getHasShowcase()) {
            itemViewDO.setShowCase(item.getHasShowcase());
        }
        itemViewDO.setApproveStatus(item.getApproveStatus());
        itemViewDO.setShopId(item.getShopId());
        itemViewDO.setBaseItemId(item.getBaseItemId());
        itemViewDO.setDesc(null == item.getDescription() ? "" : item.getDescription());
        itemViewDO.setDetail(item.getDetail());
        itemViewDO.setBaseSoldQuantity(null == item.getBaseSoldQuantity() ? 0 : item.getBaseSoldQuantity());
        itemViewDO.setIsrefund(item.getIsrefund());
        itemViewDO.setType(item.getType());
        itemViewDO.setIsTaxFree(item.getIsTaxFree());
        Object obj = ItemUtil.getExtMap(item);
        String features = JSON.toJSONString(obj);
        if (StringUtils.isNotBlank(features) && !StringUtils.equals("null", features)) {
            Map<String, String> map = JSON.parseObject(JSON.toJSONString(obj), Map.class);
            if (StringUtils.isNotBlank(item.getAddress())) {
                map.put("产地", item.getAddress());
            }
            features = JSON.toJSONString(map);
            itemViewDO.setFeatures(features);
        } else {
            if (StringUtils.isNotBlank(item.getAddress())) {
                Map<String, String> map = new HashMap<>();
                map.put("产地", item.getAddress());
                features = JSON.toJSONString(map);
                itemViewDO.setFeatures(features);
            }
        }
        return itemViewDO;
    }

    public static long buildSoldCount(long itemId, Integer soldQuantity) {
        //this.soldNum = this.soldCnt + Math.abs(Math.ceil(Math.sin(parseInt(this.itemId) + 1) * (100 + this.soldCnt)));
        long cnt = 0;
        double d = Math.sin(Double.valueOf(itemId));
        if (null == soldQuantity) {
            cnt = 0;
        } else {
            cnt = soldQuantity.longValue();
        }
        d = Math.abs(d);
        d = d * 100;
        long base = Math.round(d);
        cnt += base;
        return cnt;
    }

    /**
     * 构建消息摘要view do
     *
     * @param message
     * @return
     */
    public static MessageSumViewDO buildMsgSumViewDO(MessageSummaryDO message) {
        if (null == message) {
            return null;
        }
        MessageSumViewDO messageSumViewDO = new MessageSumViewDO();
        messageSumViewDO.setBizName(message.getBizName());
        messageSumViewDO.setBizType(message.getBizType());
        if (null != message.getDateCreated()) {
            messageSumViewDO.setDateCreated(message.getDateCreated().getTime());
        }
        messageSumViewDO.setId(message.getId());
        messageSumViewDO.setProfileId(message.getProfileId());
        messageSumViewDO.setStatus(message.getStatus());
        messageSumViewDO.setTarget(message.getTarget());
        messageSumViewDO.setMsgType(Long.valueOf(message.getMessagetype()));
        messageSumViewDO.setMsgTypeName(message.getMsgTypeName());
        if (StringUtils.isBlank(message.getContent())) {
            messageSumViewDO.setContent("");
        } else {
            messageSumViewDO.setContent(message.getContent());
        }
        return messageSumViewDO;
    }

    /**
     * 构建trade VIEW DO
     *
     * @param trade
     * @return
     */
    public static TradeViewDO buildTradeViewDO(Trade trade, CommunityDO communityDO) {
        if (null == trade) {
            return null;
        }
        TradeViewDO tradeViewDO = new TradeViewDO();
        if (null != communityDO) {
            tradeViewDO.setCommunityName(communityDO.getName());
            tradeViewDO.setCommunityMobile(communityDO.getPhone());
        }
        tradeViewDO.setDateCreated(trade.getDateCreated().getTime());
        tradeViewDO.setPayment(trade.getPayment());
        tradeViewDO.setPayType(trade.getPayType());
        tradeViewDO.setPostFee(trade.getPostFee());
        tradeViewDO.setPrice(trade.getPrice());
        tradeViewDO.setStatus(trade.getStatus());
        tradeViewDO.setTotalFee(trade.getTotalFee());
        tradeViewDO.setTradeId(String.valueOf(trade.getTradeId()));
        tradeViewDO.setCommunityId(trade.getCommunityId());
        tradeViewDO.setCanRate(trade.getCanRate());
        tradeViewDO.setBuyerRate(trade.getBuyerRate());
        if (null != trade.getPayTime()) {
            tradeViewDO.setPayTime(trade.getPayTime().getTime());
        }
        if (null != trade.getDiscountFee() && null != trade.getPointFee()) {
            tradeViewDO.setCoupon(trade.getDiscountFee() - trade.getPointFee());
        } else if (null != trade.getDiscountFee() && null == trade.getPointFee()) {
            tradeViewDO.setCoupon(trade.getDiscountFee());
        }
        if (null != trade.getPointFee()) {
            tradeViewDO.setPoint(trade.getPointFee());
        }
        if (null != trade.getConsignTime()) {
            tradeViewDO.setConsignTime(trade.getConsignTime().getTime());
        }
        if (null != trade.getConfirmTime()) {
            tradeViewDO.setConfirmTime(trade.getConfirmTime().getTime());
        }
        if (null != trade.getEndTime()) {
            tradeViewDO.setEndTime(trade.getEndTime().getTime());
        }
        if (null != trade.getBuyerMessage()) {
            tradeViewDO.setTradeMsg(StringUtil.escapeJso1n(trade.getBuyerMessage()));
        }
        tradeViewDO.setTradeType(trade.getType());
        tradeViewDO.setShippingType(trade.getShippingType());
        if (null != trade.getAppointDeliveryTime()) {
            tradeViewDO.setAppointDTime(trade.getAppointDeliveryTime().getTime());
        }
        return tradeViewDO;
    }

    /**
     * 构建order VIEW DO
     *
     * @param order
     * @return
     */
    public static OrderViewDO buildOrderViewDO(Order order) {
        if (null == order) {
            return null;
        }
        OrderViewDO orderViewDO = new OrderViewDO();
        orderViewDO.setCateId(order.getCategoryId());
        orderViewDO.setItemId(order.getArtificialId());
        orderViewDO.setNum(order.getNum());
        orderViewDO.setPics(order.getPicUrl());
        orderViewDO.setPrice(order.getPrice());
        //orderViewDO.setRated(order.getBuyerRate());
        orderViewDO.setTitle(StringUtil.escapeJso1n(order.getTitle()));
        return orderViewDO;
    }

    /**
     * 构建收货地址VIEW DO
     *
     * @param consigneeAddrDO
     * @return
     */
    public static ConsigneeViewDO buildConsigneeViewDO(ConsigneeAddrDO consigneeAddrDO) {
        ConsigneeViewDO consigneeViewDO = new ConsigneeViewDO();
        consigneeViewDO.setReceiverName(consigneeAddrDO.getReceiverName());
        consigneeViewDO.setUid(consigneeAddrDO.getUid());
        consigneeViewDO.setAddCode(consigneeAddrDO.getAddCode());
        consigneeViewDO.setCityCode(consigneeAddrDO.getCityCode());
        consigneeViewDO.setCommunityName(consigneeAddrDO.getCommunityName());
        consigneeViewDO.setGetDef(consigneeAddrDO.getGetDef());
        consigneeViewDO.setLatitude(consigneeAddrDO.getLatitude());
        consigneeViewDO.setLongitude(consigneeAddrDO.getLongitude());
        consigneeViewDO.setProvinceCode(consigneeAddrDO.getProvinceCode());
        consigneeViewDO.setReceiver_state(consigneeAddrDO.getReceiver_state());
        consigneeViewDO.setReceiverAddress(consigneeAddrDO.getReceiverAddress());
        consigneeViewDO.setReceiverCity(consigneeAddrDO.getReceiverCity());
        consigneeViewDO.setReceiverDistrict(consigneeAddrDO.getReceiverDistrict());
        consigneeViewDO.setReceiverMobile(consigneeAddrDO.getReceiverMobile());
        consigneeViewDO.setId(consigneeAddrDO.getId());
        consigneeViewDO.setCommunityId(consigneeAddrDO.getCommunityId());
        consigneeViewDO.setIdCard(consigneeAddrDO.getIdCard());
        return consigneeViewDO;
    }

    /**
     * 构建complan VIEW DO
     *
     * @param complainDO
     * @return
     */
    public static CompViewDO buildCompViewDO(ComplainDO complainDO) {
        if (null == complainDO) {
            return null;
        }
        CompViewDO compViewDO = new CompViewDO();
        compViewDO.setCp_id(complainDO.getId());
        compViewDO.setEnd(complainDO.getLastUpdated().getTime());
        compViewDO.setStart(complainDO.getDateCreated().getTime());
        compViewDO.setStatus(complainDO.getStatus());
        compViewDO.setContent(complainDO.getContent());
        return compViewDO;
    }

    /**
     * 构建complan note view do
     *
     * @param complainNoteDO
     * @return
     */
    public static CompNoteViewDO buildCompNoteViewDO(ComplainNoteDO complainNoteDO) {
        if (null == complainNoteDO) {
            return null;
        }
        CompNoteViewDO compNoteViewDO = new CompNoteViewDO();
        compNoteViewDO.setContent(complainNoteDO.getDealContent());
        compNoteViewDO.setStart(complainNoteDO.getDateCreate().getTime());
        compNoteViewDO.setStatus(complainNoteDO.getStatus());
        compNoteViewDO.setType(complainNoteDO.getReplyerType());
        return compNoteViewDO;
    }

    /**
     * 构建物流 logistics view do
     *
     * @param logisticsDO
     * @return
     */
    public static LogisticsViewDO buildLogisticsViewDO(LogisticsDO logisticsDO) {
        if (null == logisticsDO) {
            return null;
        }
        LogisticsViewDO logisticsViewDO = new LogisticsViewDO();
        logisticsViewDO.setContactName(logisticsDO.getContactName());
        logisticsViewDO.setMobile(logisticsDO.getMobile());
        logisticsViewDO.setAddr(logisticsDO.getAddr());
        logisticsViewDO.setIdCard(logisticsDO.getIdCard());
        logisticsViewDO.setExpressCompany(logisticsDO.getExpressCompany());
        logisticsViewDO.setExpressNo(logisticsDO.getExpressNo());
        return logisticsViewDO;
    }

    /**
     * 构建类目VIEW DO
     *
     * @param c
     * @return
     */
    public static CategoryViewDO buildCategoryViewDO(CategoryDO c) {
        CategoryViewDO categoryViewDO = new CategoryViewDO();
        categoryViewDO.setCategoryId(c.getId());
        categoryViewDO.setName(c.getName());
        categoryViewDO.setIsParent(c.getIsParent());
        categoryViewDO.setParentId(null == c.getParentId() ? 0L : c.getParentId());
        categoryViewDO.setPic(null == c.getPicture() ? "" : c.getPicture());
        categoryViewDO.setLevel(c.getLevel());
        return categoryViewDO;
    }

    /**
     * 构建自提点 view DO
     *
     * @param communityDO
     * @return
     */
    public static CommunityViewDO buildCommunityViewDO(CommunityDO communityDO) {
        CommunityViewDO communityViewDO = new CommunityViewDO();
        communityViewDO.setProvince(communityDO.getProvince());
        communityViewDO.setPhone(communityDO.getPhone());
        communityViewDO.setOpeningHours(communityDO.getOpeningHours());
        communityViewDO.setName(communityDO.getName());
        communityViewDO.setCity(communityDO.getCity());
        communityViewDO.setDescription(communityDO.getDescription());
        communityViewDO.setDistrict(communityDO.getDistrict());
        communityViewDO.setId(communityDO.getId());
        communityViewDO.setLocation(communityDO.getLocation());
        communityViewDO.setDeliveryArea(communityDO.getDeliveryArea());
        communityViewDO.setLbs(communityDO.getLbs());
        communityViewDO.setPicUrls(communityDO.getPicUrls());
        return communityViewDO;
    }

    /**
     * 构建banner view DO
     *
     * @param bannerDO
     * @return
     */
    public static BannerViewDO buildBannerViewDO(BannerDO bannerDO) {
        BannerViewDO bannerViewDO = new BannerViewDO();
        bannerViewDO.setTitle(bannerDO.getTitle());
        bannerViewDO.setPicUrl(bannerDO.getPicUrl());
        bannerViewDO.setRedirectType(bannerDO.getRedirectType());
        bannerViewDO.setTarget(bannerDO.getTarget());
        bannerViewDO.setType(bannerDO.getType());
        bannerViewDO.setWeight(bannerDO.getWeight());
        bannerViewDO.setDescription(bannerDO.getDescription());
        bannerViewDO.setShowText(bannerDO.getShowText());
        bannerViewDO.setCategoryId(bannerDO.getCategoryId());
        bannerViewDO.setOnlineStartTime(bannerDO.getOnlineStartTime());
        bannerViewDO.setOnlineEndTime(bannerDO.getOnlineEndTime());
        return bannerViewDO;
    }

    public static TagViewDO buildTagViewDO(TagsDO tagsDO) {
        TagViewDO tagViewDO = new TagViewDO();
        tagViewDO.setId(tagsDO.getId());
        tagViewDO.setKv(tagsDO.getKv());
        tagViewDO.setName(tagsDO.getName());
        tagViewDO.setPic(tagsDO.getPic());
        tagViewDO.setStatus(tagsDO.getStatus());
        tagViewDO.setType(tagsDO.getType());
        tagViewDO.setWeight(tagsDO.getWeight());
        tagViewDO.setBit(tagsDO.getBit());
        return tagViewDO;
    }


    /**
     * 构建annouce view do
     *
     * @param annouceDO
     * @return
     */
    public static AnnouceViewDO buildAnnouceViewDO(AnnouceDO annouceDO) {
        if (null == annouceDO) {
            return null;
        }
        AnnouceViewDO annouceViewDO = new AnnouceViewDO();
        annouceViewDO.setCommunityId(annouceDO.getCommunityId());
        annouceViewDO.setContent(annouceDO.getContent());
        return annouceViewDO;
    }

    /**
     * 构建point view DO
     *
     * @param pointRecordDO
     * @return
     */
    public static PointViewDO buildPointViewDO(PointRecordDO pointRecordDO) {
        PointViewDO pointViewDO = new PointViewDO();
        pointViewDO.setSymbol(PromotionType.getSymbol(pointRecordDO.getType()));
        pointViewDO.setType(pointRecordDO.getType());
        pointViewDO.setScore(null == pointRecordDO.getAmount() ? 0L : pointRecordDO.getAmount());
        pointViewDO.setReason(PromotionType.getMsg(pointRecordDO.getType()));
        pointViewDO.setCreateTime(pointRecordDO.getDateCreated().getTime());
        return pointViewDO;
    }

    public static List<TagViewDO> buildNewTagViewDOs(List<TagViewDO> viewDOs) {
        List<TagViewDO> tagViewDOs = Lists.newArrayList();
        if (null != viewDOs && viewDOs.size() > 0) {
            for (TagViewDO tagViewDO : viewDOs) {
                TagViewDO tmpDO = new TagViewDO();
                tmpDO.setBit(tagViewDO.getBit());
                tmpDO.setId(tagViewDO.getId());
                tmpDO.setKv(tagViewDO.getKv());
                tmpDO.setName(tagViewDO.getName());
                tmpDO.setOkv(tagViewDO.getOkv());
                tmpDO.setPic(tagViewDO.getPic());
                tmpDO.setStatus(tagViewDO.getStatus());
                tmpDO.setType(tagViewDO.getType());
                tmpDO.setWeight(tagViewDO.getWeight());
                tagViewDOs.add(tmpDO);
            }
            return tagViewDOs;
        }
        return null;
    }
    
    public static List<TagViewVO> buildNewTagViewVOs(List<TagViewDO> viewDOs) {
        List<TagViewVO> tagViewDOs = Lists.newArrayList();
        if (null != viewDOs && viewDOs.size() > 0) {
            for (TagViewDO tagViewDO : viewDOs) {
                TagViewVO tmpDO = new TagViewVO();
                tmpDO.setBit(tagViewDO.getBit());
                tmpDO.setId(tagViewDO.getId());
                tmpDO.setKv(tagViewDO.getKv());
                tmpDO.setName(tagViewDO.getName());
                tmpDO.setOkv(tagViewDO.getOkv());
                tmpDO.setPic(tagViewDO.getPic());
                tmpDO.setStatus(tagViewDO.getStatus());
                tmpDO.setType(tagViewDO.getType());
                tmpDO.setWeight(tagViewDO.getWeight());
                tagViewDOs.add(tmpDO);
            }
            return tagViewDOs;
        }
        return null;
    }
    
    /**
     * 构建trade TradeCrowdfundVO
     *
     * @param trade
     * @return
     */
    public static TradeCrowdfundVO buildTradeCrowdfundVO(Trade trade, CommunityDO communityDO) {
        if (null == trade) {
            return null;
        }
        TradeCrowdfundVO tradeViewDO = new TradeCrowdfundVO();
        if (null != communityDO) {
            tradeViewDO.setCommunityName(communityDO.getName());
            tradeViewDO.setCommunityMobile(communityDO.getPhone());
        }
        tradeViewDO.setDateCreated(trade.getDateCreated().getTime());
        tradeViewDO.setPayment(trade.getPayment());
        tradeViewDO.setPayType(trade.getPayType());
        tradeViewDO.setPostFee(trade.getPostFee());
        tradeViewDO.setPrice(trade.getPrice());
        tradeViewDO.setStatus(trade.getStatus());
        tradeViewDO.setTotalFee(trade.getTotalFee());
        tradeViewDO.setTradeId(String.valueOf(trade.getTradeId()));
        tradeViewDO.setCommunityId(trade.getCommunityId());
        tradeViewDO.setCanRate(trade.getCanRate());
        tradeViewDO.setBuyerRate(trade.getBuyerRate());
        if (null != trade.getPayTime()) {
            tradeViewDO.setPayTime(trade.getPayTime().getTime());
        }
        if (null != trade.getDiscountFee() && null != trade.getPointFee()) {
            tradeViewDO.setCoupon(trade.getDiscountFee() - trade.getPointFee());
        } else if (null != trade.getDiscountFee() && null == trade.getPointFee()) {
            tradeViewDO.setCoupon(trade.getDiscountFee());
        }
        if (null != trade.getPointFee()) {
            tradeViewDO.setPoint(trade.getPointFee());
        }
        if (null != trade.getConsignTime()) {
            tradeViewDO.setConsignTime(trade.getConsignTime().getTime());
        }
        if (null != trade.getConfirmTime()) {
            tradeViewDO.setConfirmTime(trade.getConfirmTime().getTime());
        }
        if (null != trade.getEndTime()) {
            tradeViewDO.setEndTime(trade.getEndTime().getTime());
        }
        if (null != trade.getBuyerMessage()) {
            tradeViewDO.setTradeMsg(StringUtil.escapeJso1n(trade.getBuyerMessage()));
        }
        tradeViewDO.setTradeType(trade.getType());
        tradeViewDO.setShippingType(trade.getShippingType());
        if (null != trade.getAppointDeliveryTime()) {
            tradeViewDO.setAppointDTime(trade.getAppointDeliveryTime().getTime());
        }
        return tradeViewDO;
    }

    public static void main(String[] args) {
        long cnt = buildSoldCount(3645l, 284);
        System.out.println(cnt);

    }
}
