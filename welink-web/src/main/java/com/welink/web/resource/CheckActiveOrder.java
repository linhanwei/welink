package com.welink.web.resource;

import java.util.ArrayList;
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
import com.opensymphony.xwork2.ActionContext;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemService;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.persistence.InstallActiveDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 14-12-22.
 */
@RestController
public class CheckActiveOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CheckActiveOrder.class);

    @Resource
    private ItemService itemService;

    @Resource
    private InstallActiveDOMapper installActiveDOMapper;

    @RequestMapping(value = {"/api/m/2.0/checkActiveOrder.json", "/api/h/1.0/checkActiveOrder.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        String itemIds = request.getParameter("ids");
        boolean daily = ParameterUtil.getParameterAsBooleanForSpringMVC(request, "daily");
        ActionContext context = ActionContext.getContext();
        ResponseResult result = new ResponseResult();
        WelinkVO welinkVO = new WelinkVO();
        if (StringUtils.isBlank(itemIds)) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Map resultMap = new HashMap();
        Session session = currentUser.getSession();
        if (checkSession(context, result, session)) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        String idss[] = itemIds.split(",");
        List<Long> idList = new ArrayList<Long>();
        for (String id : idss) {
            idList.add(Long.valueOf(id));
        }

        int cnt = 0;
        int limit = 0;
        /*if (idList.size() == 1) {
            long itemId = idList.get(0);
            BaseResult<Item> itemResult = itemService.fetchItemById(itemId);
            if (null != itemResult && itemResult.isSuccess() && itemResult.getResult() != null) {
                if (Long.compare(itemResult.getResult().getCategoryId(), Constants.AppointmentServiceCategory.FirstInstallActive.getCategoryId()) == 0) {
                    long baseItemId = itemResult.getResult().getBaseItemId();
                    InstallActiveDOExample qExample = new InstallActiveDOExample();
                    qExample.createCriteria().andBaseItemIdEqualTo(baseItemId).andBuyerIdEqualTo(profileId);
                    List<InstallActiveDO> installActiveDOs = installActiveDOMapper.selectByExample(qExample);
                    if (installActiveDOs != null && installActiveDOs.size() > 0) {
                        cnt = installActiveDOs.get(0).getCount();
                    }
                    //兼容老的逻辑ACTIVE_ITEM_ID
                    InstallActiveDOExample qqExample = new InstallActiveDOExample();
                    qqExample.createCriteria().andItemIdEqualTo(BizConstants.ACTIVE_ITEM_ID).andBuyerIdEqualTo(profileId);
                    List<InstallActiveDO> oldInstals = installActiveDOMapper.selectByExample(qqExample);
                    if (null != oldInstals && oldInstals.size() > 0) {
                        List<ItemCanBuy> itemCanBuys = Lists.newArrayList();
                        ItemCanBuy itemCanBuy = new ItemCanBuy();
                        itemCanBuy.setCap(0);
                        itemCanBuy.setRealCap(0);
                        itemCanBuy.setItemId(itemId);
                        itemCanBuys.add(itemCanBuy);
                        welinkVO.setStatus(1);
                        resultMap.put("itemLimits", itemCanBuys);
                        welinkVO.setResult(resultMap);
                        return JSON.toJSONString(welinkVO);
                    }
                    List<ObjectTaggedDO> tags = itemService.fetchLimitTagsViewViaItemIds(Lists.newArrayList(itemId));
                    if (null != tags && tags.size() > 0) {
                        limit = itemService.fetchLimitCount(tags.get(0));
                        if ((limit - cnt) < 1) {
                            List<ItemCanBuy> itemCanBuys = Lists.newArrayList();
                            ItemCanBuy itemCanBuy = new ItemCanBuy();
                            itemCanBuy.setCap(0);
                            itemCanBuy.setRealCap(0);
                            itemCanBuy.setItemId(itemId);
                            itemCanBuys.add(itemCanBuy);
                            welinkVO.setStatus(1);
                            resultMap.put("itemLimits", itemCanBuys);
                            welinkVO.setResult(resultMap);
                            return JSON.toJSONString(welinkVO);
                        } else {
                            List<ItemCanBuy> itemCanBuys = Lists.newArrayList();
                            ItemCanBuy itemCanBuy = new ItemCanBuy();
                            itemCanBuy.setCap(limit - cnt);
                            itemCanBuy.setRealCap(limit - cnt);
                            itemCanBuy.setItemId(itemId);
                            itemCanBuys.add(itemCanBuy);
                            welinkVO.setStatus(1);
                            resultMap.put("itemLimits", itemCanBuys);
                            welinkVO.setResult(resultMap);
                            return JSON.toJSONString(welinkVO);
                        }
                    }
                }
            }
        }*/

        //List<ItemCanBuy> itemCanBuys = itemService.fetchOutLimitItems(idList, profileId, false);
        List<ItemCanBuy> itemCanBuys = itemService.fetchOutLimitItems(idList, profileId, true);

        welinkVO.setStatus(1);
        resultMap.put("itemLimits", itemCanBuys);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    private boolean checkSession(ActionContext context, ResponseResult result, Session session) {
        if (null == session) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            context.put("result", result);
            return true;
        }
        if (null != session.getAttribute("profileId")) {
            return false;
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            context.put("result", result);
            return true;
        }
    }
}
