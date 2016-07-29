package com.welink.web.resource.multishop;

import java.math.BigInteger;
import java.util.Date;
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
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedShop;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.service.UserService;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.vo.ItemTagActivtyVO;

/**
 * 
 * ClassName: FetchactivityItems <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015年12月9日 下午12:25:42 <br/>
 *
 * @author LuoGuangChun
 */
@RestController
public class FetchactivityItems {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchactivityItems.class);

    @Resource
    private ItemService itemService;

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;

    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/fetchItemTagList.json", "/api/h/1.0/fetchItemTagList.json"}, produces = "application/json;charset=utf-8")
    public String fetchItemTagList(HttpServletRequest request, HttpServletResponse response, 
    					@RequestParam(value="bit", required = false)  BigInteger bit,  
    					@RequestParam(value="brandId", required = false)  Long startTime,
    					@RequestParam(value="brandId", required = false)  Long startTime2,
                        @RequestParam Integer pg, @RequestParam Integer sz) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        String mobile = "";
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        //1.查找商品，走搜索
        long shopId = userService.fetchLastLoginShop(session);
        shopId = shopService.fetchIdByShopId(shopId);
        log.info("根据shop_id获取id:" + shopId);
        //2. 如果搜索失败，则走数据库查询查找商品
        List<ItemTagActivtyVO> items = itemService.fetchItemsByPageCache(shopId, bit, 
        		startTime, startTime2, startRow, size);		//查询标签商品列表
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        boolean hasNext = true;
        if (null != items && items.size() > 0) {
            if (null != items && items.size() < size) {
                hasNext = false;
            } else {
                hasNext = true;
            }
            //List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);
            resultMap.put("items", items);
            resultMap.put("hasNext", hasNext);
            welinkVO.setResult(resultMap);
        } else {
        	hasNext = false;
        	resultMap.put("hasNext", hasNext);
            welinkVO.setResult(resultMap);
        }
        return JSON.toJSONString(welinkVO);
    }
}
