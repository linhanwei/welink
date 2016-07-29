package com.welink.biz.service;

import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.buy.utils.BaseResult;
import com.welink.commons.domain.CartDO;
import com.welink.commons.domain.CartDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.persistence.CartDOMapper;
import com.welink.commons.persistence.ItemMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-11-17.
 */
@Service
public class CartService {

    //log
    private static org.slf4j.Logger log = LoggerFactory.getLogger(CartService.class);

    @Resource
    private CartDOMapper cartDOMapper;

    @Resource
    private ItemService itemService;

    @Resource
    private ItemMapper itemMapper;

    /**
     * 清除购物车
     *
     * @param userId
     * @return
     */
    public boolean clearCart(long userId) {
        CartDOExample cqExample = new CartDOExample();
        cqExample.createCriteria().andUserIdEqualTo(userId);
        List<CartDO> cartDOs = cartDOMapper.selectByExample(cqExample);
        if (null != cartDOs && cartDOs.size() > 0) {
            for (CartDO cart : cartDOs) {
                if (cartDOMapper.deleteByPrimaryKey(cart.getId()) < 0) {
                    log.error("clear cart failed. userId:" + userId);
                }
            }
        }
        return true;
    }

    /**
     * 获取用户购物车中的有效商品商品
     *
     * @param userId
     * @return
     */
    public List<Item> fetchCartItems(long userId) {
        BaseResult<List<Item>> itemResult = null;
        CartDOExample cartDOExample = new CartDOExample();
        cartDOExample.createCriteria().andUserIdEqualTo(userId);
        List<CartDO> cartDOs = cartDOMapper.selectByExample(cartDOExample);
        List<Long> itemIds = new ArrayList<>();
        if (null != cartDOs && cartDOs.size() > 0) {
            for (CartDO c : cartDOs) {
                itemIds.add(c.getItemId());
            }
        }
        if (itemIds.size() > 0) {
            itemResult = itemService.fetchItemsByItemIds(itemIds);
            if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                for (Item item : itemResult.getResult()) {
                    for (CartDO cartDO : cartDOs) {
                        if (Long.compare(item.getId(), cartDO.getItemId()) == 0) {
                            item.setNum(cartDO.getItem_count().intValue());
                        }
                    }
                }
                return itemResult.getResult();
            }
        }
        return null;
    }

    /**
     * 根据商品ids获取购物车中商品
     *
     * @param userId
     * @param ids
     * @return
     */
    public List<Item> fetchCartItemsByIds(long userId, List<Long> ids) {
        BaseResult<List<Item>> itemResult = null;
        List<Item> itemList = Lists.newArrayList();
        CartDOExample cartDOExample = new CartDOExample();
        cartDOExample.createCriteria().andUserIdEqualTo(userId).andItemIdIn(ids);
        List<CartDO> cartDOs = cartDOMapper.selectByExample(cartDOExample);
        List<Long> itemIds = new ArrayList<>();
        if (null != cartDOs && cartDOs.size() > 0) {
            for (CartDO c : cartDOs) {
                itemIds.add(c.getItemId());
            }
        }
        if (itemIds.size() > 0) {
            itemResult = itemService.fetchItemsByItemIds(itemIds);
            if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                for (Item item : itemResult.getResult()) {
                    for (CartDO cartDO : cartDOs) {
                        if (Long.compare(item.getId(), cartDO.getItemId()) == 0) {
                            item.setNum(cartDO.getItem_count().intValue());
                            itemList.add(item);
                        }
                    }
                }
                itemResult.setResult(itemList);
                return itemResult.getResult();
            }
        }
        return null;
    }

    public List<Item> fetchCartItems(long userId, long shopId) {
        BaseResult<List<Item>> itemResult = null;
        List<Item> itemList = Lists.newArrayList();
        CartDOExample cartDOExample = new CartDOExample();
        cartDOExample.createCriteria().andUserIdEqualTo(userId);
        cartDOExample.setOrderByClause("date_created DESC");
        List<CartDO> cartDOs = cartDOMapper.selectByExample(cartDOExample);
        List<Long> itemIds = new ArrayList<>();
        if (null != cartDOs && cartDOs.size() > 0) {
            for (CartDO c : cartDOs) {
                itemIds.add(c.getItemId());
            }
        }
        if (itemIds.size() > 0) {
            itemResult = itemService.fetchItemsByItemIds(itemIds);
            if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                for (Item item : itemResult.getResult()) {
                    for (CartDO cartDO : cartDOs) {
                        //if (Long.compare(item.getId(), cartDO.getItemId()) == 0 && Long.compare(item.getShopId(), shopId) == 0) {
                    	if (Long.compare(item.getId(), cartDO.getItemId()) == 0 ) {
                            item.setNum(cartDO.getItem_count().intValue());
                            itemList.add(item);
                        }
                    }
                }
                itemResult.setResult(itemList);
                return itemResult.getResult();
            }
        }
        return null;
    }

    /**
     * 添加/減少商品至购物车 需要更新时候更新
     *
     * @param userId
     * @param itemCounts
     * @return
     */
    public BaseResult<List<Long>> updateItem2Cart(long userId, Map<Long, Long> itemCounts) {
        List<Long> failedItems = Lists.newArrayList();
        BaseResult result = new BaseResult();
        for (Long itemId : itemCounts.keySet()) {
            CartDO cartDO = new CartDO();
            cartDO.setItem_count(itemCounts.get(itemId.longValue()));
            cartDO.setItemId(itemId);
            cartDO.setDateCreated(new Date());
            cartDO.setLastUpdated(new Date());
            cartDO.setUserId(userId);
            CartDOExample cExample = new CartDOExample();
            cExample.createCriteria().andUserIdEqualTo(userId).andItemIdEqualTo(itemId);
            List<CartDO> cartDOs = cartDOMapper.selectByExample(cExample);
            if (null != cartDOs && cartDOs.size() > 0) {
                cartDO.setItem_count((itemCounts.get(itemId.longValue()) + cartDOs.get(0).getItem_count()) > 0 ? (itemCounts.get(itemId.longValue()) + cartDOs.get(0).getItem_count()) : 0);
                if (cartDO.getItem_count() < 1) {
                    if (cartDOMapper.deleteByPrimaryKey(cartDOs.get(0).getId()) < 0) {
                        log.error("delete item failed（item num = 0）. userId:" + userId + ",itemId:" + itemId);
                    }
                } else {
                    //判断库存是否足够
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    if (item.getNum() < (itemCounts.get(itemId.longValue()) + cartDOs.get(0).getItem_count())) {
                        failedItems.add(item.getId());
                        result.setSuccess(false);
                        result.setCode(BizErrorEnum.ADD_CART_FAILED_ITEM_COUNT.getCode());
                        result.setResult(failedItems);
                        if (item.getNum() > 0) {
                            result.setMessage("啊哦~商品 " + item.getTitle() + " 库存不足了~仅剩" + item.getNum() + "件");
                        } else {
                            result.setMessage("啊哦~商品 " + item.getTitle() + " 库存不足了~");
                        }
                        return result;
                    }
                    if (cartDOMapper.updateByExampleSelective(cartDO, cExample) < 1) {
                        log.error("update item num  failed. userId:" + userId + ",itemId:" + itemId);
                    }
                }
            } else {
                if (cartDO.getItem_count() > 0) {
                    //判断库存是否足够
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    if (item.getNum() < cartDO.getItem_count()) {
                        result.setSuccess(false);
                        result.setCode(BizErrorEnum.ADD_CART_FAILED_ITEM_COUNT.getCode());
                        failedItems.add(item.getId());
                        if (item.getNum() > 0) {
                            result.setMessage("啊哦~商品 " + item.getTitle() + " 库存不足了~仅剩" + item.getNum() + "件");
                        } else {
                            result.setMessage("啊哦~商品 " + item.getTitle() + " 库存不足了~");
                        }
                        result.setResult(failedItems);
                        return result;
                    }
                    if (cartDOMapper.insertSelective(cartDO) < 0) {
                        log.error("add item to cart failed. userId:" + userId + ",itemId:" + itemId);
                    }
                }
            }
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * 获取购物车商品种类数量
     *
     * @param userId
     * @return
     */
    public int fetchItemKindsInCart(long userId) {
        CartDOExample cExample = new CartDOExample();
        cExample.createCriteria().andUserIdEqualTo(userId);
        return cartDOMapper.countByExample(cExample);
    }

    /**
     * 从购物车删除商品
     *
     * @param userId
     * @param itemId
     * @return
     */
    public boolean deleteFromCart(long userId, long itemId) {
        CartDOExample cartDOExample = new CartDOExample();
        cartDOExample.createCriteria().andUserIdEqualTo(userId).andItemIdEqualTo(itemId);
        if (cartDOMapper.deleteByExample(cartDOExample) < 0) {
            log.error("delete item from cart. userId:" + userId + ",itemId:" + itemId);
        }
        return true;
    }

    public List<CartDO> fetchCartItemsByUserAndShop(long userId, long shopId) {
        CartDOExample cartDOExample = new CartDOExample();
        cartDOExample.createCriteria().andUserIdEqualTo(userId);
        List<CartDO> cartDOs = cartDOMapper.selectByExample(cartDOExample);
        List<CartDO> cartDOList = Lists.newArrayList();
        if (null != cartDOs && cartDOs.size() > 0) {
            for (CartDO cart : cartDOs) {
                Item item = itemMapper.selectByPrimaryKey(cart.getItemId());
                if (item != null && item.getShopId() == shopId) {
                    cartDOList.add(cart);
                }
            }
        }
        return cartDOList;
    }

}
