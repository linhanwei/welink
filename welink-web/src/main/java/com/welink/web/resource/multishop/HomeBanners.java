package com.welink.web.resource.multishop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.AnnouceViewDO;
import com.welink.biz.common.model.BannerViewDO;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedShop;
import com.welink.biz.service.AnnouceService;
import com.welink.biz.service.BannerService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.OpenSearchType;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.AnnouceDO;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MessageSummaryDO;
import com.welink.commons.domain.MessageSummaryDOExample;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MessageSummaryDOMapper;
import com.welink.commons.persistence.MikuBrandDOMapper;

/**
 * 多站点支持
 * Created by daniel on 15-3-25.
 */
@RestController
public class HomeBanners {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HomeBanners.class);

    @Resource
    private BannerService bannerService;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private ItemService itemService;

    public static final String SPLIT = ",";

    @Resource
    private ShopService shopService;

    @Resource
    private AnnouceService annouceService;

    @Resource
    private MessageSummaryDOMapper messageSummaryDOMapper;
    
    @Resource
    private MikuBrandDOMapper mikuBrandDOMapper;

    //@NeedShop
    @RequestMapping(value = {"/api/m/2.0/homeBanner.json", "/api/h/1.0/homeBanner.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String key = request.getParameter("k");
        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Map resultMap = new HashMap();
        long communityId = -1L, shopId = -1L;
        
        //橱窗推荐商品
        /*long communityId = (long) session.getAttribute(BizConstants.SHOP_ID);
        //根据communityId获取shopId
        long shopId = shopService.fetchIdByShopId(communityId);
        if (shopId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.COMMUNITY_SHOP_NOT_MATCHING.getCode());
            welinkVO.setMsg(BizErrorEnum.COMMUNITY_SHOP_NOT_MATCHING.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        log.info("根据shop_id获取id:" + shopId);*/
        List<ItemViewDO> itemViewDOs = buildCommunityAndRecItems(shopId);
        resultMap.put("recItems", itemViewDOs);
        /*MikuBrandDOExample mikuBrandDOExample = new MikuBrandDOExample();
        mikuBrandDOExample.createCriteria().andIsDeletedEqualTo((byte)0);
        List<MikuBrandDO> mikuBrandDOList = mikuBrandDOMapper.selectByExample(mikuBrandDOExample);
        resultMap.put("brands", mikuBrandDOList);*/		//品牌列表
        
        //banners
        Map<String, List<BannerViewDO>> bannerViews = bannerService.fetchBannersMapWitchCatch(shopId, null, 0);
        Map<String, List<BannerViewDO>> bannerViewDOs = new HashMap<>();
        //405 406 407 408 +409+ 410 + 411 + 416+ 417
        List<String> oldKey = Lists.newArrayList();
        oldKey.add("405");
        oldKey.add("406");
        oldKey.add("407");
        oldKey.add("408");
        oldKey.add("409");
        oldKey.add("410");
        oldKey.add("411");
        oldKey.add("416");
        oldKey.add("417");
        //兼容老版本
        if (StringUtils.isBlank(key)) {
            for (String ky : oldKey) {
                bannerViewDOs.put(ky, bannerViews.get(ky));
                resultMap.put("banners", bannerViewDOs);
                buildOtherData(resultMap, communityId);
                welinkVO.setStatus(1);
                welinkVO.setResult(resultMap);
            }
        } else {
            if (StringUtils.isNotBlank(key) && null != bannerViews && bannerViews.size() > 0) {
                List<String> keys = Arrays.asList(key.split(SPLIT));
                for (String ky : keys) {
                    bannerViewDOs.put(ky, bannerViews.get(ky));
                }
                resultMap.put("banners", bannerViewDOs);
                buildOtherData(resultMap, communityId);
                welinkVO.setStatus(1);
                welinkVO.setResult(resultMap);
            }
        }
        buildOtherData(resultMap, communityId);

        //拉取未读消息数
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            Long profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            MessageSummaryDOExample qExample = new MessageSummaryDOExample();
            qExample.createCriteria().andProfileIdEqualTo(profileId).andStatusEqualTo((byte) 1);
            List<MessageSummaryDO> messageSummaryDOs = messageSummaryDOMapper.selectByExample(qExample);
            if (null != messageSummaryDOs) {
                resultMap.put("msgCount", messageSummaryDOs.size());
            }
        }
        resultMap.put("nowTime", new Date());
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        //JSONObject.toJSONString(welinkVO,SerializerFeature.WriteMapNullValue);
        return JSON.toJSONString(welinkVO);
    }
    
    private void buildOtherData(Map resultMap, long communityId) {
        //获取公告
        List<AnnouceDO> annouceDOs = annouceService.fetchAnnouces(communityId, 1, 0, 1);
        if (null != annouceDOs && annouceDOs.size() > 0) {
            AnnouceViewDO annouceViewDO = ViewDOCopy.buildAnnouceViewDO(annouceDOs.get(0));
            resultMap.put("annouce", annouceViewDO);
        }
        resultMap.put("threshold", BizConstants.THRESHOLD);
        resultMap.put("cid", communityId);
    }

    private List<ItemViewDO> buildCommunityAndRecItems(long shopId) {
        List<Long> searchTags = new ArrayList<>();
        searchTags.add(BizConstants.SearchTagEnum.SHOW_CASE.getTag());
        List<com.welink.commons.domain.Item> items = itemService.searchOpenSearchItems(shopId, null, null, null, 0, 3, OpenSearchType.WEIGHT_ASC.getType(), searchTags, true);
        List<ItemViewDO> itemViewDOs = new ArrayList<>();
        if (null != items && items.size() > 0) {

        } else {
            ItemExample qExample = new ItemExample();
            qExample.createCriteria().andHasShowcaseEqualTo(BizConstants.ShowCaseEnum.SHOW.getType()).andApproveStatusEqualTo(Byte.valueOf("1"))
            	.andBaseItemIdIsNotNull();
            //qExample.setOrderByClause("date_created DESC");
            qExample.setOrderByClause("weight ASC");
            qExample.setOffset(0);
            qExample.setLimit(3);
            items = itemMapper.selectByExample(qExample);
        }
        if (null != items && items.size() > 0) {
            itemViewDOs = itemService.combineItemTags(items);
        }
        //最多3个橱窗推荐商品
        if (itemViewDOs.size() > 3) {
            return buildAgio(itemViewDOs.subList(0, 3));
        }
        return buildAgio(itemViewDOs);
    }

    private List<ItemViewDO> buildAgio(List<ItemViewDO> itemViewDOs) {
        for (ItemViewDO itemViewDO : itemViewDOs) {
            if (null != itemViewDO.getRefPrice() && null != itemViewDO.getPrice()) {
                BigDecimal bd1 = new BigDecimal(itemViewDO.getPrice() * 100);
                BigDecimal bd2 = new BigDecimal(itemViewDO.getRefPrice());
                double b = bd1.divide(bd2, 1, BigDecimal.ROUND_HALF_UP).doubleValue();
                long i = Math.round(b);
                itemViewDO.setAgioValue(i);
            }
        }
        return itemViewDOs;
    }
}
