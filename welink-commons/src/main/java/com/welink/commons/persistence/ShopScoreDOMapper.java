package com.welink.commons.persistence;

import com.welink.commons.domain.ShopScoreDO;
import com.welink.commons.domain.ShopScoreDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopScoreDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int countByExample(ShopScoreDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int deleteByExample(ShopScoreDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int insert(ShopScoreDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int insertSelective(ShopScoreDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    List<ShopScoreDO> selectByExampleWithRowbounds(ShopScoreDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    List<ShopScoreDO> selectByExample(ShopScoreDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    ShopScoreDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByExampleSelective(@Param("record") ShopScoreDO record, @Param("example") ShopScoreDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByExample(@Param("record") ShopScoreDO record, @Param("example") ShopScoreDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByPrimaryKeySelective(ShopScoreDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table shop_score
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByPrimaryKey(ShopScoreDO record);
}