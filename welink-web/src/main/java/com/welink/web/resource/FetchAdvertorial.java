package com.welink.web.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.thoughtworks.xstream.mapper.Mapper.Null;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.domain.MikuAdvertorialDO;
import com.welink.commons.domain.MikuAdvertorialDOExample;
import com.welink.commons.persistence.MikuAdvertorialDOMapper;

/**
 * 软文链接(发现)接口
 * Created by lin on 16-04-20.
 */
@RestController
public class FetchAdvertorial {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchAdvertorial.class);

    @Resource
    private MikuAdvertorialDOMapper mikuAdvertorialDOMapper;

    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/FetchAdvertorial.json", "/api/h/1.0/FetchAdvertorial.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
//    		@RequestParam(value="advType", required = false, defaultValue="")  Byte advType,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "10") Integer sz) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
//        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
//        Session session = currentUser.getSession();
//       
//        Byte advertorialType = advType;
        int page = pg;
        int size = sz;
        if (size < 1 || size > 50) {
            size = 10;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
        String sortTypeStr = " last_updated DESC";	//排序类型
        
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
         
        MikuAdvertorialDOExample mikuAdvertorialDOExample = new MikuAdvertorialDOExample();
        
//        if(advertorialType != null){
//        	mikuAdvertorialDOExample.createCriteria().andAdvertorialTypeEqualTo(advertorialType); //查询条件
//        }
        
        mikuAdvertorialDOExample.createCriteria().andStatusEqualTo((byte) 1).andIsDeleteEqualTo((byte) 0);//查询条件
        
        mikuAdvertorialDOExample.setOffset(startRow);     //从哪条开始
        mikuAdvertorialDOExample.setLimit(size);		//每页多少条
        mikuAdvertorialDOExample.setOrderByClause(sortTypeStr);  //排序
        List<MikuAdvertorialDO> mikuAdvertorialDOs = mikuAdvertorialDOMapper.selectByExample(mikuAdvertorialDOExample);
        
        resultMap.put("list", mikuAdvertorialDOs);
       
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    
}