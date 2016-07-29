package com.welink.web.resource.multishop;

import java.util.ArrayList;
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
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.AddressModel;
import com.welink.biz.common.model.ConsigneeViewDO;
import com.welink.biz.common.model.LngLat;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.CommunityService;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.ConsigneeAddrDOExample;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.commons.utils.ApiXUtils;
import com.welink.commons.utils.IdcardUtils;
import com.welink.commons.vo.ApixIdCardMsg;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-3-26.
 */
@RestController
public class Consignee {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(Consignee.class);

    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;

    @Resource
    private CommunityService communityService;

    @Resource
    private AddressService addressService;

    @RequestMapping(value = {"/api/m/1.0/consignee.json", "/api/h/1.0/consignee.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean adjust = ParameterUtil.getParameterAsBooleanForSpringMVC(request, "adjust");
        String op = ParameterUtil.getParameter(request, "op");
        long id = ParameterUtil.getParameterAslongForSpringMVC(request, "id", -1l);
        String addressStr = ParameterUtil.getParameter(request, "addr");
        String mobile = ParameterUtil.getParameter(request, "mobile");
        String nick = ParameterUtil.getParameter(request, "nick");
        String getDef = ParameterUtil.getParameter(request, "getDef");		//1=默认地址；0
        String idCard = ParameterUtil.getParameter(request, "idCard");		//身份证

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        if (checkSession(welinkVO, session)) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        long profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        
        if (StringUtils.equalsIgnoreCase(op, "u")) {
        	if (StringUtils.isBlank(nick)) {
            	welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg("亲~收货人不能为空~");
                return JSON.toJSONString(welinkVO);
            }
            //反射address
            AddressModel addressModel = null;
            if (StringUtils.isNotBlank(addressStr)) {
                addressModel = JSON.parseObject(addressStr, AddressModel.class);
            }
            //先查询出收货地址
            ConsigneeAddrDO qConsigneeDO = consigneeAddrDOMapper.selectByPrimaryKey(id);
            if(null != qConsigneeDO 
            		&& ((!nick.equals(qConsigneeDO.getReceiverName())
            				&& StringUtils.isNotBlank(qConsigneeDO.getIdCard())) 
            		|| (StringUtils.isNotBlank(idCard) && !idCard.equals(qConsigneeDO.getIdCard())))){
            	//验证身份证是否合法
    	    	if(!IdcardUtils.validateCard(idCard)){
    	    		welinkVO.setStatus(0);
    	            welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_FOUND.getMsg());
    	            welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_FOUND.getCode());
    	            return JSON.toJSONString(welinkVO);
    	    	}
            	//验证身份证的是否真实性
        		ApixIdCardMsg apixIdCardMsg = ApiXUtils.requestGetApixIdCardMsg(nick, idCard.trim());
            	if(null != apixIdCardMsg && !apixIdCardMsg.getCode().equals(0)){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg(apixIdCardMsg.getMsg());
                    welinkVO.setCode(-999);
                    return JSON.toJSONString(welinkVO);
            	}else if(null == apixIdCardMsg){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_FOUND.getMsg());
                    welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_FOUND.getCode());
                    return JSON.toJSONString(welinkVO);
            	}
            }
            ConsigneeAddrDO consigneeAddrDO = new ConsigneeAddrDO();
            if (null != addressModel) {
                if (adjust) {
                    String address = geoAddr(addressModel);
                    if (StringUtils.isNotBlank(address)) {
                        LngLat lngLat = addressService.deGeo(address);
                        if (lngLat != null) {
                            addressModel.setLatitude(lngLat.getLatitude());
                            addressModel.setLongitude(lngLat.getLongitude());
                        }
                    }
                }
                //根据经纬度获取配送站点id communityId
                String point = addressModel.getLongitude() + "," + addressModel.getLatitude();
                long deliveryId = qConsigneeDO.getCommunityId();
                if (StringUtils.isNotBlank(addressModel.getLatitude()) && StringUtils.isNotBlank(addressModel.getLongitude())) {
                    log.info("add address latitude:" + addressModel.getLatitude() + ",longitude:" + addressModel.getLongitude() + ",sessionId:" + session.getId());
                    deliveryId = communityService.queryCommunityIdByCoordinates(point);
                }
                consigneeAddrDO.setCommunityId(deliveryId);
                buildConsigneeDO(addressModel, consigneeAddrDO);
                consigneeAddrDO.setVersion(qConsigneeDO.getVersion() + 1l);
                consigneeAddrDO.setStatus(1);
            }
            //更新
            if (StringUtils.isNotBlank(nick)) {
                consigneeAddrDO.setReceiverName(nick);
            }
            if (StringUtils.isNotBlank(mobile)) {
                consigneeAddrDO.setReceiverMobile(mobile);
            }
            if(StringUtils.isNotBlank(idCard)){
            	consigneeAddrDO.setIdCard(idCard);
            }
            ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
            cExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(1);
            if (consigneeAddrDOMapper.updateByExampleSelective(consigneeAddrDO, cExample) < 1) {
                log.error("update consignee address failed. id:" + id + ",profileId:" + profileId + ",sessionId:" + session.getId());
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.UPDATE_CONSIGNEE_ADDRESS_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.UPDATE_CONSIGNEE_ADDRESS_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        } else if (StringUtils.equalsIgnoreCase(op, "add")) {
        	if (StringUtils.isBlank(nick)) {
            	welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg("亲~收货人不能为空~");
                return JSON.toJSONString(welinkVO);
            }
            //参数检查
            if (StringUtils.isBlank(addressStr)) {
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            //验证身份证和姓名
            if(StringUtils.isNotBlank(idCard)){
    	        //验证身份证是否合法
    	    	if(!IdcardUtils.validateCard(idCard)){
    	    		welinkVO.setStatus(0);
    	            welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_FOUND.getMsg());
    	            welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_FOUND.getCode());
    	            return JSON.toJSONString(welinkVO);
    	    	}
            	//验证身份证的是否真实性
        		ApixIdCardMsg apixIdCardMsg = ApiXUtils.requestGetApixIdCardMsg(nick, idCard.trim());
            	if(null != apixIdCardMsg && !apixIdCardMsg.getCode().equals(0)){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg(apixIdCardMsg.getMsg());
                    welinkVO.setCode(-999);
                    return JSON.toJSONString(welinkVO);
            	}else if(null == apixIdCardMsg){
            		welinkVO.setStatus(0);
                    welinkVO.setMsg(BizErrorEnum.ID_CARD_NOT_FOUND.getMsg());
                    welinkVO.setCode(BizErrorEnum.ID_CARD_NOT_FOUND.getCode());
                    return JSON.toJSONString(welinkVO);
            	}
            }
            ConsigneeAddrDOExample cntExample = new ConsigneeAddrDOExample();
            cntExample.createCriteria().andUserIdEqualTo(profileId).andStatusEqualTo(1);
            int consigneeCount = consigneeAddrDOMapper.countByExample(cntExample);
            if (consigneeCount > BizConstants.CONSIGNEE_COUNT) {
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.ADD_CONSIGNEE_ADDRESS_FAILED_COUNT_FULL.getCode());
                welinkVO.setMsg(BizErrorEnum.ADD_CONSIGNEE_ADDRESS_FAILED_COUNT_FULL.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            //反射address
            AddressModel addressModel = JSON.parseObject(addressStr, AddressModel.class);
            if (addressModel != null && adjust) {
                String address = geoAddr(addressModel);
                if (StringUtils.isNotBlank(address)) {
                    LngLat lngLat = addressService.deGeo(address);
                    if (lngLat != null) {
                        addressModel.setLatitude(lngLat.getLatitude());
                        addressModel.setLongitude(lngLat.getLongitude());
                    }
                }
            }
            //更新
            //根据经纬度获取配送站点id communityId
            long deliveryId = -1l;
            if (StringUtils.isNotBlank(addressModel.getLatitude()) && StringUtils.isNotBlank(addressModel.getLongitude())) {
                String point = addressModel.getLongitude() + "," + addressModel.getLatitude();
                deliveryId = communityService.queryCommunityIdByCoordinates(point);
            }
            ConsigneeAddrDO consigneeAddrDO = new ConsigneeAddrDO();
            buildConsigneeDO(addressModel, consigneeAddrDO);
            consigneeAddrDO.setReceiverMobile(mobile);
            consigneeAddrDO.setCommunityId(deliveryId);
            consigneeAddrDO.setType(BizConstants.ConsigneeTypeEnum.SEARTCH.getType());
            consigneeAddrDO.setReceiverName(nick);
            if(null != getDef && "1".equals(getDef)){
            	//其余标记非默认
                makeConsigneesUndef(profileId);
            	consigneeAddrDO.setGetDef((byte) 1);
            }else{
            	consigneeAddrDO.setGetDef((byte) 0);
            }
            ConsigneeAddrDOExample qcExample = new ConsigneeAddrDOExample();
            qcExample.createCriteria().andUserIdEqualTo(profileId).andStatusEqualTo(1);
            List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(qcExample);
            boolean firstAdd = false;
            if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0) {
                //
            } else {
                consigneeAddrDO.setGetDef((byte) 1);
                firstAdd = true;
            }
            if (firstAdd) {
                consigneeAddrDO.setGetDef((byte) 1);
            }
            consigneeAddrDO.setVersion(1l);
            consigneeAddrDO.setUserId(profileId);
            consigneeAddrDO.setDateCreated(new Date());
            consigneeAddrDO.setStatus(1);
            if(StringUtils.isNotBlank(idCard)){
            	consigneeAddrDO.setIdCard(idCard);
            }
            if (consigneeAddrDOMapper.insertSelective(consigneeAddrDO) < 0) {
                log.error("add consignee address failed. profileId:" + profileId + ",sessionId:" + session.getId());
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.ADD_CONSIGNEE_ADDRESS_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.ADD_CONSIGNEE_ADDRESS_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            //如果是第一次添加收货地址，返回该收货地址
            if (firstAdd) {
                List<ConsigneeAddrDO> consigneeAddrDOList = consigneeAddrDOMapper.selectByExample(qcExample);
                List<ConsigneeViewDO> consigneeViewDOs = new ArrayList<>();
                if (null != consigneeAddrDOList && consigneeAddrDOList.size() > 0) {
                    for (ConsigneeAddrDO conDO : consigneeAddrDOList) {
                        consigneeViewDOs.add(ViewDOCopy.buildConsigneeViewDO(conDO));
                    }
                }
                welinkVO.setStatus(1);
                Map resultMap = new HashMap();
                resultMap.put("consignees", consigneeViewDOs);
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            }
        } else if (StringUtils.equalsIgnoreCase(op, "del")) {
            if (consigneeAddrDOMapper.deleteByPrimaryKey(id) < 0) {
                log.error("delete consignee address failed. profileId:" + profileId + ",id:" + id + ",sessionId:" + session.getId());
                //删除降级
            }
        }//选择收货地址
        else if (StringUtils.equalsIgnoreCase(op, "chs")) {
            //先查询
            ConsigneeAddrDOExample qcExample = new ConsigneeAddrDOExample();
            qcExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(1);
            List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(qcExample);
            //其余标记非默认
            makeConsigneesUndef(profileId);
            //标记默认
            makeConsigneeDef(id, consigneeAddrDOs);
        } else if (StringUtils.equalsIgnoreCase(op, "schs")) {//单个查询
            ConsigneeAddrDOExample qcExample = new ConsigneeAddrDOExample();
            qcExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(1);
            List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(qcExample);
            welinkVO.setStatus(1);
            Map resultMap = new HashMap();
            resultMap.put("consignees", consigneeAddrDOs);
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //其余情况都是查询更新后的收货地址
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andUserIdEqualTo(profileId).andStatusEqualTo(1);
        List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(cExample);
        List<ConsigneeViewDO> consigneeViewDOs = new ArrayList<>();

        for (ConsigneeAddrDO conDO : consigneeAddrDOs) {
            ConsigneeViewDO v = ViewDOCopy.buildConsigneeViewDO(conDO);
            consigneeViewDOs.add(v);
        }

        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        resultMap.put("consignees", consigneeViewDOs);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    private void makeConsigneeDef(long id, List<ConsigneeAddrDO> consigneeAddrDOs) {
        ConsigneeAddrDO uConsigneeAddrDO = new ConsigneeAddrDO();
        uConsigneeAddrDO.setLastUpdated(new Date());
        uConsigneeAddrDO.setGetDef((byte) 1);
        if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0) {
            if (null != consigneeAddrDOs.get(0).getLongitude() && null != consigneeAddrDOs.get(0).getLatitude()) {
                String point = consigneeAddrDOs.get(0).getLongitude() + "," + consigneeAddrDOs.get(0).getLatitude();
                long deliveId = communityService.queryCommunityIdByCoordinates(point);
                uConsigneeAddrDO.setCommunityId(deliveId);
            }
        }
        ConsigneeAddrDOExample uExample = new ConsigneeAddrDOExample();
        uExample.createCriteria().andIdEqualTo(id).andStatusEqualTo(1);
        consigneeAddrDOMapper.updateByExampleSelective(uConsigneeAddrDO, uExample);
    }

    private void makeConsigneesUndef(long profileId) {
        ConsigneeAddrDOExample qAllExample = new ConsigneeAddrDOExample();
        qAllExample.createCriteria().andUserIdEqualTo(profileId).andStatusEqualTo(1);
        List<ConsigneeAddrDO> consigneeAddrAll = consigneeAddrDOMapper.selectByExample(qAllExample);
        for (ConsigneeAddrDO consigneeAddrDO : consigneeAddrAll) {
            ConsigneeAddrDO uconsigneeAddrDO = new ConsigneeAddrDO();
            uconsigneeAddrDO.setGetDef((byte) 0);
            uconsigneeAddrDO.setLastUpdated(new Date());
            ConsigneeAddrDOExample cuExample = new ConsigneeAddrDOExample();
            cuExample.createCriteria().andIdEqualTo(consigneeAddrDO.getId()).andStatusEqualTo(1);
            consigneeAddrDOMapper.updateByExampleSelective(uconsigneeAddrDO, cuExample);
        }
    }

    /**
     * 构建build consignee do
     *
     * @param addressModel
     * @param consigneeAddrDO
     */
    private void buildConsigneeDO(AddressModel addressModel, ConsigneeAddrDO consigneeAddrDO) {
        String communityAddress = buildAddress(addressModel);
        String tmpComm = communityAddress.replace(BizConstants.SPLIT, "");
        if (StringUtils.isNotBlank(tmpComm)) {
            consigneeAddrDO.setCommunity(communityAddress);
        }
        if (null != addressModel.getProvince()) {
            consigneeAddrDO.setReceiver_state(addressModel.getProvince());
        }
        if (null != addressModel.getCity()) {
            consigneeAddrDO.setReceiverCity(addressModel.getCity());
        }
        if (null != addressModel.getDistrict()) {
            consigneeAddrDO.setReceiverDistrict(addressModel.getDistrict());
        }
        if (null != addressModel.getName()) {
            consigneeAddrDO.setCommunityName(addressModel.getName());
        }
        if (null != addressModel.getAddress()) {
            consigneeAddrDO.setReceiverAddress(addressModel.getAddress());
        }
        if (null != addressModel.getPostcode()) {
            consigneeAddrDO.setReceiverZip(addressModel.getPostcode());
        }
        if (null != addressModel.getPcode()) {
            consigneeAddrDO.setProvinceCode(addressModel.getPcode());
        }
        if (null != addressModel.getCitycode()) {
            consigneeAddrDO.setCityCode(addressModel.getCitycode());
        }
        if (null != addressModel.getAddcode()) {
            consigneeAddrDO.setAddCode(addressModel.getAddcode());
        }
        consigneeAddrDO.setLastUpdated(new Date());
        if (null != addressModel.getLatitude()) {
            consigneeAddrDO.setLatitude(addressModel.getLatitude());
        }
        if (null != addressModel.getLongitude()) {
            consigneeAddrDO.setLongitude(addressModel.getLongitude());
        }
    }

    /**
     * 获取高德逆地理需要的地址
     *
     * @param addressModel
     * @return
     */
    public String geoAddr(AddressModel addressModel) {
        if (null == addressModel) {
            return null;
        }
        String communityAddress = buildAddress(addressModel);
        String geoAddr = communityAddress.replace(BizConstants.SPLIT, "");
        return geoAddr;
    }

    private String buildAddress(AddressModel addressModel) {
        String address = "";
        if (StringUtils.isNotBlank(addressModel.getProvince())) {
            address += addressModel.getProvince();
        }
        address += BizConstants.SPLIT;
        if (StringUtils.isNotBlank(addressModel.getCity())) {
            address += addressModel.getCity();
        }
        if (StringUtils.isNotBlank(addressModel.getDistrict())) {
            address += addressModel.getDistrict();
        }
        address += BizConstants.SPLIT;
        if (StringUtils.isNotBlank(addressModel.getAddrgeo())) {
            address += addressModel.getAddrgeo();
        }
        if (StringUtils.isNotBlank(addressModel.getName())) {
            address += addressModel.getName();
        }
//        if (StringUtils.isNotBlank(addressModel.getAddress())) {
//            address += addressModel.getAddress();
//        }
        return address;
    }

    private boolean checkSession(WelinkVO welinkVO, Session session) {
        if (null == session) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
        if (null != session.getAttribute("profileId")) {
            return false;
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
    }
}
