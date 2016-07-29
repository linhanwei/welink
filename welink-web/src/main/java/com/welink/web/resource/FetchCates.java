package com.welink.web.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.Banner;
import com.welink.biz.common.model.CategoryViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemSearchService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.CategoryService;
import com.welink.commons.domain.CategoryDO;
import com.welink.commons.domain.MikuBrandDO;

/**
 * 获取类目及类目banner
 * Created by daniel on 14-11-10.
 */
@RestController
public class FetchCates {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchCates.class);

    @Resource
    private CategoryService categoryService;

    @Resource
    private ItemSearchService itemSearchService;

    @Resource
    private ShopService shopService;

    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/fetchCates.json", "/api/h/1.0/fetchCates.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        //long communityId = (long) session.getAttribute(BizConstants.SHOP_ID);
        //根据communityId获取shopId
        //long shopId = shopService.fetchIdByShopId(communityId);
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

                /*SearchResult<Item> itemSearchResult = itemSearchService.defaultSearch(shopId, null, c.getId(), 0, 1000, OpenSearchType.RECOMMEND_DESC, null, true);
                if (null != itemSearchResult && itemSearchResult.isSuccess() && null != itemSearchResult.getResult()) {
                    cvDO.setCount(itemSearchResult.getResult().getTotal());
                } else {
                    cvDO.setCount(0);
                }*/
                categoryViewDOs.add(cvDO);
            }
        }
        resultMap.put("cates", categoryViewDOs);
        resultMap.put("suggest", "草莓");
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/fetchCatesByParams.json", "/api/h/1.0/fetchCatesByParams.json"}, produces = "application/json;charset=utf-8")
    public String fetchCatesByParams(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="brandId", required = false) Long brandId,
    		@RequestParam(value="cateParentId", required = false) Long cateParentId,
    		@RequestParam(value="level", required = false) Long level) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        //long communityId = (long) session.getAttribute(BizConstants.SHOP_ID);
        //根据communityId获取shopId
        //long shopId = shopService.fetchIdByShopId(communityId);
        //long shopId = shopService.fetchIdByShopId(2015);
        List<CategoryDO> categoryDOList = categoryService.fetchFreshCatesWithCache(cateParentId, level, brandId);
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

                /*SearchResult<Item> itemSearchResult = itemSearchService.defaultSearch(shopId, null, c.getId(), 0, 1000, OpenSearchType.RECOMMEND_DESC, null, true);
                if (null != itemSearchResult && itemSearchResult.isSuccess() && null != itemSearchResult.getResult()) {
                    cvDO.setCount(itemSearchResult.getResult().getTotal());
                } else {
                    cvDO.setCount(0);
                }*/
                categoryViewDOs.add(cvDO);
            }
        }
        resultMap.put("cates", categoryViewDOs);
        resultMap.put("suggest", "草莓");
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 
     * fetchBrands:(根据分类id等条件查询品牌列表). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param cateId	分类id(-2=全部)
     * @param cateLevel			（1=一级；2=二级；3=三级）
     * @return
     */
    @RequestMapping(value = {"/api/m/1.0/fetchBrands.json", "/api/h/1.0/fetchBrands.json"}, produces = "application/json;charset=utf-8")
    public String fetchCatesByParams(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="cateId", required = false, defaultValue="-2") Long cateId,
    		@RequestParam(value="cateLevel", required = false, defaultValue="0") Integer cateLevel) throws Exception {
    	WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        List<MikuBrandDO> brandList = categoryService.fetchBrands(cateId, cateLevel);
        resultMap.put("brands", brandList);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
}