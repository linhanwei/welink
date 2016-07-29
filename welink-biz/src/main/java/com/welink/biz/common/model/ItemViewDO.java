package com.welink.biz.common.model;

import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 15-1-14.
 */
public class ItemViewDO {
    String pics;//主图图片
    Long price;//价格
    String title;//标题
    long itemId;//id
    Long cateId;//类目id
    Long endTime;//结束时间
    Long soldCnt;//已售数量
    Long refPrice;//参考价格
    Long itemNum;//库存数量
    String desc;//表示
    String specification;//规格
    String addr;//产地
    String detail;//详情图片
    String features;
    Integer cartCount;//购物车中数量 仅在购物车接口中使用
    Long shopId;
    Long baseItemId;
    Byte showCase;
    byte approveStatus;
    List<TagViewDO> tags;
    Long agioValue;
    String brandName;
    Byte type;	//商品类型
    Byte isTaxFree;	//是否免税(0=不免税;1=免税)
    
    Byte isrefund;		//是否可退货  0 为否   1为是
    
    Long brokerageFee;	//佣金
    Integer hasBrokerageFee;	//是否有佣金(0=无佣金;1=有佣金)
    
    Integer baseSoldQuantity;
    
    Integer multiple;		//倍数
    
    Date activtyStartTime;	//活动开始时间
    Date activtyEndTime;	//活动结束时间
    Integer activtyBaseSoldQuantity;	//活动销售基数
    Integer activtyMultiple;		//活动倍数
    Long activtyPrice;//活动价格
    Long activtySoldCnt;//活动已售数量
    Long activtyItemNum;//活动库存数量
    Integer activtyStatus;	//活动状态	-1=无活动;0=未开始;1=已开始；2=已结束
    
    //专场
    Long topicId;			//专题id
    String topicName; 		//专题名
    String topicParameter;	//满减参数
    Date topicStartTime;	//专题活动开始时间
    Date topicEndTime;		//专题活动结束时间
    
    byte isNeedPostFee;	//是否包邮(0=包邮;1=不包邮)
    
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

    public Long getBaseItemId() {
        return baseItemId;
    }

    public void setBaseItemId(Long baseItemId) {
        this.baseItemId = baseItemId;
    }

    public List<TagViewDO> getTags() {
        return tags;
    }

    public void setTags(List<TagViewDO> tags) {
        this.tags = tags;
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

	public Integer getMultiple() {
		return multiple;
	}

	public void setMultiple(Integer multiple) {
		this.multiple = multiple;
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

	public Long getBrokerageFee() {
		return brokerageFee;
	}

	public void setBrokerageFee(Long brokerageFee) {
		this.brokerageFee = brokerageFee;
	}

	public byte getIsNeedPostFee() {
		return isNeedPostFee;
	}

	public void setIsNeedPostFee(byte isNeedPostFee) {
		this.isNeedPostFee = isNeedPostFee;
	}

	public Byte getIsrefund() {
		return isrefund;
	}

	public void setIsrefund(Byte isrefund) {
		this.isrefund = isrefund;
	}

	public Integer getActivtyBaseSoldQuantity() {
		return activtyBaseSoldQuantity;
	}

	public void setActivtyBaseSoldQuantity(Integer activtyBaseSoldQuantity) {
		this.activtyBaseSoldQuantity = activtyBaseSoldQuantity;
	}

	public Integer getActivtyMultiple() {
		return activtyMultiple;
	}

	public void setActivtyMultiple(Integer activtyMultiple) {
		this.activtyMultiple = activtyMultiple;
	}

	public Long getActivtyPrice() {
		return activtyPrice;
	}

	public void setActivtyPrice(Long activtyPrice) {
		this.activtyPrice = activtyPrice;
	}

	public Long getActivtySoldCnt() {
		return activtySoldCnt;
	}

	public void setActivtySoldCnt(Long activtySoldCnt) {
		this.activtySoldCnt = activtySoldCnt;
	}

	public Long getActivtyItemNum() {
		return activtyItemNum;
	}

	public void setActivtyItemNum(Long activtyItemNum) {
		this.activtyItemNum = activtyItemNum;
	}

	public Long getCateId() {
		return cateId;
	}

	public void setCateId(Long cateId) {
		this.cateId = cateId;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public Long getSoldCnt() {
		return soldCnt;
	}

	public void setSoldCnt(Long soldCnt) {
		this.soldCnt = soldCnt;
	}

	public Long getShopId() {
		return shopId;
	}

	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public Integer getActivtyStatus() {
		return activtyStatus;
	}

	public void setActivtyStatus(Integer activtyStatus) {
		this.activtyStatus = activtyStatus;
	}

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	public Byte getIsTaxFree() {
		return isTaxFree;
	}

	public void setIsTaxFree(Byte isTaxFree) {
		this.isTaxFree = isTaxFree;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getTopicParameter() {
		return topicParameter;
	}

	public void setTopicParameter(String topicParameter) {
		this.topicParameter = topicParameter;
	}

	public Date getTopicStartTime() {
		return topicStartTime;
	}

	public void setTopicStartTime(Date topicStartTime) {
		this.topicStartTime = topicStartTime;
	}

	public Date getTopicEndTime() {
		return topicEndTime;
	}

	public void setTopicEndTime(Date topicEndTime) {
		this.topicEndTime = topicEndTime;
	}

	public Integer getHasBrokerageFee() {
		return hasBrokerageFee;
	}

	public void setHasBrokerageFee(Integer hasBrokerageFee) {
		this.hasBrokerageFee = hasBrokerageFee;
	}

}
