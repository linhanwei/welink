package com.welink.biz.service;

import com.welink.biz.common.model.SearchResult;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.domain.Item;

import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-1-29.
 */
public interface ItemSearchService {

    /**
     * 搜索排序，通过关键字来获得商品
     *
     * @param shopId        搜索的店铺，不能是null
     * @param query         搜索关键词，可以为null，如果使用关键词搜索，默认使用人工排序
     * @param categoryId    类目，如果是null，就跨类目了
     * @param offset        offset
     * @param limit         limit
     * @param type          OpenSearchType
     * @param tags          标签
     * @param isOperatorAnd 标签的关系是与还是或，两个以上才有效
     * @return
     */
    public SearchResult<Item> defaultSearch(Long shopId, String query, Long categoryId, Long brandId, int offset, int limit, OpenSearchType type, List<Long> tags, boolean isOperatorAnd);

    public Map<String, Object> cloudsearchSuggest(String query);
    
    String getIndexName();

}
