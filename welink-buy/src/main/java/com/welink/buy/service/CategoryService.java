package com.welink.buy.service;

import com.google.common.base.Function;
import com.google.common.cache.*;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.ParametersStringMaker;
import com.welink.commons.domain.CategoryDO;
import com.welink.commons.domain.CategoryDOExample;
import com.welink.commons.domain.CategoryDOExample.Criteria;
import com.welink.commons.domain.MikuBrandDO;
import com.welink.commons.domain.MikuBrandDOExample;
import com.welink.commons.persistence.CategoryDOMapper;
import com.welink.commons.persistence.MikuBrandDOMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 14-11-18.
 */
@Service
public class CategoryService {

    static Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    @Resource
    private MikuBrandDOMapper mikuBrandDOMapper;
    
    @Resource
    private CategoryDOMapper categoryDOMapper;

    private LoadingCache<String, List<CategoryDO>> categoryCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, List<CategoryDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<CategoryDO>> objectObjectRemovalNotification) {
                    logger.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            /*.build(new CacheLoader<String, List<CategoryDO>>(){
                @Override
                public List<CategoryDO> load(@Nullable String input) {
                	String[] params = StringUtils.split(input, ParametersStringMaker.SEPARATOR);
                	Long cateParentId = null, level = null;
                	if(null != params[0] && !"".equals(params[0].trim()) && StringUtils.isNumeric(params[0])){
                		cateParentId = Long.valueOf(params[0]);
                	}
                	if(params.length <= 2 && null != params[1] && !"".equals(params[1].trim()) && StringUtils.isNumeric(params[1])){
                		level = Long.valueOf(params[1]);
                	}
                	return fetchFreshCates(cateParentId, level);
                    //return fetchFreshCates();
                }
            });*/
            .build(CacheLoader.<String, List<CategoryDO>>from(new Function<String, List<CategoryDO>>() {
                @Override
                public List<CategoryDO> apply(@Nullable String input) {
                	String[] params = StringUtils.split(input, ParametersStringMaker.SEPARATOR);
                	Long cateParentId = null, level = null, brandId = null;
                	if(params.length <= 3){
                		if(null != params[0] && !"".equals(params[0].trim()) && StringUtils.isNumeric(params[0])){
                			cateParentId = Long.valueOf(params[0]);
                		}
                		if(null != params[1] && !"".equals(params[1].trim()) && StringUtils.isNumeric(params[1])){
                			level = Long.valueOf(params[1]);
                		}
                		if(null != params[2] && !"".equals(params[2].trim()) && StringUtils.isNumeric(params[2])){
                			brandId = Long.valueOf(params[2]);
                		}
                	}
                	if(null != brandId && brandId > 0){
        				Map<String,Object> paramMap = new HashMap<String, Object>();
        				if(null != level && level > 0L){
        					paramMap.put("cateLevel", level);
        				}
        				if(null != cateParentId && cateParentId > 0){
        					paramMap.put("cateParentId", cateParentId);
        				}
        				if(null != brandId && brandId > 0){
        					paramMap.put("brandId", brandId);
        				}
        				return mikuBrandDOMapper.getCatesByBrands(paramMap);
        			}else{
        				return fetchFreshCates(cateParentId, level);
        			}
                }
            }));
    
    /**
     * 品牌列表缓存
     */
    private LoadingCache<String, List<MikuBrandDO>> brandsCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, List<MikuBrandDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<MikuBrandDO>> objectObjectRemovalNotification) {
                    logger.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<MikuBrandDO>>from(new Function<String, List<MikuBrandDO>>() {
                @Override
                public List<MikuBrandDO> apply(@Nullable String input) {
                	String[] params = StringUtils.split(input, ParametersStringMaker.SEPARATOR);
                	Long categoryId = null;
                	Integer cateLevel = null;
                	if(null != params[0] && !"".equals(params[0].trim()) && StringUtils.isNumeric(params[0])){
                		categoryId = Long.valueOf(params[0]);
            		}
                	if(null != params[1] && !"".equals(params[1].trim()) && StringUtils.isNumeric(params[1])){
                		cateLevel = Integer.valueOf(params[1]);
            		}
                	
                	Map<String,Object> paramMap = new HashMap<String, Object>();
                	if(null != categoryId && categoryId > -2L){
                		paramMap.put("categoryId", categoryId);
                	}
                	if(null != cateLevel && cateLevel > 0){
                		paramMap.put("cateLevel", cateLevel);
                	}
                	return mikuBrandDOMapper.getBrandsByCates(paramMap);
                	/*MikuBrandDOExample mikuBrandDOExample = new MikuBrandDOExample();
                    mikuBrandDOExample.createCriteria().andIsDeletedEqualTo((byte)0);
                    return mikuBrandDOMapper.selectByExample(mikuBrandDOExample);*/
                }
            }));

    /**
     * 是否有运费，特产不收运费，生鲜配送按totalfee收运费
     *
     * @param cateId
     * @return
     */
    public boolean hasPostFee(long cateId) {
        boolean hasPostFee = true;
        while (true) {
            CategoryDOExample cExample = new CategoryDOExample();
            cExample.createCriteria().andIdEqualTo(cateId).andStatusEqualTo((byte) 1);
            CategoryDO categoryDO = categoryDOMapper.selectByPrimaryKey(cateId);
            if (null != categoryDO && categoryDO.getIsParent() == 0) {
                cateId = categoryDO.getParentId();
            } else {
                if (categoryDO.getIsParent() == 1) {
                    cateId = categoryDO.getId();
                    break;
                } else {
                    break;
                }
            }
        }
        //全国特产不收运费
        if (Long.compare(Constants.AppointmentServiceCategory.NationalSpecialProduct.getCategoryId(), cateId) == 0) {
            hasPostFee = false;
        }
        return hasPostFee;
    }

    /**
     * 获取一级品类，排除全国特产，并排除初装有礼活动类目
     *
     * @return
     */
    public List<CategoryDO> fetchCatesExcludeNative() {
        CategoryDOExample cExample = new CategoryDOExample();
        cExample.createCriteria().andLevelEqualTo(2l).andStatusEqualTo((byte) 1);
        List<CategoryDO> categoryDOs = categoryDOMapper.selectByExample(cExample);
        List<CategoryDO> categoryList = new ArrayList<>();
        if (null != categoryDOs && categoryDOs.size() > 0) {
            for (CategoryDO ct : categoryDOs) {
                if (Long.compare(ct.getId(), Constants.AppointmentServiceCategory.NationalSpecialProduct.getCategoryId()) != 0
                        && Long.compare(ct.getId(), Constants.AppointmentServiceCategory.FirstInstallActive.getCategoryId()) != 0) {
                    categoryList.add(ct);
                }
            }
        }
        return categoryList;
    }

    public List<CategoryDO> fetchFreshCates() {
        CategoryDOExample cExample = new CategoryDOExample();
        //cExample.createCriteria().andParentIdEqualTo(Constants.AppointmentServiceCategory.FRESH_SEA.getCategoryId()).andStatusEqualTo((byte) 1);
        cExample.createCriteria().andStatusEqualTo((byte) 1);
        cExample.setOrderByClause("level DESC, weight DESC");
        List<CategoryDO> categoryDOs = categoryDOMapper.selectByExample(cExample);
        List<CategoryDO> categoryList = new ArrayList<>();
        if (null != categoryDOs && categoryDOs.size() > 0) {
            for (CategoryDO ct : categoryDOs) {
                categoryList.add(ct);
            }
        }
        return categoryList;
    }
    
    public List<CategoryDO> fetchFreshCates(Long cateParentId, Long level) {
        CategoryDOExample cExample = new CategoryDOExample();
        Criteria createCriteria = cExample.createCriteria();
        createCriteria.andStatusEqualTo((byte) 1);
        if(null != cateParentId){
        	createCriteria.andParentIdEqualTo(cateParentId);
        }
        if(null != level && level > 0){
        	createCriteria.andLevelEqualTo(level);
        }else{
        	createCriteria.andLevelNotEqualTo(3L);
        }
        cExample.setOrderByClause("level ASC, weight DESC");
        List<CategoryDO> categoryDOs = categoryDOMapper.selectByExample(cExample);
        List<CategoryDO> categoryList = new ArrayList<>();
        if (null != categoryDOs && categoryDOs.size() > 0) {
            for (CategoryDO ct : categoryDOs) {
                categoryList.add(ct);
            }
        }
        return categoryList;
    }

    public List<CategoryDO> fetchFreshCatesWithCache() {
        //return categoryCache.getUnchecked(ParametersStringMaker.parametersMake(null));
    	try {
    		return categoryCache.getUnchecked(ParametersStringMaker.parametersMake(null,-1,-1));
		} catch (Exception e) {
			return fetchFreshCates(null, null);
		}
    }
    
    public List<CategoryDO> fetchFreshCatesWithCache(Long cateParentId, Long level, Long brandId) {
    	try {
    		return categoryCache.getUnchecked(ParametersStringMaker.parametersMake(cateParentId, level, brandId));
		} catch (Exception e) {
			if(null != brandId && brandId > 0){
				Map<String,Object> paramMap = new HashMap<String, Object>();
				if(null != level && level > 0L){
					paramMap.put("cateLevel", level);
				}
				if(null != cateParentId && cateParentId > 0){
					paramMap.put("cateParentId", cateParentId);
				}
				if(null != brandId && brandId > 0){
					paramMap.put("brandId", brandId);
				}
				return mikuBrandDOMapper.getCatesByBrands(paramMap);
			}else{
				return fetchFreshCates(cateParentId, level);
			}
		}
    	//return fetchFreshCates(cateParentId, level);
    }

    public List<CategoryDO> fetchFreshCateById(long cateId) {
        CategoryDO categoryDO = categoryDOMapper.selectByPrimaryKey(cateId);
        List<CategoryDO> categoryDOs = new ArrayList<>();
        if (null != categoryDO) {
            categoryDOs.add(categoryDO);
        }
        return categoryDOs;
    }
    
    /**
     * 
     * fetchBrands:(获取品牌列表). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @return
     */
    public List<MikuBrandDO> fetchBrands(Long categoryId, Integer cateLevel) {
    	try {
    		return brandsCache.getUnchecked(ParametersStringMaker.parametersMake(categoryId, cateLevel));
		} catch (Exception e) {
		}
    	
    	Map<String,Object> paramMap = new HashMap<String, Object>();
    	if(null != cateLevel && cateLevel > 0L && null != categoryId && categoryId > -2L){
    		paramMap.put("cateLevel", cateLevel);
    		paramMap.put("categoryId", categoryId);
    	}
    	return mikuBrandDOMapper.getBrandsByCates(paramMap);
    	/*MikuBrandDOExample mikuBrandDOExample = new MikuBrandDOExample();
        mikuBrandDOExample.createCriteria().andIsDeletedEqualTo((byte)0);
        return mikuBrandDOMapper.selectByExample(mikuBrandDOExample);*/
    }
    
}
