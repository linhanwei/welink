package com.welink.commons.persistence;

import com.welink.commons.domain.AvosNexusDO;
import com.welink.commons.domain.AvosNexusDOExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface AvosNexusDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int countByExample(AvosNexusDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int deleteByExample(AvosNexusDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int insert(AvosNexusDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int insertSelective(AvosNexusDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    List<AvosNexusDO> selectByExampleWithRowbounds(AvosNexusDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    List<AvosNexusDO> selectByExample(AvosNexusDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    AvosNexusDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByExampleSelective(@Param("record") AvosNexusDO record, @Param("example") AvosNexusDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByExample(@Param("record") AvosNexusDO record, @Param("example") AvosNexusDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByPrimaryKeySelective(AvosNexusDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table avos_nexus
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    int updateByPrimaryKey(AvosNexusDO record);
}