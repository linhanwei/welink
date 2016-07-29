package com.welink.biz.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 15-1-14.
 */
public class CategoryViewDO {

    long categoryId;

    String name;
    
    String pic;

    List<Banner> banners = new ArrayList<>();

    Integer count;
    
    Byte isParent;
    
    Long parentId;
    
    Long level;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Banner> getBanners() {
        return banners;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
    }

	public Byte getIsParent() {
		return isParent;
	}

	public void setIsParent(Byte isParent) {
		this.isParent = isParent;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public Long getLevel() {
		return level;
	}

	public void setLevel(Long level) {
		this.level = level;
	}

}
