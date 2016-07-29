package com.welink.biz.common.model;

/**
 * Created by daniel on 15-4-27.
 */
public class OrderViewDO {
    String title;
    Long cateId;
    Long price;
    String pics;
    Integer num;
    Byte rated;
    Long itemId;
    String specification;
    
    Long id;
    Long totalFee;
    Byte returnStatus;

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Byte getRated() {
        return rated;
    }

    public void setRated(Byte rated) {
        this.rated = rated;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

	public Long getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Long totalFee) {
		this.totalFee = totalFee;
	}

	public Byte getReturnStatus() {
		return returnStatus;
	}

	public void setReturnStatus(Byte returnStatus) {
		this.returnStatus = returnStatus;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
