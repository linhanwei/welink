/**
 * Project Name:welink-biz
 * File Name:UserAgencyService.java
 * Package Name:com.welink.biz.service
 * Date:2015年11月1日上午11:09:27
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
 */

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.jdbc.Connection;
import com.welink.biz.common.cache.CheckNOGenerator;
import com.welink.biz.common.constants.ProfileStatusEnum;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuMobileAreaDO;
import com.welink.commons.domain.MikuMobileAreaDOExample;
import com.welink.commons.domain.MikuUserAgencyDO;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.MikuWalletDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuMobileAreaDOMapper;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.MikuWalletDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;

/**
 * ClassName:UserAgencyService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015年11月1日 上午11:09:27 <br/>
 * 
 * @author LuoGuangChun
 * @version
 * @since JDK 1.6
 * @see
 */
@Service
public class MikuUserAgencyService implements InitializingBean {

	static Logger log = LoggerFactory.getLogger(MikuUserAgencyService.class);

	@Resource
	private ProfileDOMapper profileDOMapper;

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Resource
	private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
	
	@Resource
	private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
    private MikuMobileAreaDOMapper mikuMobileAreaDOMapper;
	
	@Resource
    private MikuWalletDOMapper mikuWalletDOMapper;

	/**
	 * 
	 * addProfile:(注册用户和建立代理关系,根据电话号码归宿地建立代理关系). <br/>
	 *
	 * @author LuoGuangChun
	 * @param buildingId
	 * @param mobileNum
	 * @param storedPassword
	 * @param deplom
	 * @param parentUserId
	 * @return
	 */
	public ProfileDO addProfileAndAgencyByMobileArea(long buildingId, final String mobileNum,
			String storedPassword, byte deplom, final String parentUserId, Byte isAgency) {
		final ProfileDO profileDO = new ProfileDO();
		profileDO.setDateCreated(new Date());
		profileDO.setLastUpdated(new Date());
		profileDO.setMobile(mobileNum);
		profileDO.setStatus(ProfileStatusEnum.valid.getCode());
		profileDO.setInstalledApp((byte) 1);
		profileDO.setLastCommunity(buildingId);
		profileDO.setPassword(storedPassword);
		profileDO.setDiploma(deplom);
		String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
		if(mobileNum.length() > 4){
			profileDO.setNickname(mobileNum.substring(mobileNum.length()-4, mobileNum.length())+checkCode);
		}else{
			profileDO.setNickname(CheckNOGenerator.getFixLenthString(6));
		}
		profileDO.setIsAgency(isAgency);
		
		int updateprofile = transactionTemplate
				.execute(new TransactionCallback<Integer>() {
					@Override
					public Integer doInTransaction(
							TransactionStatus transactionStatus) {
						ProfileDO parentProfileDO = null;//上级用户
						if (null != parentUserId && !"".equals(parentUserId.trim())) {
							parentProfileDO = profileDOMapper.selectByPrimaryKey(Long.valueOf(parentUserId));	//上级用户
						}
						
						String tempParentUserId = "0";
						//查询电话号码所属地域
						MikuMobileAreaDOExample mikuMobileAreaDOExample = new MikuMobileAreaDOExample();
						mikuMobileAreaDOExample.createCriteria().andMobileEqualTo(mobileNum.substring(0, 7));
						List<MikuMobileAreaDO> mikuMobileAreaDOList = mikuMobileAreaDOMapper.selectByExample(mikuMobileAreaDOExample);
						if (!mikuMobileAreaDOList.isEmpty()) {
							MikuMobileAreaDO mikuMobileAreaDO = mikuMobileAreaDOList.get(0);
							String city = mikuMobileAreaDOList.get(0).getCity();	//电话号码所属城市
							if(null != parentProfileDO && city.equals(parentProfileDO.getCity())){	
								//当上级代理的地区与当前电话所属地区相等时
								tempParentUserId = String.valueOf(parentProfileDO.getId());
							}else{	//没有上级代理时,或当上级代理的地区与当前电话所属地区不相等时
								ProfileDOExample profileDOExampleArea = new ProfileDOExample();
								profileDOExampleArea.createCriteria().andCityEqualTo(city).andIsAgencyEqualTo((byte)1);
								/*profileDOExampleArea.setLimit(1);
		        				profileDOExampleArea.setOffset(0);*/
								int countProfile = profileDOMapper.countByExample(profileDOExampleArea);
								Random rand = new Random();
								if(countProfile > 0){
									int randNumAll = rand.nextInt(countProfile);
									profileDOExampleArea.setLimit(5);
				    				profileDOExampleArea.setOffset(randNumAll);
									List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExampleArea);	//查询电话号码所属区域的所有代理
									if(!profiles.isEmpty() && profiles.get(0).getCity().equals(city)){
										//int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;	//获取特定区间的随机数
										int randNum = rand.nextInt(profiles.size()-1 - 0 + 1) + 0;
										ProfileDO profileDORand = profiles.get(randNum);
										if(null != profileDORand){
											tempParentUserId = String.valueOf(profileDORand.getId());		//设置随机上级代理
										}
									}
								}
							}
							profileDO.setProvince(mikuMobileAreaDO.getProvince());
							profileDO.setCity(city);
							profileDO.setCorp(mikuMobileAreaDO.getCorp());
							profileDO.setPostcode(mikuMobileAreaDO.getPostcode());
							profileDO.setAreacode(mikuMobileAreaDO.getAreacode());
						}else{	//当根据电话号码未查到所属区域时，设为未知
							ProfileDOExample profileDOExampleArea = new ProfileDOExample();
							profileDOExampleArea.createCriteria().andCityEqualTo("未知").andIsAgencyEqualTo((byte)1);
							/*profileDOExampleArea.setLimit(1);
	        				profileDOExampleArea.setOffset(0);*/
							int countProfile = profileDOMapper.countByExample(profileDOExampleArea);
							Random rand = new Random();
							if(countProfile > 0){
								int randNumAll = rand.nextInt(countProfile);
								profileDOExampleArea.setLimit(5);
			    				profileDOExampleArea.setOffset(randNumAll);
								List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExampleArea);	//查询电话号码所属区域的所有代理
								if(!profiles.isEmpty()){
									//int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;	//获取特定区间的随机数
									int randNum = rand.nextInt(profiles.size()-1 - 0 + 1) + 0;
									ProfileDO profileDORand = profiles.get(randNum);
									if(null != profileDORand){
										tempParentUserId = String.valueOf(profileDORand.getId());		//设置随机上级代理
									}
								}
							}
							profileDO.setProvince("未知");
							profileDO.setCity("未知");
							profileDO.setCorp("未知");
							profileDO.setPostcode("未知");
							profileDO.setAreacode("未知");
						}
						
						int insertNum = profileDOMapper.insertSelective(profileDO); // 注册用户
						if(insertNum < 1){
							log.error("注册用户数据失败. update profile failed");
							transactionStatus.setRollbackOnly();
							return insertNum;
						}
						
						insertNum = mikuAgencyShareAccountDOMapper.insertSelective(
								setMikuAgencyShareAccountDO(profileDO.getId()));		//插入代理用户帐号
						if(insertNum < 1){
							log.error("注册插入代理用户帐号数据失败. update mikuAgencyShareAccount failed");
							transactionStatus.setRollbackOnly();
							return insertNum;
						}
						int insertMikuUserAgency = 0;
						if (null != tempParentUserId && !"".equals(tempParentUserId.trim())) {
							insertMikuUserAgency = insertMikuUserAgency(
									Long.valueOf(tempParentUserId), profileDO.getId(), null); // 设置用户代理关系
						}else{
							insertMikuUserAgency = insertMikuUserAgency(
									0L, profileDO.getId(), null); // 设置用户代理关系
						}
						if (insertMikuUserAgency <= 0) {
							log.error("注册设置用户代理关系数据失败. update MikuUserAgency failed");
							transactionStatus.setRollbackOnly();
							return insertMikuUserAgency;
						}
						
						insertNum = mikuWalletDOMapper.insertSelective(
								setMikuWalletDO(profileDO));		//插入我的钱包
						if(insertNum < 1){
							log.error("注册插入用户钱包数据失败. update MikuWallet failed");
							transactionStatus.setRollbackOnly();
							return insertNum;
						}
						
						return insertNum;
					}

				});

		if (updateprofile <= 0) {
			log.error("注册过程更改数据失败. update profile failed. mobile:" + mobileNum);
			return null;
		}
		return profileDO;
	}
	
	/**
	 * 
	 * addProfileAndAgency:(注册用户和建立代理关系,根据邀请人建立). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param buildingId
	 * @param mobileNum
	 * @param storedPassword
	 * @param deplom
	 * @param parentUserId
	 * @param isAgency
	 * @return
	 */
	public ProfileDO addProfileAndAgency(long buildingId, final String mobileNum,
			String storedPassword, byte deplom, final String parentUserId, Byte isAgency) {
		final ProfileDO profileDO = new ProfileDO();
		profileDO.setDateCreated(new Date());
		profileDO.setLastUpdated(new Date());
		profileDO.setMobile(mobileNum);
		profileDO.setStatus(ProfileStatusEnum.valid.getCode());
		profileDO.setInstalledApp((byte) 1);
		profileDO.setLastCommunity(buildingId);
		profileDO.setPassword(storedPassword);
		profileDO.setDiploma(deplom);
		String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
		if(mobileNum.length() > 4){
			profileDO.setNickname(mobileNum.substring(mobileNum.length()-4, mobileNum.length())+checkCode);
		}else{
			profileDO.setNickname(CheckNOGenerator.getFixLenthString(6));
		}
		profileDO.setIsAgency(isAgency);
		profileDO.setIsExpert((byte)0);
		
		int updateprofile = transactionTemplate
				.execute(new TransactionCallback<Integer>() {
					@Override
					public Integer doInTransaction(
							TransactionStatus transactionStatus) {
						ProfileDO parentProfileDO = null;//上级用户
						if (null != parentUserId && !"".equals(parentUserId.trim())) {
							parentProfileDO = profileDOMapper.selectByPrimaryKey(Long.valueOf(parentUserId));	//上级用户
						}
						
						String tempParentUserId = "0";
						if(null != parentProfileDO && parentProfileDO.getIsAgency().equals((byte)1)){	
							//当有上级，并上级是代理
							tempParentUserId = String.valueOf(parentProfileDO.getId());
						}else{	//没有上级代理时,或当上级代理的地区与当前电话所属地区不相等时
							/*ProfileDOExample profileDOExampleArea = new ProfileDOExample();
							profileDOExampleArea.createCriteria().andIsAgencyEqualTo((byte)1);
							int countProfile = profileDOMapper.countByExample(profileDOExampleArea);
							Random rand = new Random();
							if(countProfile > 0){
								int randNumAll = rand.nextInt(countProfile);
								profileDOExampleArea.setLimit(5);
			    				profileDOExampleArea.setOffset(randNumAll);
								List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExampleArea);	//查询电话号码所属区域的所有代理
								if(!profiles.isEmpty()){
									//int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;	//获取特定区间的随机数
									int randNum = rand.nextInt(profiles.size()-1 - 0 + 1) + 0;
									ProfileDO profileDORand = profiles.get(randNum);
									if(null != profileDORand){
										tempParentUserId = String.valueOf(profileDORand.getId());		//设置随机上级代理
									}
								}
							}else{
								tempParentUserId = "0";
							}*/
						}
						
						int insertNum = profileDOMapper.insertSelective(profileDO); // 注册用户
						if(insertNum < 1){
							log.error("注册用户数据失败. update profile failed");
							transactionStatus.setRollbackOnly();
							return insertNum;
						}
						
						insertNum = mikuAgencyShareAccountDOMapper.insertSelective(
								setMikuAgencyShareAccountDO(profileDO.getId()));		//插入代理用户帐号
						if(insertNum < 1){
							log.error("注册插入代理用户帐号数据失败. update mikuAgencyShareAccount failed");
							transactionStatus.setRollbackOnly();
							return insertNum;
						}
						int insertMikuUserAgency = 0;
						if (null != tempParentUserId && !"".equals(tempParentUserId.trim())
								&& !"0".equals(tempParentUserId.trim())) {
							insertMikuUserAgency = insertMikuUserAgency(
									Long.valueOf(tempParentUserId), profileDO.getId(), transactionStatus); // 设置用户代理关系
						}else{
							insertMikuUserAgency = 1;
							/*insertMikuUserAgency = insertMikuUserAgency(
									0L, profileDO.getId(), transactionStatus);*/ // 设置用户代理关系
						}
						if (insertMikuUserAgency <= 0) {
							log.error("注册设置用户代理关系数据失败. update MikuUserAgency failed");
							transactionStatus.setRollbackOnly();
							return insertMikuUserAgency;
						}
						
						insertNum = mikuWalletDOMapper.insertSelective(
								setMikuWalletDO(profileDO));		//插入我的钱包
						if(insertNum < 1){
							log.error("注册插入用户钱包数据失败. update MikuWallet failed");
							transactionStatus.setRollbackOnly();
							return insertNum;
						}
						
						return insertNum;
					}

				});

		if (updateprofile <= 0) {
			log.error("注册过程更改数据失败. update profile failed. mobile:" + mobileNum);
			return null;
		}
		return profileDO;
	}
	
	//初始化钱包用于注册时
	public MikuWalletDO setMikuWalletDO(ProfileDO profileDO) {
		Date nowDate = new Date();
		MikuWalletDO mikuWalletDO = new MikuWalletDO();
		mikuWalletDO.setBalanceFee(0L);
		mikuWalletDO.setGetpayedFee(0L);
		mikuWalletDO.setGetpayingFee(0L);
		mikuWalletDO.setMobile(profileDO.getMobile());
		mikuWalletDO.setUserId(profileDO.getId());
		mikuWalletDO.setVersion(0L);
		mikuWalletDO.setDateCreated(nowDate);
		mikuWalletDO.setLastUpdated(nowDate);
		return mikuWalletDO;
	}

	private MikuAgencyShareAccountDO setMikuAgencyShareAccountDO(Long id) {
		MikuAgencyShareAccountDO mikuAgencyShareAccountDO = new MikuAgencyShareAccountDO();
		Date now = new Date();
		mikuAgencyShareAccountDO.setVersion(0L);
		mikuAgencyShareAccountDO.setAgencyId(id);
		mikuAgencyShareAccountDO.setTotalShareFee(0L);
		mikuAgencyShareAccountDO.setTotalGotpayFee(0L);
		mikuAgencyShareAccountDO.setDirectSalesFee(0L);
		mikuAgencyShareAccountDO.setDirectShareFee(0L);
		mikuAgencyShareAccountDO.setGetpayingFee(0L);
		mikuAgencyShareAccountDO.setIndirectSalesFee(0L);
		mikuAgencyShareAccountDO.setIndirectShareFee(0L);
		mikuAgencyShareAccountDO.setNoGetpayFee(0L);
		mikuAgencyShareAccountDO.setpOfferFee(0L);
		mikuAgencyShareAccountDO.setP2OfferFee(0L);
		mikuAgencyShareAccountDO.setP3OfferFee(0L);
		mikuAgencyShareAccountDO.setP4OfferFee(0L);
		mikuAgencyShareAccountDO.setP5OfferFee(0L);
		mikuAgencyShareAccountDO.setpSalesFee(0L);
		mikuAgencyShareAccountDO.setP2SalesFee(0L);
		mikuAgencyShareAccountDO.setP3SalesFee(0L);
		mikuAgencyShareAccountDO.setP4SalesFee(0L);
		mikuAgencyShareAccountDO.setP5SalesFee(0L);
		mikuAgencyShareAccountDO.setpTradesCount(0L);
		mikuAgencyShareAccountDO.setP2TradesCount(0L);
		mikuAgencyShareAccountDO.setP3TradesCount(0L);
		mikuAgencyShareAccountDO.setP4TradesCount(0L);
		mikuAgencyShareAccountDO.setP5TradesCount(0L);
		mikuAgencyShareAccountDO.setDateCreated(now);
		mikuAgencyShareAccountDO.setLastUpdated(now);
		mikuAgencyShareAccountDO.setVersion(0L);
		return mikuAgencyShareAccountDO;
	}

	// 设置代理关系
	public int insertMikuUserAgency(Long parentUserId, Long userId, TransactionStatus transactionStatus) {
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(userId);
		boolean isAgency = false;
		if(null != profileDO && profileDO.getIsAgency().equals((byte)1)){
			isAgency = true;
		}
		MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
    	mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(userId);
    	//查询代理关系
    	List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);	
    	if(!mikuUserAgencyDOList.isEmpty()){	//建立过代理关系
    		return 1;
    	}
		
		
		MikuUserAgencyDO mikuUserAgencyDO = new MikuUserAgencyDO();
		Long agencyLevel = 0L;
		MikuUserAgencyDO pidMikuUserAgencyDO = null;	//上级代理用户关系
		if(null != parentUserId && parentUserId > 0){
			ProfileDO profileDOParent = profileDOMapper.selectByPrimaryKey(parentUserId);
			if(null != profileDOParent && profileDOParent.getIsAgency().equals((byte)1)){
				MikuUserAgencyDOExample mikuUserAgencyDOExample2 = new MikuUserAgencyDOExample();
				mikuUserAgencyDOExample2.createCriteria().andUserIdEqualTo(parentUserId);
				//查询代理关系
				List<MikuUserAgencyDO> mikuUserAgencyDOList2 = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample2);
				if(null != mikuUserAgencyDOList2 && !mikuUserAgencyDOList2.isEmpty()){
					pidMikuUserAgencyDO = mikuUserAgencyDOList2.get(0);
				}
			}
		}
		if(null == pidMikuUserAgencyDO){
			pidMikuUserAgencyDO = mikuUserAgencyDOMapper.selectByPrimaryKey(1076L);//myron代理用户关系
		}
		if(null == pidMikuUserAgencyDO){
			pidMikuUserAgencyDO = new MikuUserAgencyDO();
		}
		//if (null != parentUserId && parentUserId > 0) {
		if(null != pidMikuUserAgencyDO){
			/*MikuUserAgencyDOExample example = new MikuUserAgencyDOExample();
			example.createCriteria().andUserIdEqualTo(parentUserId);
			List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper
					.selectByExample(example);
			if (!mikuUserAgencyDOList.isEmpty()) {
				MikuUserAgencyDO pidMikuUserAgencyDO = mikuUserAgencyDOList
						.get(0);*/
				mikuUserAgencyDO.setpUserId(pidMikuUserAgencyDO.getUserId());
				mikuUserAgencyDO.setP2UserId(pidMikuUserAgencyDO.getpUserId());
				mikuUserAgencyDO.setP3UserId(pidMikuUserAgencyDO.getP2UserId());
				mikuUserAgencyDO.setP4UserId(pidMikuUserAgencyDO.getP3UserId());
				mikuUserAgencyDO.setP5UserId(pidMikuUserAgencyDO.getP4UserId());
				mikuUserAgencyDO.setP6UserId(pidMikuUserAgencyDO.getP5UserId());
				mikuUserAgencyDO.setP7UserId(pidMikuUserAgencyDO.getP6UserId());
				mikuUserAgencyDO.setP8UserId(pidMikuUserAgencyDO.getP7UserId());
				mikuUserAgencyDO.setCeoUserId(pidMikuUserAgencyDO.getCeoUserId());
				mikuUserAgencyDO.setCeo2UserId(pidMikuUserAgencyDO.getCeo2UserId());
				mikuUserAgencyDO.setCeo3UserId(pidMikuUserAgencyDO.getCeo3UserId());
				mikuUserAgencyDO.setCeo4UserId(pidMikuUserAgencyDO.getCeo4UserId());
				if(isAgency){
					mikuUserAgencyDO.setLevelId(Long.valueOf(BizConstants.MikuAgencyLevel.CEO5.getId()));	//推广经理
				}else{
					mikuUserAgencyDO.setLevelId(Long.valueOf(BizConstants.MikuAgencyLevel.USER.getId()));	//普通用户
				}
				String path = pidMikuUserAgencyDO.getPath();
				/*
				 * String path = pidMikuUserAgencyDO.getPath(); if(null != path
				 * && !"".equals(path.trim())){ path += ","+parentUserId; }else{
				 * path = parentUserId.toString(); }
				 * mikuUserAgencyDO.setPath(path);
				 */
				agencyLevel = (null == pidMikuUserAgencyDO.getAgencyLevel() ? 0L
						: pidMikuUserAgencyDO.getAgencyLevel()) + 1L;
			//}
		}
		Date nowDate = new Date();
		mikuUserAgencyDO.setAgencyLevel(agencyLevel);
		mikuUserAgencyDO.setIsParent(Byte.valueOf("0"));
		mikuUserAgencyDO.setUserId(userId);
		mikuUserAgencyDO.setVersion(0L);
		mikuUserAgencyDO.setIsValidated(Byte.valueOf("1"));
		mikuUserAgencyDO.setIsDeleted(Byte.valueOf("0"));
		mikuUserAgencyDO.setDateCreated(nowDate);
		mikuUserAgencyDO.setLastUpdated(nowDate);
		mikuUserAgencyDO.setVersion(0L);
		int i = mikuUserAgencyDOMapper.insertSelective(mikuUserAgencyDO);
		if(i < 1 && null != transactionStatus){
			transactionStatus.setRollbackOnly();
		}
		//return mikuUserAgencyDOMapper.insertSelective(mikuUserAgencyDO);
		return i;
	}
	
	// 更新代理关系
	public int updateMikuUserAgency(Long parentUserId, Long userId, TransactionStatus transactionStatus) {
		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(userId);
		boolean isAgency = false;
		boolean hasUserAgency = false; //是否已建立过代理关系(true=已建立代理关系;false=未建立代理关系)
		Long hasUserAgency_userId = -1L;	//如果有建立代理关系的上级id
		Long agencyId = -1L;		//用户代理id
		if(null != profileDO && profileDO.getIsAgency().equals((byte)1)){
			isAgency = true;
		}
		MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
    	mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(userId);
    	//查询代理关系
    	List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);	
    	if(!mikuUserAgencyDOList.isEmpty()){	
    		hasUserAgency = true;	//建立过代理关系
    		MikuUserAgencyDO mikuUserAgencyDO = mikuUserAgencyDOList.get(0);
    		if(null != mikuUserAgencyDO){
    			agencyId = mikuUserAgencyDO.getId();
    			if(null != mikuUserAgencyDO.getpUserId()){
    				hasUserAgency_userId = mikuUserAgencyDO.getpUserId();
    			}
    		}
    		//return 1;
    	}
		
    	if((null == parentUserId || (null != parentUserId && parentUserId < 1L)) && hasUserAgency && hasUserAgency_userId > 0L){
    		//如果有建立代理关系,并且没有传上级id过来，则用原来的代理关系
    		parentUserId = hasUserAgency_userId;
		}
		
		MikuUserAgencyDO mikuUserAgencyDO = new MikuUserAgencyDO();
		Long agencyLevel = 0L;
		MikuUserAgencyDO pidMikuUserAgencyDO = null;	//上级代理用户关系
		if(null != parentUserId && parentUserId > 0){
			ProfileDO profileDOParent = profileDOMapper.selectByPrimaryKey(parentUserId);
			if(null != profileDOParent && profileDOParent.getIsAgency().equals((byte)1)){
				MikuUserAgencyDOExample mikuUserAgencyDOExample2 = new MikuUserAgencyDOExample();
				mikuUserAgencyDOExample2.createCriteria().andUserIdEqualTo(parentUserId);
				//查询代理关系
				List<MikuUserAgencyDO> mikuUserAgencyDOList2 = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample2);
				if(null != mikuUserAgencyDOList2 && !mikuUserAgencyDOList2.isEmpty()){
					pidMikuUserAgencyDO = mikuUserAgencyDOList2.get(0);
				}
			}
		}
		if(null == pidMikuUserAgencyDO){
			pidMikuUserAgencyDO = mikuUserAgencyDOMapper.selectByPrimaryKey(1076L);//myron代理用户关系
		}
		if(null == pidMikuUserAgencyDO){
			pidMikuUserAgencyDO = new MikuUserAgencyDO();
		}
		//if (null != parentUserId && parentUserId > 0) {
		if(null != pidMikuUserAgencyDO){
			/*MikuUserAgencyDOExample example = new MikuUserAgencyDOExample();
			example.createCriteria().andUserIdEqualTo(parentUserId);
			List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper
					.selectByExample(example);
			if (!mikuUserAgencyDOList.isEmpty()) {
				MikuUserAgencyDO pidMikuUserAgencyDO = mikuUserAgencyDOList
						.get(0);*/
				mikuUserAgencyDO.setpUserId(pidMikuUserAgencyDO.getUserId());
				mikuUserAgencyDO.setP2UserId(pidMikuUserAgencyDO.getpUserId());
				mikuUserAgencyDO.setP3UserId(pidMikuUserAgencyDO.getP2UserId());
				mikuUserAgencyDO.setP4UserId(pidMikuUserAgencyDO.getP3UserId());
				mikuUserAgencyDO.setP5UserId(pidMikuUserAgencyDO.getP4UserId());
				mikuUserAgencyDO.setP6UserId(pidMikuUserAgencyDO.getP5UserId());
				mikuUserAgencyDO.setP7UserId(pidMikuUserAgencyDO.getP6UserId());
				mikuUserAgencyDO.setP8UserId(pidMikuUserAgencyDO.getP7UserId());
				mikuUserAgencyDO.setCeoUserId(pidMikuUserAgencyDO.getCeoUserId());
				mikuUserAgencyDO.setCeo2UserId(pidMikuUserAgencyDO.getCeo2UserId());
				mikuUserAgencyDO.setCeo3UserId(pidMikuUserAgencyDO.getCeo3UserId());
				mikuUserAgencyDO.setCeo4UserId(pidMikuUserAgencyDO.getCeo4UserId());
				if(isAgency){
					mikuUserAgencyDO.setLevelId(Long.valueOf(BizConstants.MikuAgencyLevel.CEO5.getId()));	//推广经理
				}else{
					mikuUserAgencyDO.setLevelId(Long.valueOf(BizConstants.MikuAgencyLevel.USER.getId()));	//普通用户
				}
				String path = pidMikuUserAgencyDO.getPath();
				/*
				 * String path = pidMikuUserAgencyDO.getPath(); if(null != path
				 * && !"".equals(path.trim())){ path += ","+parentUserId; }else{
				 * path = parentUserId.toString(); }
				 * mikuUserAgencyDO.setPath(path);
				 */
				agencyLevel = (null == pidMikuUserAgencyDO.getAgencyLevel() ? 0L
						: pidMikuUserAgencyDO.getAgencyLevel()) + 1L;
			//}
		}
		Date nowDate = new Date();
		mikuUserAgencyDO.setAgencyLevel(agencyLevel);
		mikuUserAgencyDO.setIsParent(Byte.valueOf("0"));
		mikuUserAgencyDO.setUserId(userId);
		mikuUserAgencyDO.setVersion(0L);
		mikuUserAgencyDO.setIsValidated(Byte.valueOf("1"));
		mikuUserAgencyDO.setIsDeleted(Byte.valueOf("0"));
		//mikuUserAgencyDO.setDateCreated(nowDate);
		mikuUserAgencyDO.setLastUpdated(nowDate);
		mikuUserAgencyDO.setVersion(0L);
		int i = 0;
		if(hasUserAgency && null != agencyId && agencyId > 0L){
			mikuUserAgencyDO.setId(agencyId);
			i = mikuUserAgencyDOMapper.updateByPrimaryKeySelective(mikuUserAgencyDO);
		}else{
			mikuUserAgencyDO.setDateCreated(nowDate);
			i = mikuUserAgencyDOMapper.insertSelective(mikuUserAgencyDO);
		}
		if(i < 1 && null != transactionStatus){
			transactionStatus.setRollbackOnly();
		}
		//return mikuUserAgencyDOMapper.insertSelective(mikuUserAgencyDO);
		return i;
	}
	
	/**
	 * 
	 * getParentProfileByProfileId:(通过profileId查找父级用户). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param profileId
	 */
	public ProfileDO getParentProfileByProfileId(Long profileId){
		if(null != profileId){
			MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
			mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
			List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);
			if(null != mikuUserAgencyDOList && !mikuUserAgencyDOList.isEmpty()){
				MikuUserAgencyDO mikuUserAgencyDO = mikuUserAgencyDOList.get(0);
				if(null != mikuUserAgencyDO && null != mikuUserAgencyDO.getpUserId() && mikuUserAgencyDO.getpUserId() > 0){
					ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(mikuUserAgencyDO.getpUserId());
					if(null != profileDO){
						return profileDO;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		checkNotNull(profileDOMapper);
		checkNotNull(transactionManager);

		transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setName("userAgentcy-transaction");
		transactionTemplate
				.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate
				.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
	}

	public static void main(String[] args) {
		String s = "爱新觉罗是谁啊爱何为爱为什么呢爱新觉罗";

		String t = "爱";
		/*for (int i = 0; i < s.length(); i++) {
			int a = s.indexOf(t, i);
			if (a < 0) {
				break;
			}
			System.out.println(a);
			i = a;
		}*/
		String sss = "0123456789";
		sss = sss.substring(sss.length()-4, sss.length());
		System.out.println(CheckNOGenerator.getFixLenthString(6));
	}

}
