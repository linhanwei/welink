package com.welink.web.resource.multishop;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.CommunityViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.AmapTransformService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.ConsigneeAddrDOExample;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.web.common.util.ParameterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-3-25.
 */
@RestController
public class LbsCommunity {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(LbsCommunity.class);

    @Resource
    private AddressService addressService;

    @Resource
    private UserService userService;

    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;

    @Resource
    private AmapTransformService amapTransformService;

    private void buildData(Map resultMap, CommunityDO communityDO, WelinkVO welinkVO) {
        //站点信息
        CommunityViewDO communityViewDO = ViewDOCopy.buildCommunityViewDO(communityDO);
        resultMap.put("community", communityViewDO);
        welinkVO.setResult(resultMap);
    }

    @RequestMapping(value = {"/api/m/1.0/lbsCommunity.json", "/api/h/1.0/lbsCommunity.json"}, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        String longitude = ParameterUtil.getParameter(request, "lng");
        String latitude = ParameterUtil.getParameter(request, "lat");
        Long communityId = ParameterUtil.getParameterAslongForSpringMVC(request, "cid", -1l);
        String gpsType = ParameterUtil.getParameter(request, "type");
        String op = ParameterUtil.getParameter(request, "op");
        String addr = ParameterUtil.getParameter(request, "addr");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        //设置所有站点信息
        List<CommunityViewDO> allCommunity = addressService.fetchCommunityViewDOs();
        resultMap.put("cityCommunity", allCommunity);

        try {
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            log.error("fetch profileId from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId());
        }
        //传communityId查询指定站点信息
        if (communityId > 0 && StringUtils.equalsIgnoreCase(op, "fetch")) {
            CommunityDO communityDO = addressService.fetchCommunity(communityId);
            if (null != communityDO) {
                buildData(resultMap, communityDO, welinkVO);
                welinkVO.setResult(resultMap);
                welinkVO.setStatus(1);
                if (StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                    session.setAttribute(BizConstants.LBS_LAST_ADDRESS, session.getAttribute(BizConstants.LBS_LAST_ADDRESS));
                }
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                //设置当前站点信息与位置信息
                if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS))) &&
                        !org.apache.commons.lang.StringUtils.equalsIgnoreCase("null", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                    resultMap.put("currentLocation", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)));
                }
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            } else {
                log.error("找不到对应的服务站点 communityId:" + communityId + ",sessionId:" + session.getId());
                welinkVO.setStatus(1);
                welinkVO.setCode(BizErrorEnum.SHOULD_CHANGE_COMMUNITY.getCode());
                welinkVO.setMsg(BizErrorEnum.SHOULD_CHANGE_COMMUNITY.getMsg());
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            }
        } else
            //手动选择小区并设置
            if (communityId > 0 && StringUtils.equalsIgnoreCase(op, "set")) {
                CommunityDO communityDO = addressService.fetchCommunity(communityId);
                if (null != communityDO) {
                    buildData(resultMap, communityDO, welinkVO);
                    welinkVO.setResult(resultMap);
                    welinkVO.setStatus(1);
                    if (StringUtils.isNotBlank(addr)) {
                        session.setAttribute(BizConstants.LBS_LAST_ADDRESS, addr);
                    }
                    CommunityViewDO communityViewDO = ViewDOCopy.buildCommunityViewDO(communityDO);
                    resultMap.put("currentCommunity", communityViewDO);
                    session.setAttribute(BizConstants.SHOP_ID, communityDO.getId());
                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    //设置当前站点信息与位置信息
                    if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS))) &&
                            !org.apache.commons.lang.StringUtils.equalsIgnoreCase("null", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                        resultMap.put("currentLocation", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)));
                    }
                    welinkVO.setResult(resultMap);
                    return JSON.toJSONString(welinkVO);
                } else {
                    log.error("找不到对应的服务站点 communityId:" + communityId + ",sessionId:" + session.getId());
                    welinkVO.setStatus(1);
                    welinkVO.setCode(BizErrorEnum.SHOULD_CHANGE_COMMUNITY.getCode());
                    welinkVO.setMsg(BizErrorEnum.SHOULD_CHANGE_COMMUNITY.getMsg());
                    welinkVO.setResult(resultMap);
                    return JSON.toJSONString(welinkVO);
                }
            }
        //单纯的获取所有站点信息，community_id不传递 如果session中没有，找用户的默认收货地址
        if (StringUtils.equalsIgnoreCase(op, "fetch")) {
            log.error("查询所有站点信息并设置: op: fetch" + ",sessionId:" + session.getId());
            welinkVO.setStatus(1);
            //查询当前站点信息
            try {
                communityId = (long) session.getAttribute(BizConstants.SHOP_ID);
            } catch (Exception e) {
                log.info("no communityid shopid in session");
            }
            CommunityDO communityDOtmp = addressService.fetchCommunity(communityId);
            if (null == communityDOtmp) {
                //查找默认收货地址
                if (profileId > 0) {
                    ConsigneeAddrDOExample qExample = new ConsigneeAddrDOExample();
                    qExample.createCriteria().andUserIdEqualTo(profileId).andGetDefEqualTo((byte) 1);
                    List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(qExample);
                    if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0 && consigneeAddrDOs.get(0).getCommunityId() > 0) {
                        CommunityDO communityDO = addressService.fetchCommunity(consigneeAddrDOs.get(0).getCommunityId());
                        if (null != communityDO) {
                            CommunityViewDO communityViewDO = ViewDOCopy.buildCommunityViewDO(communityDO);
                            resultMap.put("currentCommunity", communityViewDO);
                            String cm = consigneeAddrDOs.get(0).getCommunity();
                            if (cm.split(BizConstants.SPLIT).length > 1) {
                                cm = cm.split(BizConstants.SPLIT)[1];
                            }
                            Address address = new Address();
                            address.setLat(consigneeAddrDOs.get(0).getLatitude());
                            address.setLng(consigneeAddrDOs.get(0).getLongitude());
                            address.setName(cm);
                            //缓存设置
                            session.setAttribute(BizConstants.LBS_LAST_ADDRESS, URLEncoder.encode(JSON.toJSONString(address)));
                            session.setAttribute(BizConstants.SHOP_ID, communityDO.getId());
                            resultMap.put("currentLocation", URLEncoder.encode(JSON.toJSONString(address)));
                            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            welinkVO.setStatus(1);
                            welinkVO.setResult(resultMap);
                            return JSON.toJSONString(welinkVO);
                        }
                    }
                }
                log.error("查询默认服务站点失败 communityId:" + communityId + ",sessionId:" + session.getId());
                welinkVO.setStatus(1);
                welinkVO.setCode(BizErrorEnum.CAN_NOT_COMMUNITY.getCode());
                welinkVO.setMsg(BizErrorEnum.CAN_NOT_COMMUNITY.getMsg());
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            }
            CommunityDO communityDO = addressService.fetchCommunity(communityId);
            if (null != communityDO) {
                CommunityViewDO communityViewDO = ViewDOCopy.buildCommunityViewDO(communityDO);
                resultMap.put("currentCommunity", communityViewDO);
                welinkVO.setStatus(1);
                if (StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                    session.setAttribute(BizConstants.LBS_LAST_ADDRESS, session.getAttribute(BizConstants.LBS_LAST_ADDRESS));
                }
                session.setAttribute(BizConstants.SHOP_ID, communityId);
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                //设置当前站点信息与位置信息
                if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS))) &&
                        !org.apache.commons.lang.StringUtils.equalsIgnoreCase("null", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                    resultMap.put("currentLocation", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)));
                }
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            } else
            //当前
            {
                log.error("查询默认服务站点失败 communityId:" + communityId + ",sessionId:" + session.getId());
                welinkVO.setStatus(1);
                welinkVO.setCode(BizErrorEnum.CAN_NOT_COMMUNITY.getCode());
                welinkVO.setMsg(BizErrorEnum.CAN_NOT_COMMUNITY.getMsg());
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            }
        }
        //根据lbs信息获取站点
        if (StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(latitude)) {
            //根据用户经纬度位置信息获取站点信息
            String point = longitude + "," + latitude;
            if (StringUtils.equals(gpsType, "gps")) {
                Optional<String> lbs = amapTransformService.pointTransform(point);
                if (lbs.isPresent()) {
                    if (org.apache.commons.lang.StringUtils.isNotBlank(lbs.get()) && lbs.get().split(",").length > 1) {
                        point = lbs.get().split(",")[0] + "," + lbs.get().split(",")[1];
                        resultMap.put("location", point);
                    }
                }
            }
            CommunityDO lbsCommunityDO = addressService.fetchCommunityViaLatlng(point);
//            session.setAttribute(BizConstants.SHOP_ID, -1l);
            if (null != lbsCommunityDO) {
                welinkVO.setStatus(1);
                session.setAttribute(BizConstants.SHOP_ID, lbsCommunityDO.getId());
                if (StringUtils.isNotBlank(addr)) {
                    session.setAttribute(BizConstants.LBS_LAST_ADDRESS, addr);
                }
                if (profileId > 0) {
                    userService.updateLastLoginCommunity(lbsCommunityDO.getId(), profileId);
                }
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                welinkVO.setStatus(1);
                CommunityViewDO communityViewDO = ViewDOCopy.buildCommunityViewDO(lbsCommunityDO);
                resultMap.put("currentCommunity", communityViewDO);
                if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS))) &&
                        !org.apache.commons.lang.StringUtils.equalsIgnoreCase("null", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                    resultMap.put("currentLocation", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)));
                }
            } else {
                log.error("找不到对应的服务站点 longitude:" + longitude + ",latitude:" + latitude + ",sessionId:" + session.getId());
                welinkVO.setStatus(1);
                welinkVO.setCode(BizErrorEnum.SHOULD_CHANGE_COMMUNITY.getCode());
                welinkVO.setMsg(BizErrorEnum.SHOULD_CHANGE_COMMUNITY.getMsg());
                welinkVO.setResult(resultMap);
                welinkVO.setResult(resultMap);
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                welinkVO.setResult(resultMap);
                if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS))) &&
                        !org.apache.commons.lang.StringUtils.equalsIgnoreCase("null", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
                    resultMap.put("currentLocation", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)));
                }
                return JSON.toJSONString(welinkVO);
            }
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //设置当前站点信息与位置信息
        if (org.apache.commons.lang.StringUtils.isNotBlank(String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS))) &&
                !org.apache.commons.lang.StringUtils.equalsIgnoreCase("null", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)))) {
            resultMap.put("currentLocation", String.valueOf(session.getAttribute(BizConstants.LBS_LAST_ADDRESS)));
        }
        welinkVO.setStatus(0);
        welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
        welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    class Address {
        String name;
        String lat;
        String lng;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }
}
