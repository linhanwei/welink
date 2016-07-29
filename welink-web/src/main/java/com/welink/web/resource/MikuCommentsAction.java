package com.welink.web.resource;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.MikuCommentsService;
import com.welink.commons.domain.MikuCommentsCountDO;
import com.welink.commons.domain.MikuCommentsCountDOExample;
import com.welink.commons.domain.MikuCommentsCountDOExample.Criteria;
import com.welink.commons.domain.MikuCommentsDO;
import com.welink.commons.domain.MikuCommentsDOExample;
import com.welink.commons.domain.MikuCommentsReplyDO;
import com.welink.commons.domain.MikuCommentsReplyDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuCommentsCountDOMapper;
import com.welink.commons.persistence.MikuCommentsDOMapper;
import com.welink.commons.persistence.MikuCommentsReplyDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.utils.UpYunUtil;

/**
 * 
 * ClassName: MikuCommentsAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2016年1月6日 下午6:03:38 <br/>
 *
 * @author LuoGuangChun
 * @version 
 * @since JDK 1.6
 */
@RestController
public class MikuCommentsAction {
	
	@Resource
	private MikuCommentsService mikuCommentsService;
	
	@Resource
	private MikuCommentsDOMapper mikuCommentsDOMapper;
	
	@Resource
	private MikuCommentsReplyDOMapper mikuCommentsReplyDOMapper;
	
	@Resource
	private MikuCommentsCountDOMapper mikuCommentsCountDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	/**
	 * 
	 * addComments:(对单个进行评论). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param buildingType	评论类型（如商品=1；众筹=2）
	 * @param star			星级评论	(1到5星)，默认5星
	 * @param pics			上传图片
	 * @param buildingId	评论对象（如商品id；众筹id）
	 * @param content		评论内容
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/addSingleComments.json", "/api/h/1.0/addSingleComments.json"}, produces = "application/json;charset=utf-8")
	public String addSingleComments(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="tradeId", required = false, defaultValue = "-1") Long tradeId,
			@RequestParam(value="buildingType", required = false, defaultValue = "1") byte buildingType,
			@RequestParam(value="star", required = false, defaultValue = "5") byte star,
			@RequestParam(value="content", required = false, defaultValue = "") String content,
			//@RequestParam(value="pics", required = false) MultipartFile[] pics,
			@RequestParam(value="picUrls", required = false, defaultValue = "") String picUrls,
			Long buildingId) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		Date now = new Date();
		if(null == profileDO){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		//获取敏感词
		Map<String, List<String>> sensitiveWordsMapCache = mikuCommentsService.getSensitiveWordsMapCache();
		boolean wordCheckFlag = false;	//敏感词需要审核Flag
		boolean wordBlacklistFlag = false;	//敏感词黑名单Flag
		//敏感词type 类型(1=需要审核;2=黑名单)
		if(null != sensitiveWordsMapCache){
			List<String> words = sensitiveWordsMapCache.get("1");
			List<String> words2 = sensitiveWordsMapCache.get("2");
			if(null != words && !words2.isEmpty()){	//敏感词type 类型(2=黑名单)
				String hasWordBlack = "";
				for(String word : words2){
					if(content.indexOf(word) > -1){
						hasWordBlack = word;
						wordBlacklistFlag = true;
						break;
					}
				}
				if(wordBlacklistFlag){	//若包含黑名单词语
					welinkVO.setStatus(0);
					welinkVO.setCode(BizErrorEnum.SENSITIVE_WORD.getCode());
					welinkVO.setMsg(BizErrorEnum.SENSITIVE_WORD.getMsg()+"<"+hasWordBlack+">");
					return JSON.toJSONString(welinkVO);
				}
			}
			if(null != words && !words.isEmpty()){	//敏感词type 类型(1=需要审核)
				for(String word : words){
					if(content.indexOf(word) > -1){
						wordCheckFlag = true;
						break;
					}
				}
			}
		}
		/*String picUrls = "";
		// 判断文件是否为空
		if (null != pics && pics.length > 0) {
			for(MultipartFile pic : pics){
				if(null != pic && !pic.isEmpty()){
					picUrls += "";
				}
			}
		}*/
		MikuCommentsDO mikuCommentsDO = new MikuCommentsDO();
		mikuCommentsDO.setUserId(profileId);
		mikuCommentsDO.setBuildingId(buildingId);
		mikuCommentsDO.setBuildingType(buildingType);
		if(StringUtils.isNotBlank(content)){
			mikuCommentsDO.setContent(content);
		}
		mikuCommentsDO.setUserName(profileDO.getNickname());
		//mikuCommentsDO.setMobile
		mikuCommentsDO.setStar(star);
		if(wordCheckFlag){	//包含敏感词
			mikuCommentsDO.setStatus((byte)0);
		}else{
			mikuCommentsDO.setStatus((byte)1);
		}
		mikuCommentsDO.setPicUrls(picUrls);
		mikuCommentsDO.setDateCreated(now);
		mikuCommentsDO.setLastUpdated(now);
		//mikuCommentsService.addComments(profileDO, mikuCommentsDO);
		mikuCommentsDOMapper.insertSelective(mikuCommentsDO);	//插入评论
		
