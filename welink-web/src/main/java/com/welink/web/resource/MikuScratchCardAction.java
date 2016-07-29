/**
 * Project Name:welink-web
 * File Name:MikuScratchCardAction.java
 * Package Name:com.welink.web.resource
 * Date:2016年4月5日下午7:44:57
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuScratchCardDO;
import com.welink.commons.domain.MikuScratchCardDOExample;
import com.welink.commons.domain.MikuScratchCardDOExample.Criteria;
import com.welink.commons.domain.MikuActivityBonusDOExample;
import com.welink.commons.domain.MikuWalletDO;
import com.welink.commons.domain.MikuWalletDOExample;
import com.welink.commons.domain.MikuWalletOriginDO;
import com.welink.commons.domain.MikuWalletOriginDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuActivityBonusDOMapper;
import com.welink.commons.persistence.MikuScratchCardDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.promotion.drools.Utils;

/**
 * ClassName:MikuScratchCardAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年4月5日 下午7:44:57 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class MikuScratchCardAction {
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@Resource
	private MikuScratchCardDOMapper mikuScratchCardDOMapper;
	
	@Resource
	private MikuActivityBonusDOMapper mikuActivityBonusDOMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	@Resource
    private Env env;
	
	@RequestMapping(value = {"/api/m/1.0/scratchCard.json", "/api/h/1.0/scratchCard.json"}, produces = "application/json;charset=utf-8")
	public String getMyWallet(HttpServletRequest request, HttpServletResponse response, String number) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId || profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		if(StringUtils.isBlank(number) || !StringUtils.isNumeric(number) || (null != number && number.length() != 16)){
			welinkVO.setStatus(0);
			welinkVO.setCode(6);
			welinkVO.setMsg("亲~您输入的刮刮卡号码有误，请重新输入~");
			return JSON.toJSONString(welinkVO);
		}
		int maxScratchCard = 10;
		String probability = "0.083";
		if (env.isProd()) {
			maxScratchCard = 10;
			probability = "0.083";
        } else {
        	maxScratchCard = 10;
        	probability = "0.083";
        }
		MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
		mikuScratchCardDOExample.createCriteria().andUserIdEqualTo(profileId)
			.andDateCreatedGreaterThan(getTimesmorning());
		int countScratchCard = mikuScratchCardDOMapper.countByExample(mikuScratchCardDOExample);
		if(countScratchCard > maxScratchCard){	//每天最多刮5张
			welinkVO.setStatus(0);
			welinkVO.setCode(6);
			welinkVO.setMsg("亲~每天最多刮"+maxScratchCard+"张喲~");
			return JSON.toJSONString(welinkVO);
		}
		
		MikuActivityBonusDOExample mikuActivityBonusDOExample = new MikuActivityBonusDOExample();
		mikuActivityBonusDOExample.createCriteria().andNumberEqualTo(number);
		int countNumber = mikuActivityBonusDOMapper.countByExample(mikuActivityBonusDOExample);
		if(countNumber < 1){	//查询是否有此兑奖号码
			welinkVO.setStatus(0);
			welinkVO.setCode(6);
			welinkVO.setMsg("亲~没有此兑奖号码，请重新输入~");
			return JSON.toJSONString(welinkVO);
		}
		
		MikuScratchCardDOExample mikuScratchCardDOExample2 = new MikuScratchCardDOExample();
		mikuScratchCardDOExample2.createCriteria().andNumberEqualTo(number);
		int countHasScratchCard = mikuScratchCardDOMapper.countByExample(mikuScratchCardDOExample2);
		if(countHasScratchCard > 0){	//刮刮卡已使用过
			welinkVO.setStatus(0);
			welinkVO.setCode(6);
			welinkVO.setMsg("亲~号码为<"+number+">的刮刮卡已使用过~");
			return JSON.toJSONString(welinkVO);
		}
		
		Date now = new Date();
		boolean isReward = false;
		MikuScratchCardDO mikuScratchCardDO = null;
		if(Utils.lucky(probability)){
			ItemExample itemExample = new ItemExample();
			itemExample.createCriteria().andApproveStatusEqualTo((byte)1)
				.andBaseItemIdIsNotNull().andTypeEqualTo(Constants.TradeType.scratch_card.getTradeTypeId());
			List<Item> itemList = itemMapper.selectByExample(itemExample);
			if(!itemList.isEmpty()){
				Item item = itemList.get(0);
				if(null != item && null != item.getNum() && item.getNum() > 0){	//如果中奖
					isReward = true;
					mikuScratchCardDO = new MikuScratchCardDO();
					mikuScratchCardDO.setUserId(profileId);
					mikuScratchCardDO.setNumber(number);
					mikuScratchCardDO.setIsReward((byte)1);		//0=未中奖；1=中奖
					mikuScratchCardDO.setItemId(item.getId());
					mikuScratchCardDO.setItemTitle(item.getTitle());
					if (StringUtils.isNoneBlank(item.getPicUrls())) {
						mikuScratchCardDO.setPicUrls(StringUtils.split(item.getPicUrls(), ';')[0]);
					}
					mikuScratchCardDO.setStatus(Constants.ScratchCardStatus.NO_ORDER.getStatusId());;	//状态(0=已取消;1=未下单;2=已下单;3=已付款)
					mikuScratchCardDO.setVersion(1L);
					mikuScratchCardDO.setDateCreated(now);
					mikuScratchCardDO.setLastUpdated(now);
					resultMap.put("isReward", 1);	//0=未中奖；1=中奖
				}
			}
		}
		if(!isReward){	//如果未中奖
			mikuScratchCardDO = new MikuScratchCardDO();
			mikuScratchCardDO.setUserId(profileId);
			mikuScratchCardDO.setNumber(number);
			mikuScratchCardDO.setIsReward((byte)0);		//0=未中奖；1=中奖
			mikuScratchCardDO.setStatus(Constants.ScratchCardStatus.NO_ORDER.getStatusId());;	//状态(0=已取消;1=未下单;2=未付款;3=已付款)
			mikuScratchCardDO.setVersion(1L);
			mikuScratchCardDO.setDateCreated(now);
			mikuScratchCardDO.setLastUpdated(now);
			resultMap.put("isReward", 0);	//0=未中奖；1=中奖
		}
		if(null != mikuScratchCardDO){
			if(mikuScratchCardDOMapper.insertSelective(mikuScratchCardDO) < 1){
				welinkVO.setStatus(0);
				welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
				welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
				return JSON.toJSONString(welinkVO);
			}
			resultMap.put("vo", mikuScratchCardDO);
		}
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * getMyScratchCardList:(获取我的刮刮卡列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/getMyScratchCardList.json", "/api/h/1.0/getMyScratchCardList.json"}, produces = "application/json;charset=utf-8")
	public String getMyScratchCardList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="isReward", required = false, defaultValue = "1") Byte isReward,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
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
        
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
		Map resultMap = new HashMap();
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0L) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		
		MikuScratchCardDOExample mikuScratchCardDOExample = new MikuScratchCardDOExample();
		Criteria createCriteria = mikuScratchCardDOExample.createCriteria();
		createCriteria.andUserIdEqualTo(profileId);
		if(null != isReward && isReward >= (byte)0){
			createCriteria.andIsRewardEqualTo(isReward);
		}
		mikuScratchCardDOExample.setOffset(startRow);
		mikuScratchCardDOExample.setLimit(size);
		List<MikuScratchCardDO> mikuScratchCardDOList = mikuScratchCardDOMapper.selectByExample(mikuScratchCardDOExample);
		boolean hasNext = true;
		if (null != mikuScratchCardDOList && mikuScratchCardDOList.size() < size) {
			hasNext = false;
		}else if(null == mikuScratchCardDOList){
			hasNext = false;
		}else{
			hasNext = true;
		}
		welinkVO.setStatus(1);
		resultMap.put("list", mikuScratchCardDOList);
		resultMap.put("hasNext", hasNext);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
		
	}
	
	private Date getTimesmorning(){  
        Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime();  
    }
	
}

