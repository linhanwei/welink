package com.welink.web.search.index.item;

import com.welink.commons.domain.SearchItem;

import java.util.List;

/**
 * Created by saarixx on 13/12/14.
 */
public interface ItemIndexService {

    /**
     * 更新索引
     *
     * @param searchItems
     * @param type
     */
    public void doIndex(List<SearchItem> searchItems, String type);

    public void refreshAll();

}
