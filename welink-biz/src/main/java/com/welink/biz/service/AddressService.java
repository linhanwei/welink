package com.welink.biz.service;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.MSG.HttpRequest;
import com.welink.biz.common.model.AddressResult;
import com.welink.biz.common.model.CommunityViewDO;
import com.welink.biz.common.model.GaoDeResult;
import com.welink.biz.common.model.LngLat;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.AddressDOMapper;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.commons.persistence.LogisticsDOMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 14-9-25.
 */
@Service
public class AddressService {

    private static Logger log = LoggerFactory.getLogger(AddressService.class);

    @Resource
    private AddressDOMapper addressDOMapper;

    @Resource
    private CommunityDOMapper communityDOMapper;

    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;

    @Resource
    private LogisticsDOMapper logisticsDOMapper;

    @Resource
    private UserService userService;

    @Resource
    private CommunityService communityService;

    /**
     * 获取用户最后登录的站点
     *
     * @param profileId
     * @return
     */
    public CommunityDO fetchLastLoginCommunity(long profileId) {
        CommunityDO communityDO = null;
        ProfileDO profileDO = userService.fetchProfileById(profileId);
        if (null != profileDO) {
            communityDO = fetchCommunity(profileDO.getLastCommunity());
        }
        return communityDO;
    }

    /**
     * 根据经纬度获取站点信息
     *
     * @param point
     * @return
     */
    public CommunityDO fetchCommunityViaLatlng(String point) {
        if (StringUtils.isBlank(point)) {
            log.error("根据经纬度获取站点信息失败...经纬度参数错误 point:");
            return null;
        }
        CommunityDO communityDO = null;
        long communityId = communityService.queryCommunityIdByCoordinates(point);
        communityDO = fetchCommunity(communityId);
        return communityDO;
    }

    /**
     * 获取所有自提点信息
     *
     * @return
     */
    public List<CommunityViewDO> fetchCommunityViewDOs() {
        CommunityDOExample communityDOExample = new CommunityDOExample();
        communityDOExample.createCriteria().andStatusEqualTo((byte) 1);
        communityDOExample.setOrderByClause("id DESC");
        List<CommunityDO> comtDOs = communityDOMapper.selectByExample(communityDOExample);
        List<CommunityViewDO> communityViewDOs = new ArrayList<>();
        if (null != comtDOs && comtDOs.size() > 0) {
            for (CommunityDO communityDO : comtDOs) {
                communityViewDOs.add(ViewDOCopy.buildCommunityViewDO(communityDO));
            }
        }
        return communityViewDOs;
    }

    /**
     * 获取用户的收货地址
     *
     * @param userId
     * @return
     */
    public List<ConsigneeAddrDO> fetchConsignee(long userId) {
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(1);
        return consigneeAddrDOMapper.selectByExample(cExample);
    }