		if(!wordCheckFlag){	//如果不包含敏感词，插入评论统计
			MikuCommentsCountDOExample mikuCommentsCountDOExample = new MikuCommentsCountDOExample();
			mikuCommentsCountDOExample.createCriteria().andBuildingIdEqualTo(buildingId).andBuildingTypeEqualTo(buildingType);
			List<MikuCommentsCountDO> mikuCommentsCountDOs = mikuCommentsCountDOMapper.selectByExample(mikuCommentsCountDOExample);
			MikuCommentsCountDO mikuCommentsCountDO = null;
			if(mikuCommentsCountDOs.isEmpty()){	//没有此评论统计记录，插入评论统计
				mikuCommentsCountDO = new MikuCommentsCountDO();
				mikuCommentsCountDO.setBuildingId(buildingId);
				mikuCommentsCountDO.setBuildingType(buildingType);
				mikuCommentsCountDO.setCount(1);
				mikuCommentsCountDO.setHighOpinion(100);
				mikuCommentsCountDO.setStarCount(0);
				mikuCommentsCountDO.setStar2Count(0);
				mikuCommentsCountDO.setStar3Count(0);
				mikuCommentsCountDO.setStar4Count(0);
				mikuCommentsCountDO.setStar5Count(0);
				if(star == 1){
					mikuCommentsCountDO.setStarCount(mikuCommentsCountDO.getStarCount());
				}else if(star == 2){
					mikuCommentsCountDO.setStar2Count(mikuCommentsCountDO.getStar2Count());
				}else if(star == 3){
					mikuCommentsCountDO.setStar3Count(mikuCommentsCountDO.getStar3Count());
				}else if(star == 4){
					mikuCommentsCountDO.setStar4Count(mikuCommentsCountDO.getStar4Count());
				}else{
					mikuCommentsCountDO.setStar5Count(mikuCommentsCountDO.getStar5Count());
				}
				mikuCommentsCountDO.setDateCreated(now);
				mikuCommentsCountDO.setLastUpdated(now);
				mikuCommentsCountDOMapper.insertSelective(mikuCommentsCountDO);//插入评论统计
			}else{	//有此评论统计记录，更新评论统计
				mikuCommentsCountDO = mikuCommentsCountDOs.get(0);
				if(null != mikuCommentsCountDO && null != mikuCommentsCountDO.getId() && mikuCommentsCountDO.getId() > 0){
					int startCount = null == mikuCommentsCountDO.getStarCount() ? 0 : mikuCommentsCountDO.getStarCount();
					int start2Count = null == mikuCommentsCountDO.getStar2Count() ? 0 : mikuCommentsCountDO.getStar2Count();
					int start3Count = null == mikuCommentsCountDO.getStar3Count() ? 0 : mikuCommentsCountDO.getStar3Count();
					int start4Count = null == mikuCommentsCountDO.getStar4Count() ? 0 : mikuCommentsCountDO.getStar4Count();
					int start5Count = null == mikuCommentsCountDO.getStar5Count() ? 0 : mikuCommentsCountDO.getStar5Count();
					int count = null == mikuCommentsCountDO.getCount() ? 0 : mikuCommentsCountDO.getCount();
					
					if(star == 1){
						mikuCommentsCountDO.setStarCount(startCount + 1);
					}else if(star == 2){
						mikuCommentsCountDO.setStar2Count(start2Count + 1);
					}else if(star == 3){
						mikuCommentsCountDO.setStar3Count(start3Count + 1);
					}else if(star == 4){
						mikuCommentsCountDO.setStar4Count(start4Count + 1);
					}else{
						mikuCommentsCountDO.setStar5Count(start5Count + 1);
					}
					mikuCommentsCountDO.setCount(count + 1);
					mikuCommentsCountDO.setHighOpinion(100);	//好评率
					count = startCount + start2Count + start3Count + start4Count + start5Count;
					int goodCount = start4Count + start5Count;	//好评数
					if(count > 0 && goodCount > 0 ){
						mikuCommentsCountDO.setHighOpinion((int)((double)goodCount / (double)count * 100));//好评率
					}
					mikuCommentsCountDO.setLastUpdated(now);
					mikuCommentsCountDOMapper.updateByPrimaryKeySelective(mikuCommentsCountDO);//更新评论统计
				}
			}
		}
		welinkVO.setStatus(1);
		//welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * addComments:(添加评论). <br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param tradeId
	 * @param buildingType 评论类型（如商品=1；众筹=2）
	 * @param commentsJson [{\"buildingId\":1,\"star\":\"5\",\"content\":\"content\",\"picUrls\":\"picUrls\"}]
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/addComments.json", "/api/h/1.0/addComments.json"}, produces = "application/json;charset=utf-8")
	public String addComments(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="buildingType", required = false, defaultValue = "1") byte buildingType,
			@RequestParam(value="tradeId", required = false, defaultValue = "-1") Long tradeId,
			String commentsJson) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		if(buildingType == 1 && tradeId < 1){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~订单不能为空");
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		Date now = new Date();
		if(null == profileDO){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		
		return JSON.toJSONString(mikuCommentsService.addComments(profileDO, commentsJson, tradeId, buildingType));
	}
	
	/**
	 * 
	 * addCommentsReply:(回复评论). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param pics			图片(多张图片以“;”分号分开)
	 * @param replyUserId	被回复用户id
	 * @param commentsId	评论id
	 * @param content		回复内容
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/addCommentsReply.json", "/api/h/1.0/addCommentsReply.json"}, produces = "application/json;charset=utf-8")
	public String addCommentsReply(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="picUrls", required = false, defaultValue = "") String picUrls,
			@RequestParam(value="content", required = false, defaultValue = "") String content,
			Long replyUserId, Long commentsId) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
		ProfileDO replyProfileDO = profileDOMapper.selectByPrimaryKey(replyUserId);	//被回复用户
		Date now = new Date();
		if(null == profileDO){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		//获取敏感词
		Map<String, List<String>> sensitiveWordsMapCache = mikuCommentsService.getSensitiveWordsMapCache();
		boolean wordCheckFlag = false;	//敏感词需要审核Flag
		boolean wordBlacklistFlag = false;	//敏感词黑名单Flag
		//敏感词type 类型(1=需要审核;2=黑名单)
		if(null != sensitiveWordsMapCache){
			List<String> words = sensitiveWordsMapCache.get("1");
			List<String> words2 = sensitiveWordsMapCache.get("2");
			if(null != words && !words2.isEmpty()){	//敏感词type 类型(2=黑名单)
				String hasWordBlack = "";
				for(String word : words2){
					if(content.indexOf(word) > -1){
						hasWordBlack = word;
						wordBlacklistFlag = true;
						break;
					}
				}
				if(wordBlacklistFlag){	//若包含黑名单词语
					welinkVO.setStatus(0);
					welinkVO.setCode(BizErrorEnum.SENSITIVE_WORD.getCode());
					welinkVO.setMsg(BizErrorEnum.SENSITIVE_WORD.getMsg()+"<"+hasWordBlack+">");
					return JSON.toJSONString(welinkVO);
				}
			}
			if(null != words && !words.isEmpty()){	//敏感词type 类型(1=需要审核)
				for(String word : words){
					if(content.indexOf(word) > -1){
						wordCheckFlag = true;
						break;
					}
				}
			}
		}
		/*String picUrls = "";
		// 判断文件是否为空
		if (null != pics && pics.length > 0) {
			for(MultipartFile pic : pics){
				if(null != pic && !pic.isEmpty()){
					picUrls += "";
				}
			}
		}*/
		MikuCommentsReplyDO mikuCommentsReplyDO = new MikuCommentsReplyDO();
		mikuCommentsReplyDO.setCommentsId(commentsId);
		mikuCommentsReplyDO.setUserId(profileId);
		mikuCommentsReplyDO.setUserName(profileDO.getNickname());
		mikuCommentsReplyDO.setUserMobile(profileDO.getMobile());
		if(null != replyProfileDO){
			mikuCommentsReplyDO.setReplyUserId(replyUserId);
			mikuCommentsReplyDO.setReplyUserName(replyProfileDO.getNickname());
			mikuCommentsReplyDO.setReplyUserMobile(replyProfileDO.getMobile());
		}
		mikuCommentsReplyDO.setContent(content);
		mikuCommentsReplyDO.setPicUrls(picUrls);
		mikuCommentsReplyDO.setVersion(0L);
		if(wordCheckFlag){	//包含敏感词
			mikuCommentsReplyDO.setStatus((byte)0);	//Status(0=待审核)
		}else{
			mikuCommentsReplyDO.setStatus((byte)1);	//Status(1=已审核)
		}
		mikuCommentsReplyDO.setDateCreated(now);
		mikuCommentsReplyDO.setLastUpdated(now);
		int insertCommentsReply = mikuCommentsReplyDOMapper.insertSelective(mikuCommentsReplyDO);	//插入评论
		MikuCommentsDO mikuCommentsDO = mikuCommentsDOMapper.selectByPrimaryKey(commentsId);
		if(insertCommentsReply > 0 && null != mikuCommentsDO && mikuCommentsDO.getId() > 0 && !wordCheckFlag){	//更新评论回复数
			int replyCount = null == mikuCommentsDO.getReplyCount() ? 0 : mikuCommentsDO.getReplyCount();
			mikuCommentsDO.setReplyCount(replyCount + 1);
			mikuCommentsDOMapper.updateByPrimaryKey(mikuCommentsDO);
		}
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * commentsList:(评论列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param buildingType
	 * @param buildingId
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/commentsList.json", "/api/h/1.0/commentsList.json"}, produces = "application/json;charset=utf-8")
	public String commentsList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="buildingType", required = false, defaultValue = "1") byte buildingType,
			Long buildingId,
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
		Map resultMap = new HashMap();
		/*profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}*/
		MikuCommentsDOExample mikuCommentsDOExample = new MikuCommentsDOExample();
		mikuCommentsDOExample.createCriteria().andBuildingIdEqualTo(buildingId)
			.andBuildingTypeEqualTo(buildingType).andStatusEqualTo((byte)1).andContentIsNotNull();
		mikuCommentsDOExample.setOffset(startRow);
		mikuCommentsDOExample.setLimit(size);
		mikuCommentsDOExample.setOrderByClause("date_created desc");
		List<MikuCommentsDO> mikuCommentsDOs = mikuCommentsDOMapper.selectByExample(mikuCommentsDOExample);
		
		boolean hasNext = true;
        if (null != mikuCommentsDOs && mikuCommentsDOs.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuCommentsDOs);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * commentsReplyList:(回复评论列表). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param commentsId	评论id
	 * @param pg
	 * @param sz
	 * @return
	 */
	@RequestMapping(value = {"/api/m/1.0/commentsReplyList.json", "/api/h/1.0/commentsReplyList.json"}, produces = "application/json;charset=utf-8")
	public String commentsReplyList(HttpServletRequest request, HttpServletResponse response,
			Long commentsId,
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
		Map resultMap = new HashMap();
		/*profileId = (Long) session.getAttribute("profileId");
		if (null == profileId || profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}*/
		MikuCommentsReplyDOExample mikuCommentsReplyDOExample = new MikuCommentsReplyDOExample();
		mikuCommentsReplyDOExample.createCriteria().andCommentsIdEqualTo(commentsId).andStatusEqualTo((byte)1)
			.andContentIsNotNull();
		mikuCommentsReplyDOExample.setOffset(startRow);
		mikuCommentsReplyDOExample.setLimit(size);
		mikuCommentsReplyDOExample.setOrderByClause("date_created desc");
		List<MikuCommentsReplyDO> mikuCommentsReplyDOList = mikuCommentsReplyDOMapper.selectByExample(mikuCommentsReplyDOExample);
		
		boolean hasNext = true;
        if (null != mikuCommentsReplyDOList && mikuCommentsReplyDOList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        welinkVO.setStatus(1);
        resultMap.put("list", mikuCommentsReplyDOList);
        resultMap.put("hasNext", hasNext);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	@RequestMapping(value = {"/api/m/1.0/getCommentsCount.json", "/api/h/1.0/getCommentsCount.json"}, produces = "application/json;charset=utf-8")
	public String getCommentsCount(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="buildingType", required = false, defaultValue = "1") byte buildingType,
			Long buildingId) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		/*if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}*/
		MikuCommentsCountDOExample mikuCommentsCountDOExample = new MikuCommentsCountDOExample();
		Criteria createCriteria = mikuCommentsCountDOExample.createCriteria();
		createCriteria.andBuildingIdEqualTo(buildingId).andBuildingTypeEqualTo(buildingType);
		List<MikuCommentsCountDO> mikuCommentsCountDOList = mikuCommentsCountDOMapper.selectByExample(mikuCommentsCountDOExample);
		MikuCommentsCountDO mikuCommentsCountDO = null;
		if(null != mikuCommentsCountDOList && !mikuCommentsCountDOList.isEmpty()){
			mikuCommentsCountDO = mikuCommentsCountDOList.get(0);
        }
		welinkVO.setStatus(1);
        resultMap.put("vo", mikuCommentsCountDO);
        welinkVO.setResult(resultMap);
        //return JSON.toJSONString(welinkVO,SerializerFeature.WriteDateUseDateFormat);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
	 * 
	 * addCommentsPic:(评论上传图片). <br/>
	 *
	 * @author LuoGuangChun
	 * @param request
	 * @param response
	 * @param files
	 * @param commentsId
	 * @return
	 */
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/addCommentsPic.json", "/api/h/1.0/addCommentsPic.json"}, produces = "application/json;charset=utf-8")
	public String addCommentsPic(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file", required=true) MultipartFile[] files, // 关键就是这句话起了作用
			//@RequestParam(value="picUrls", required = false, defaultValue = "") String picUrls,
			//@RequestParam(value="content", required = false, defaultValue = "") String content,
			Long commentsId) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		if(null == files || files.length < 1 || "".equals(files)){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~您未上传图片");
			return JSON.toJSONString(welinkVO);
		}
		for(MultipartFile file : files){
			// 判断文件是否为空
			if (!file.isEmpty()) {
				if(!UpYunUtil.isImage(file)){
					//判断是否是图片
					welinkVO.setStatus(0);
					welinkVO.setMsg("啊哦~请选择正确的图片~");
					return JSON.toJSONString(welinkVO);
				}
			}
		}
		MikuCommentsDO mikuCommentsDO = mikuCommentsDOMapper.selectByPrimaryKey(commentsId);
		if(null == mikuCommentsDO){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~你未评价不能添加图片");
			return JSON.toJSONString(welinkVO);
		}else{
			if(!mikuCommentsDO.getUserId().equals(profileId)){
				welinkVO.setStatus(0);
				welinkVO.setMsg("啊哦~此评论不是你的，不能添加图片");
				return JSON.toJSONString(welinkVO);
			}
		}
		String picUrlsPre = null == mikuCommentsDO.getPicUrls() ? "" : mikuCommentsDO.getPicUrls().trim();
		String picUrls = "";
		int countPic = 0;
		for(MultipartFile file : files){
			// 判断文件是否为空
			if (!file.isEmpty()) {
				try {
					String picUrl = "comment"+System.currentTimeMillis()+ commentsId +".jpg";
					String dir = UpYunUtil.COMMENTS_DIR_ROOT;	//上传目录
					// 文件保存路径
					/*String filePath = request.getSession().getServletContext()
							.getRealPath("/")
							+ file.getOriginalFilename();*/
					byte[] bytes = file.getBytes();
					if(UpYunUtil.writePicByMultipartFile(file, dir, picUrl, null)){	//上传
						picUrls += UpYunUtil.UPYUN_URL+dir+picUrl+";";
						countPic++;
					}
					// 转存文件
					//file.transferTo(new File(filePath));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if("".equals(picUrls.trim())){
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
			welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		if(null != picUrlsPre && !"".equals(picUrlsPre.trim())){
			if(!picUrlsPre.endsWith(";")){
				picUrls = picUrlsPre+";"+picUrls;
			}else{
				picUrls = picUrlsPre+picUrls;
			}
		}
		mikuCommentsDO.setPicUrls(picUrls.substring(0, picUrls.length()-1));
		if(mikuCommentsDOMapper.updateByPrimaryKey(mikuCommentsDO) < 1){
			welinkVO.setStatus(0);
			welinkVO.setMsg("啊哦~图片更新失败!");
			return JSON.toJSONString(welinkVO);
		}
		welinkVO.setStatus(1);
		return JSON.toJSONString(welinkVO);
	}
	
	public static void main(String[] args) {
		//[{"id":1,"value":"10","title":"一级"},{"id":2,"value":"20","title":"二级"},{"id":3,"value":"20","title":"三级"}]
		String commentsJson = "[";
		commentsJson += "{\"buildingId\":1,\"buildingType\":\"1\",\"star\":\"5\",\"content\":\"content\",\"picUrls\":\"picUrls\"},";
		commentsJson += "{\"buildingId\":2,\"buildingType\":\"1\",\"star\":\"5\",\"content\":\"content2\",\"picUrls\":\"picUrls2\"},";
		commentsJson += "{\"buildingId\":3,\"buildingType\":\"1\",\"star\":\"5\",\"content\":\"content3\",\"picUrls\":\"picUrls3\"}";
		commentsJson += "]";
		List<MikuCommentsDO> mikuCommentsDOList = JSONArray.parseArray(commentsJson, MikuCommentsDO.class);
		if(null != mikuCommentsDOList && !mikuCommentsDOList.isEmpty()){
			//buildingType  star   content picUrls  buildingId
			//tradeId
			for(MikuCommentsDO commentsDO : mikuCommentsDOList){
				if(null == commentsDO.getBuildingId() || commentsDO.getBuildingId() < 0){
					System.out.println("error");
				}else{
					System.out.println("BuildingId:"+commentsDO.getBuildingId()+"....Content:"+commentsDO.getContent());
				}
			}
		}
	}
	
}

