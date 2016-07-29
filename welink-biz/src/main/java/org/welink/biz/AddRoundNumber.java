package org.welink.biz;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.MSG.HttpRequest;
import com.welink.commons.commons.BizConstants;
import com.welink.biz.common.model.BaiduResponse;
import com.welink.biz.common.model.PlaceEn;
import com.welink.commons.domain.AroundNumberDO;
import com.welink.commons.persistence.AroundNumberDOMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by daniel on 14-10-15.
 */
public class AddRoundNumber {

    @Resource
    private static AroundNumberDOMapper aroundNumberDOMapper;

    public static void main(String[] args) {
        //百度获取电话信息并入库
        String q = "超市去死吧";
        String location = "39.915,116.404";
        String url = BizConstants.BAIDU_API_URL;
        String radius = "3000";
        String pageSize = "20";
        String url1 = BizConstants.BAIDU_API_URL + "ak=" + BizConstants.BAIDU_API_AK + "&output=json&query=" + q + "&page_size=" + pageSize + "&page_num=0&scope=2&location=" +
                location + "&radius=" + radius;
        String body = HttpRequest.sendGet(BizConstants.BAIDU_API_URL, "ak=" + BizConstants.BAIDU_API_AK + "&output=json&query=" + q + "&page_size=" + pageSize + "&page_num=0&scope=2&location=" +
                location + "&radius=" + radius);
        BaiduResponse brsp = JSON.parseObject(body, BaiduResponse.class);
        if (null != brsp && brsp.getResults() != null && brsp.getResults().size() > 0) {
            System.out.println("===============\ntotal:" + brsp.getResults().size());
            for (PlaceEn pe : brsp.getResults()) {
                long communityId = 199l;
                byte type = BizConstants.NumberType.MARKET.getType();
                double lat = pe.getLocation().getLat();
                double lng = pe.getLocation().getLng();
                String address = pe.getAddress();
                String tel = pe.getTelephone();
                long count = 0l;
                long distance = pe.getDetail_info().getDistance();
                String tags = pe.getDetail_info().getTag();
                String name = pe.getName();
                long price = 0l;
                Date dateCreated = new Date();
                Date lastUpdated = new Date();

                boolean add = false;
                if (StringUtils.isNotBlank(tel)) {
                    add = addArdNumbers(communityId, type, lat, lng, address,
                            tel, distance, tags, name, price);
                }
                System.out.println("添加结果:" + add);
            }
        }
    }

    public static boolean addArdNumbers(long communityId, byte type, double lat, double lng, String address,
                                        String tel, long distance, String tags, String name, long price) {


        ApplicationContext ac = new FileSystemXmlApplicationContext("//users/daniel/work/welinkCommons/welink/welink-web/src/main/resources/applicationContext.xml");
        AroundNumberDOMapper aroundNumberDOMapper1 = (AroundNumberDOMapper) ac.getBean("aroundNumberDOMapper");
        AroundNumberDO aroundNumberDO = new AroundNumberDO();
        aroundNumberDO.setLastUpdated(new Date());
        aroundNumberDO.setAddress(address);
        aroundNumberDO.setCommunityId(communityId);
        aroundNumberDO.setCount(0l);
        aroundNumberDO.setDateCreated(new Date());
        aroundNumberDO.setDistance(distance);
        aroundNumberDO.setLat(lat);
        aroundNumberDO.setLng(lng);
        aroundNumberDO.setName(name);
        aroundNumberDO.setPrice(price);
        aroundNumberDO.setTags(tags);
        aroundNumberDO.setTel(tel);
        aroundNumberDO.setType(type);
        aroundNumberDO.setTypeName(BizConstants.NumberType.getNumberTypeName(type));
        aroundNumberDOMapper1.insertSelective(aroundNumberDO);
        return false;
    }
}