    /**
     * 获取用户的默认收货地址
     *
     * @param userId
     * @return
     */
    public ConsigneeAddrDO fetchDefConsignee(long userId) {
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andUserIdEqualTo(userId).andGetDefEqualTo((byte) 1).andStatusEqualTo(1);
        List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(cExample);
        if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0) {
            return consigneeAddrDOs.get(0);
        }
        return null;
    }

    /**
     * 添加物流单据
     *
     * @param consigneeAddrDO
     * @param type 类型(0=购买物流;1=退货物流)
     * @return
     */
    public long addLogistics(ConsigneeAddrDO consigneeAddrDO, long shippingId, byte type) {
        if (shippingId > 0) {
            CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(shippingId);
            if (null != communityDO) {
                LogisticsDO logisticsDO = new LogisticsDO();
                logisticsDO.setLastUpdated(new Date());
                //logisticsDO.setAddr(communityDO.getLocation());
                logisticsDO.setAddr(consigneeAddrDO.getCommunity() + BizConstants.SPLIT + consigneeAddrDO.getReceiverAddress());
                logisticsDO.setCancelDef((byte) 0);
                logisticsDO.setCity(communityDO.getCity());
                logisticsDO.setCommunity_id(shippingId);
                logisticsDO.setContactName(consigneeAddrDO.getReceiverName());
                logisticsDO.setCountry("中国");
                logisticsDO.setGetDef((byte) 0);
                logisticsDO.setDistrictId(-1l);
                logisticsDO.setMemo(null);
                logisticsDO.setMobile(consigneeAddrDO.getReceiverMobile());
                logisticsDO.setPhone(consigneeAddrDO.getReceiverPhone());
                logisticsDO.setUserId(consigneeAddrDO.getUserId());
                logisticsDO.setZipCode(null);
                logisticsDO.setDateCreated(new Date());
                logisticsDO.setSellerCompany(null);
                logisticsDO.setProvince(communityDO.getProvince());
                logisticsDO.setConsigneeId(BizConstants.SHIPPING_SELF_PICK_CONSING_ID);//自提consignId -1
                logisticsDO.setType(type);
                logisticsDO.setIdCard(consigneeAddrDO.getIdCard());
                if (logisticsDOMapper.insertSelective(logisticsDO) < 0) {
                    log.error("添加自提收货地址单据失败. add logistics failed. userId:" + consigneeAddrDO.getUserId());
                    return -1;
                }
                return logisticsDO.getId();
            }
            log.error("add logistics for self picking... shippingId:" + shippingId);
            return -1l;
        } else {
            LogisticsDO logisticsDO = new LogisticsDO();
            logisticsDO.setLastUpdated(new Date());
            logisticsDO.setAddr(consigneeAddrDO.getCommunity() + BizConstants.SPLIT + consigneeAddrDO.getReceiverAddress());
            logisticsDO.setLatitude(consigneeAddrDO.getLatitude());
            logisticsDO.setLongitude(consigneeAddrDO.getLongitude());
            logisticsDO.setCancelDef((byte) 0);
            logisticsDO.setCity(consigneeAddrDO.getReceiverCity());
            logisticsDO.setCommunity_id(consigneeAddrDO.getCommunityId());
            logisticsDO.setContactName(consigneeAddrDO.getReceiverName());
            logisticsDO.setCountry("中国");
            logisticsDO.setGetDef((byte) 0);
            logisticsDO.setDistrictId(-1l);
            logisticsDO.setMemo(null);
            logisticsDO.setMobile(consigneeAddrDO.getReceiverMobile());
            logisticsDO.setPhone(consigneeAddrDO.getReceiverPhone());
            logisticsDO.setUserId(consigneeAddrDO.getUserId());
            logisticsDO.setZipCode(null);
            logisticsDO.setDateCreated(new Date());
            logisticsDO.setSellerCompany(null);
            logisticsDO.setProvince(consigneeAddrDO.getReceiver_state());
            logisticsDO.setConsigneeId(consigneeAddrDO.getId());
            logisticsDO.setIdCard(consigneeAddrDO.getIdCard());
            if (logisticsDOMapper.insertSelective(logisticsDO) < 0) {
                log.error("添加收货地址单据失败. add logistics failed. userId:" + consigneeAddrDO.getUserId());
                return -1;
            }
            return logisticsDO.getId();
        }
    }

    /**
     * 根据id查询收货地址信息
     *
     * @param cId
     * @return
     */
    public ConsigneeAddrDO fetchConsigneeByConsigneeId(long cId) {
        return consigneeAddrDOMapper.selectByPrimaryKey(cId);
    }

    /**
     * 根据logistics id 获取地址
     *
     * @param cId
     * @return
     */
    public LogisticsDO fetchLogisticsByConsigneeId(long cId) {
        return logisticsDOMapper.selectByPrimaryKey(cId);
    }

    /**
     * 更新收货地址 update or insert
     *
     * @param userId
     * @param communityId
     * @param mobile
     * @param address
     * @return
     */
    public AddressResult updateConsignee(long userId, long communityId, String mobile, String address, String nick) {
        AddressResult result = new AddressResult();
        result.setUpdate(false);
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(1);
        List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(cExample);
        CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(communityId);
        if (null == communityDO) {
            result.setUpdate(false);
            return result;
        }
        //update
        if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0) {
            ConsigneeAddrDO consigneeAddrDO = new ConsigneeAddrDO();
            consigneeAddrDO.setLastUpdated(new Date());
            consigneeAddrDO.setReceiverAddress(address);
            if (StringUtils.isNotBlank(nick)) {
                consigneeAddrDO.setReceiverName(nick);
            }
            consigneeAddrDO.setReceiverMobile(mobile);
            consigneeAddrDO.setVersion(consigneeAddrDOs.get(0).getVersion() + 1);
            consigneeAddrDO.setCommunityId(communityId);
            consigneeAddrDO.setCommunity(communityDO.getProvince() + communityDO.getCity() + communityDO.getLocation() + BizConstants.SPLIT + communityDO.getName());
            consigneeAddrDO.setReceiverDistrict(communityDO.getDistrict());
            consigneeAddrDO.setReceiverCity(communityDO.getCity());
            consigneeAddrDO.setReceiver_state(communityDO.getProvince());
            if (consigneeAddrDOMapper.updateByExampleSelective(consigneeAddrDO, cExample) < 1) {
                log.error("update consignee address failed. userId:" + userId + ",mobile:" + mobile);
                result.setUpdate(false);
                return result;
            }
            cExample.createCriteria().andUserIdEqualTo(userId).andCommunityIdEqualTo(communityId).andStatusEqualTo(1);
            List<ConsigneeAddrDO> consigneeAddrDOList = consigneeAddrDOMapper.selectByExample(cExample);
            if (consigneeAddrDOList != null && consigneeAddrDOList.size() > 0) {
                result.setUpdate(true);
                result.setAddress(consigneeAddrDOList.get(0).getCommunity() + consigneeAddrDO.getReceiverAddress());
                result.setName(consigneeAddrDOList.get(0).getReceiverName());
            } else {
                result.setUpdate(false);
            }
            return result;
        }
        //insert
        if (null != communityDO) {
        } else {
            log.error("update consignee address failed. userId:" + userId + ",mobile:" + mobile);
            result.setUpdate(false);
            return result;
        }
        ConsigneeAddrDO consigneeAddrDO = new ConsigneeAddrDO();
        consigneeAddrDO.setDateCreated(new Date());
        consigneeAddrDO.setLastUpdated(new Date());
        consigneeAddrDO.setReceiverAddress(address);
        consigneeAddrDO.setReceiverMobile(String.valueOf(mobile));
        consigneeAddrDO.setReceiver_state(communityDO.getProvince());
        consigneeAddrDO.setReceiverCity(communityDO.getCity());
        consigneeAddrDO.setReceiverDistrict(communityDO.getDistrict());
        consigneeAddrDO.setUserId(userId);
        if (StringUtils.isNotBlank(nick)) {
            consigneeAddrDO.setReceiverName(nick);
        }
        consigneeAddrDO.setVersion(0l);
        consigneeAddrDO.setCommunityId(communityId);
        consigneeAddrDO.setCommunity(communityDO.getProvince() + communityDO.getCity() + communityDO.getLocation() + BizConstants.SPLIT + communityDO.getName());
        long update = consigneeAddrDOMapper.insertSelective(consigneeAddrDO);
        if (update < 0) {
            log.error("insert consignee address failed. userId:" + userId + ",mobile:" + mobile);
            result.setUpdate(false);
            return result;
        }
        List<ConsigneeAddrDO> consigneeAddrDOList = consigneeAddrDOMapper.selectByExample(cExample);
        if (consigneeAddrDOList != null && consigneeAddrDOList.size() > 0) {
            result.setUpdate(true);
            result.setAddress(consigneeAddrDOList.get(0).getCommunity() + consigneeAddrDO.getReceiverAddress());
            result.setName(consigneeAddrDOList.get(0).getReceiverName());
        } else {
            result.setUpdate(false);
        }
        return result;
    }

    /**
     * happy add consignee address
     *
     * @param userId
     * @param receiverName
     * @param receiverMobile
     * @param receiverState
     * @param receiverCity
     * @param receiverDistrict
     * @param receiverAddress
     * @param receiverZip
     * @param communityId
     * @param getDef
     * @param cancelDef
     * @return
     */
    public boolean addConsigneeAddress(long userId, String receiverName, String receiverMobile, String receiverState, String receiverCity, String receiverDistrict,
                                       String receiverAddress, String receiverZip, long communityId, byte getDef, byte cancelDef) {
        ConsigneeAddrDO consigneeAddrDO = new ConsigneeAddrDO();
        consigneeAddrDO.setUserId(userId);
        consigneeAddrDO.setReceiverName(receiverName);
        consigneeAddrDO.setReceiverMobile(receiverMobile);
        consigneeAddrDO.setReceiver_state(receiverState);
        consigneeAddrDO.setReceiverCity(receiverCity);
        consigneeAddrDO.setReceiverDistrict(receiverDistrict);
        consigneeAddrDO.setReceiverAddress(receiverAddress);
        consigneeAddrDO.setReceiverZip(receiverZip);
        consigneeAddrDO.setCommunityId(communityId);
        consigneeAddrDO.setGetDef(getDef);
        consigneeAddrDO.setCancelDef(cancelDef);
        consigneeAddrDO.setDateCreated(new Date());
        consigneeAddrDO.setLastUpdated(new Date());
        consigneeAddrDO.setVersion(1l);
        consigneeAddrDO.setCommunity(genCommunity(receiverState, receiverCity, receiverDistrict, receiverAddress));
        if (consigneeAddrDOMapper.insertSelective(consigneeAddrDO) < 0) {
            log.error("insert consignee address failed. userId:" + userId);
            return false;
        }
        return true;
    }

    /**
     * happy 更新收货地址
     *
     * @param id
     * @param receiverName
     * @param receiverMobile
     * @param receiverState
     * @param receiverCity
     * @param receiverDistrict
     * @param receiverAddress
     * @param receiverZip
     * @param communityId
     * @param getDef
     * @param cancelDef
     * @return
     */
    public boolean updateConsigneeAddress(long id, String receiverName, String receiverMobile, String receiverState, String receiverCity, String receiverDistrict,
                                          String receiverAddress, String receiverZip, long communityId, byte getDef, byte cancelDef) {
        ConsigneeAddrDO qConsigneeAddrDO = consigneeAddrDOMapper.selectByPrimaryKey(id);
        long version = qConsigneeAddrDO.getVersion();

        ConsigneeAddrDO consigneeAddrDO = new ConsigneeAddrDO();
        consigneeAddrDO.setReceiverName(receiverName);
        consigneeAddrDO.setReceiverMobile(receiverMobile);
        consigneeAddrDO.setReceiver_state(receiverState);
        consigneeAddrDO.setReceiverCity(receiverCity);
        consigneeAddrDO.setReceiverDistrict(receiverDistrict);
        consigneeAddrDO.setReceiverAddress(receiverAddress);
        consigneeAddrDO.setReceiverZip(receiverZip);
        consigneeAddrDO.setCommunityId(communityId);
        consigneeAddrDO.setGetDef(getDef);
        consigneeAddrDO.setCancelDef(cancelDef);
        consigneeAddrDO.setLastUpdated(new Date());
        consigneeAddrDO.setVersion(version + 1l);
        consigneeAddrDO.setCommunity(genCommunity(receiverState, receiverCity, receiverDistrict, receiverAddress));
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andIdEqualTo(id);
        if (consigneeAddrDOMapper.updateByExampleSelective(consigneeAddrDO, cExample) < 1) {
            log.error("update consignee address failed. consignee id:" + id);
            return false;
        }
        return true;
    }

    /**
     * 根据userId获取用户的全部收获地址
     *
     * @param userId
     * @return
     */
    public List<ConsigneeAddrDO> fetchConsigneesByUserId(long userId) {
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(1);
        List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(cExample);
        if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0) {
            return consigneeAddrDOs;
        }
        return null;
    }

    /**
     * 构建收货地址全地址
     *
     * @param receiverState
     * @param receiverCity
     * @param receiverDistrict
     * @param receiverAddress
     * @return
     */
    private String genCommunity(String receiverState, String receiverCity, String receiverDistrict, String receiverAddress) {
        String community = "";
        if (StringUtils.isNotBlank(receiverState)) {
            community += receiverState;
        }
        if (StringUtils.isNotBlank(receiverCity)) {
            community += receiverCity;
        }
        if (StringUtils.isNotBlank(receiverDistrict)) {
            community += receiverDistrict;
        }
//        if (StringUtils.isNotBlank(receiverAddress)) {
//            community += receiverAddress;
//        }
        return community;
    }

    public List<AddressDO> fetchAddressByCommunityId_lv1(long communityId, byte level, long parentId) {
        AddressDOExample level1 = new AddressDOExample();
        level1.createCriteria() //
                .andCommunityIdEqualTo(communityId); //

        if (parentId >= 0 && level <= 0) {
            level1 = new AddressDOExample();
            level1.createCriteria() //
                    .andCommunityIdEqualTo(communityId) //
                    .andParentIdEqualTo(parentId);
        }
        if (parentId >= 0 && level > 0) {
            level1 = new AddressDOExample();
            level1.createCriteria() //
                    .andCommunityIdEqualTo(communityId) //
                    .andParentIdEqualTo(parentId)
                    .andLevelEqualTo(level);
        }

        List<AddressDO> addressDOs = addressDOMapper.selectByExample(level1);

        return addressDOs;
    }

    public AddressDO fetchAddressByBuildingId(long buildingId) {
        AddressDO addressDO = new AddressDO();
        AddressDOExample example = new AddressDOExample();
        example.createCriteria().andBuildingIdEqualTo(buildingId);
        List<AddressDO> addressDOs = addressDOMapper.selectByExample(example);
        if (null != addressDOs && addressDOs.size() > 0) {
            addressDO = addressDOs.get(0);
        }
        return addressDO;
    }

    public AddressDO fetchAddressByBuildingIdAndCommunityId(long buildingId, long communityId) {
        AddressDO addressDO = new AddressDO();
        AddressDOExample example = new AddressDOExample();
        example.createCriteria().andBuildingIdEqualTo(buildingId).andCommunityIdEqualTo(communityId);
        List<AddressDO> addressDOs = addressDOMapper.selectByExample(example);
        if (null != addressDOs && addressDOs.size() > 0) {
            addressDO = addressDOs.get(0);
        }
        return addressDO;
    }

    /**
     * 根据communityId获取community
     *
     * @param communityId
     * @return
     */
    public CommunityDO fetchCommunity(Long communityId) {
        if (communityId != null && communityId > 0) {
            CommunityDOExample cExample = new CommunityDOExample();
            cExample.createCriteria().andIdEqualTo(communityId).andStatusEqualTo((byte) 1);
            List<CommunityDO> communityDOs = communityDOMapper.selectByExample(cExample);
            if (null != communityDOs && communityDOs.size() > 0) {
                return communityDOs.get(0);
            }
        }
        return null;
    }


    /**
     * 逆地理轉換
     *
     * @param addr
     * @return
     */
    public LngLat deGeo(String addr) {
        String url = "http://restapi.amap.com/v3/geocode/geo";
        String params = "address=" + addr + "&key=36d9f8e2f79ffb2dd451b6d109ac1d4b";
        LngLat location = new LngLat();
        String result = HttpRequest.sendGet(url, params);
        if (StringUtils.isNotBlank(result)) {
            GaoDeResult gResult = JSON.parseObject(result, GaoDeResult.class);
            if (null != gResult && StringUtils.equals("1", gResult.getStatus()) && null != gResult.getGeocodes() &&
                    gResult.getGeocodes().size() > 0) {
                String latlng = gResult.getGeocodes().get(0).getLocation();
                if (StringUtils.isNotBlank(latlng) && latlng.split(",").length > 1) {
                    location.setLongitude(latlng.split(",")[0]);
                    location.setLatitude(latlng.split(",")[1]);
                    return location;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String split = "\u0001";
        String a = "你好";
        String b = "不可见";
        System.out.println(a + split + b);
        String d = a + split + b;
        System.out.println(d.split(a)[1]);

        String location = "120.019197,30.241384";
    }
}
