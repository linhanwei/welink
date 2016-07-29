package com.welink.commons.persistence;

import com.welink.commons.domain.MikuCsadGroupDO;
import com.welink.commons.domain.MikuCsadGroupDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface MikuCsadGroupDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int countByExample(MikuCsadGroupDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int deleteByExample(MikuCsadGroupDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int insert(MikuCsadGroupDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int insertSelective(MikuCsadGroupDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    List<MikuCsadGroupDO> selectByExampleWithRowbounds(MikuCsadGroupDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    List<MikuCsadGroupDO> selectByExample(MikuCsadGroupDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    MikuCsadGroupDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuCsadGroupDO record, @Param("example") MikuCsadGroupDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int updateByExample(@Param("record") MikuCsadGroupDO record, @Param("example") MikuCsadGroupDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int updateByPrimaryKeySelective(MikuCsadGroupDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad_group
     *
     * @mbggenerated Wed Apr 20 16:18:39 CST 2016
     */
    int updateByPrimaryKey(MikuCsadGroupDO record);
}