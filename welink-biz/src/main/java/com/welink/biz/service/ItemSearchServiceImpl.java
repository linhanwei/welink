package com.welink.biz.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.opensearch.CloudsearchClient;
import com.aliyun.opensearch.CloudsearchSearch;
import com.aliyun.opensearch.CloudsearchSuggest;
import com.aliyun.opensearch.object.KeyTypeEnum;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
/*import com.opensearch.javasdk.CloudsearchClient;
import com.opensearch.javasdk.CloudsearchSearch;
import com.opensearch.javasdk.object.KeyTypeEnum;*/
import com.welink.biz.common.model.SearchResult;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.persistence.ItemMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.welink.biz.common.constants.BizErrorEnum.SYSTEM_BUSY;

/**
 * Created by saarixx on 28/11/14.
 */
@Service(value = "itemSearchService")
public class ItemSearchServiceImpl implements ItemSearchService {

    static Logger logger = LoggerFactory.getLogger(ItemSearchServiceImpl.class);

    @Resource
    private Env env;

    @Resource
    private ItemMapper itemMapper;

    private String indexName;

    public SearchResult<Item> defaultSearch(Long shopId, String query, Long categoryId, Long brandId, int offset, int limit, OpenSearchType type, List<Long> tags, boolean isOperatorAnd) {
        Preconditions.checkNotNull(shopId);

        List<Item> itemList = Lists.newArrayListWithCapacity(limit);

        CloudsearchSearch cloudsearchSearch = new CloudsearchSearch(getCloudsearchClient());
        cloudsearchSearch.addIndex(getIndexName());

        if (categoryId != null) {
            /*cloudsearchSearch.addFilter("category_id=" + categoryId + " AND status=1 AND shop_id=" + shopId + 
            		(null == brandId ? "" : " AND brand_id="+brandId) + " AND (type=1 OR type=2) " );*/
        	cloudsearchSearch.addFilter("category_id=" + categoryId + " AND status=1 AND shop_id>999" +
            		(null == brandId ? "" : " AND brand_id="+brandId) + " AND type=1 " );
        } else {
            /*cloudsearchSearch.addFilter("status=1 AND shop_id=" + shopId +
            		(null == brandId ? "" : " AND brand_id="+brandId));*/
        	//cloudsearchSearch.addFilter("status=1 AND shop_id>999" +  " AND (type=1 OR type=2) " +
        	cloudsearchSearch.addFilter("status=1 AND shop_id>999" +  " AND type=1 " +
            		(null == brandId ? "" : " AND brand_id="+brandId));
        }

        if (StringUtils.isNoneBlank(query)) {
            cloudsearchSearch.setQueryString("title:'" + query + "'" + " OR key_word:'"+ query + "'");
        }

        if (type != null) {
            cloudsearchSearch.addSort(type.getFormulaName().substring(1), type.getFormulaName().substring(0, 1));
        }

        if (tags != null && !tags.isEmpty()) {
            if (isOperatorAnd) {
                Preconditions.checkArgument(tags.size() <= 9, "the size of tags should be less than 9...");
            } else {
                Preconditions.checkArgument(tags.size() <= 10, "the size of tags should be less than 10...");
            }

            StringBuilder code = new StringBuilder("(tag_id");

            for (Long id : tags) {
                if (isOperatorAnd) {
                    code.append(" &").append(id);
                } else {
                    code.append(" |").append(id);
                }
            }

            code.append(") > 0");

            cloudsearchSearch.addFilter(code.toString());
        }
        cloudsearchSearch.setFormat("json");
        cloudsearchSearch.setStartHit(offset);
        cloudsearchSearch.setHits(limit);


        String search;

        SearchResult<Item> itemSearchResult = new SearchResult<Item>();

        try {
            search = cloudsearchSearch.search();

            if (env.isDev()) {
                logger.info(cloudsearchSearch.getDebugInfo());
            }

            JSONObject jsonObject = JSON.parseObject(search);
            if ("ok".equalsIgnoreCase(String.valueOf(jsonObject.get("status")))) { // 如果是 OK 的
                JSONObject result = jsonObject.getJSONObject("result");

                float searchTime = result.getFloatValue("searchtime");
                int total = result.getIntValue("total");
                int num = result.getIntValue("num");
                int viewTotal = result.getIntValue("viewtotal");

                SearchResult.Result<Item> ret = new SearchResult.Result<Item>();
                ret.setSearchTime(searchTime);
                ret.setTotal(total);
                ret.setNum(num);
                ret.setViewTotal(viewTotal);

                JSONArray array = result.getJSONArray("items");

                int min = Math.min(array.size(), limit);

                for (int i = 0; i < min; i++) {
                    long itemId = array.getJSONObject(i).getLongValue("id");
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    //搜索，过滤掉团购商品
                    if (null != item && item.getType() != null && item.getType() != 3) {
                        ret.getResultList().add(item);
                    }
                }

                itemSearchResult.setResult(ret);
                itemSearchResult.setSuccess(true);

                return itemSearchResult;
            } else {
                logger.error(JSON.toJSONString(jsonObject, true));
                JSONArray errors = jsonObject.getJSONArray("errors");
                return SearchResult.<Item>failure(errors.getJSONObject(0).getString("code"), errors.getJSONObject(0).getString("message"));
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return SearchResult.<Item>failure(String.valueOf(SYSTEM_BUSY.getCode()), SYSTEM_BUSY.getMsg());
        } finally {
            cloudsearchSearch.clear();
        }
    }
    
    //提示下拉框
    public Map<String, Object> cloudsearchSuggest(String query){
        String suggestName = "title";
        CloudsearchSuggest suggest = new CloudsearchSuggest(getIndexName(), suggestName, getCloudsearchClient());
        //suggest.addFilter("status=1 AND shop_id=" + shopId);
        suggest.setHit(10);
        suggest.setQuery(query);
        String result;
		try {
			result = suggest.search();
			//JSONObject jsonResult = new JSONObject(result);
			JSONObject jsonResult = JSONObject.parseObject(result);
			List<String> suggestions = new ArrayList<String>();
			if ( null == jsonResult.get("errors")) {
				System.out.println();
				JSONArray itemsJsonArray = (JSONArray) jsonResult
						.get("suggestions");
				for (int i = 0; i < itemsJsonArray.size(); i++) {
					JSONObject item = (JSONObject) itemsJsonArray.get(i);
					suggestions.add(item.getString("suggestion"));
				}
				Map<String, Object> ret = new HashMap<String, Object>();
				ret.put("result", suggestions);
				ret.put("status", "1");
				return ret;
			} else {
				Map<String, Object> ret = new HashMap<String, Object>();
				ret.put("status", "0");
				return ret;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
        
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    public CloudsearchClient getCloudsearchClient() {
        /*String accesskey = "rMlOdjfVBlJH852g";
        String secret = "YDG248OQfD5z9w5hHQaTdGdEBeRWMP";*/
    	String accesskey = null;
    	String secret = null;
    	String host = null;

        Map<String, Object> opts = Maps.newHashMap();

        if (env.isProd()) {
        	accesskey = BizConstants.ALIYUN_ACCESSKEY_ID_PRO;
        	secret = BizConstants.ALIYUN_ACCESSKEY_SECRET_PRO;
        	host = "http://intranet.opensearch-cn-hangzhou.aliyuncs.com";
            opts.put("host", "http://intranet.opensearch-cn-hangzhou.aliyuncs.com");
            opts.put("gzip", "true");
        } else {
        	/*accesskey = BizConstants.ALIYUN_ACCESSKEY_ID_DEV;
        	secret = BizConstants.ALIYUN_ACCESSKEY_SECRET_DEV;
        	host = "http://opensearch-cn-hangzhou.aliyuncs.com";
            opts.put("host", "http://opensearch-cn-hangzhou.aliyuncs.com");
            opts.put("gzip", "true");
            opts.put("debug", "true");*/
        	accesskey = BizConstants.ALIYUN_ACCESSKEY_ID_DEV;
        	secret = BizConstants.ALIYUN_ACCESSKEY_SECRET_DEV;
        	host = "http://opensearch-cn-hangzhou.aliyuncs.com";
            opts.put("host", host);
            opts.put("gzip", "true");
        }

        //CloudsearchClient cloudsearchClient = new CloudsearchClient(accesskey, secret, opts, KeyTypeEnum.ALIYUN);
        CloudsearchClient cloudsearchClient = null;
		try {
			cloudsearchClient = new CloudsearchClient(accesskey, secret, host, opts, KeyTypeEnum.ALIYUN);
			cloudsearchClient.setMaxConnections(10);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        return cloudsearchClient;
    }

    @PostConstruct
    public void init() {
        if (env.isProd()) { // prod
            //indexName = "welinjia_search";
            indexName = "miku_item_search";
        } else { // dev or test
            //indexName = "test_welinjia_search";
            //indexName = "welinjia_search";
        	indexName = "mikutest_item_search";
        }
    }
    public static void main(String[] args) {
    	String str = "{\"request_id\":\"144723139417790789248385\",\"searchtime\":0.047048,\"suggestions\":[],\"errors\":[{\"code\":2551,\"message\":\"No such suggestion\"}]}";
    	JSONObject jsonObject = JSON.parseObject(str);
    	Object object = jsonObject.get("errors");
    	System.out.println("error...."+JSON.toJSONString(object));
    	
    	String str2 = "{\"request_id\":\"144723139417790789248385\",\"searchtime\":0.047048,\"suggestions\":[]}";
    	JSONObject jsonObject2 = JSON.parseObject(str2);
    	Object object2 = jsonObject2.get("errors");
    	if(null != object2){
    		System.out.println("error2...."+JSON.toJSONString(object2));
    	}else{
    		System.out.println("222222222222222222222222222222222222222");
    	}
    	
	}
    
}

