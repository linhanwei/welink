package com.welink.commons.persistence;

import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.ObjectTaggedDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectTaggedDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int countByExample(ObjectTaggedDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int deleteByExample(ObjectTaggedDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int insert(ObjectTaggedDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int insertSelective(ObjectTaggedDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    List<ObjectTaggedDO> selectByExampleWithRowbounds(ObjectTaggedDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    List<ObjectTaggedDO> selectByExample(ObjectTaggedDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    ObjectTaggedDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int updateByExampleSelective(@Param("record") ObjectTaggedDO record, @Param("example") ObjectTaggedDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int updateByExample(@Param("record") ObjectTaggedDO record, @Param("example") ObjectTaggedDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int updateByPrimaryKeySelective(ObjectTaggedDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table object_tagged
     *
     * @mbggenerated Mon Dec 07 09:54:38 CST 2015
     */
    int updateByPrimaryKey(ObjectTaggedDO record);
}