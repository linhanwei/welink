package com.welink.commons.vo;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 15-1-14.
 */
public class ItemTagActivtyVO {
    String pics;//主图图片
    Long price;//价格
    String title;//标题
    long itemId;//id
    long cateId;//类目id
    long endTime;//结束时间
    long soldCnt;//已售数量
    Long refPrice;//参考价格
    Long itemNum;//库存数量
    String desc;//表示
    String specification;//规格
    String addr;//产地
    String detail;//详情图片
    String features;
    Integer cartCount;//购物车中数量 仅在购物车接口中使用
    long shopId;
    Long baseItemId;
    Byte showCase;
    byte approveStatus;
    List<TagViewVO> tags;
    Long agioValue;
    String brandName;
    
    Integer baseSoldQuantity;
    
    String tagName;
    String kv;
    Integer multiple;		//倍数
    BigInteger bit;
    Date activtyStartTime;
    Date activtyEndTime;
    Integer activityNum;
    Integer activitySoldNum;

    public Long getAgioValue() {
        return agioValue;
    }

    public void setAgioValue(Long agioValue) {
        this.agioValue = agioValue;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(Long refPrice) {
        this.refPrice = refPrice;
    }

    public Byte getShowCase() {
        return showCase;
    }

    public void setShowCase(Byte showCase) {
        this.showCase = showCase;
    }

    public byte getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(byte approveStatus) {
        this.approveStatus = approveStatus;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getCateId() {
        return cateId;
    }

    public void setCateId(long cateId) {
        this.cateId = cateId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getSoldCnt() {
        return soldCnt;
    }

    public void setSoldCnt(long soldCnt) {
        this.soldCnt = soldCnt;
    }

    public Long getItemNum() {
        return itemNum;
    }

    public void setItemNum(Long itemNum) {
        this.itemNum = itemNum;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public Integer getCartCount() {
        return cartCount;
    }

    public void setCartCount(Integer cartCount) {
        this.cartCount = cartCount;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public Long getBaseItemId() {
        return baseItemId;
    }

    public void setBaseItemId(Long baseItemId) {
        this.baseItemId = baseItemId;
    }

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public Integer getBaseSoldQuantity() {
		return baseSoldQuantity;
	}

	public void setBaseSoldQuantity(Integer baseSoldQuantity) {
		this.baseSoldQuantity = baseSoldQuantity;
	}

	public Date getActivtyStartTime() {
		return activtyStartTime;
	}

	public void setActivtyStartTime(Date activtyStartTime) {
		this.activtyStartTime = activtyStartTime;
	}

	public Date getActivtyEndTime() {
		return activtyEndTime;
	}

	public void setActivtyEndTime(Date activtyEndTime) {
		this.activtyEndTime = activtyEndTime;
	}

	public Integer getActivityNum() {
		return activityNum;
	}

	public void setActivityNum(Integer activityNum) {
		this.activityNum = activityNum;
	}

	public Integer getActivitySoldNum() {
		return activitySoldNum;
	}

	public void setActivitySoldNum(Integer activitySoldNum) {
		this.activitySoldNum = activitySoldNum;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getKv() {
		return kv;
	}

	public void setKv(String kv) {
		this.kv = kv;
	}

	public BigInteger getBit() {
		return bit;
	}

	public void setBit(BigInteger bit) {
		this.bit = bit;
	}

	public Integer getMultiple() {
		return multiple;
	}

	public void setMultiple(Integer multiple) {
		this.multiple = multiple;
	}

	public List<TagViewVO> getTags() {
		return tags;
	}

	public void setTags(List<TagViewVO> tags) {
		this.tags = tags;
	}
}
