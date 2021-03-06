package com.welink.commons.persistence;

import com.welink.commons.domain.ProfileCoopDO;
import com.welink.commons.domain.ProfileCoopDOExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileCoopDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int countByExample(ProfileCoopDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int deleteByExample(ProfileCoopDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int insert(ProfileCoopDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int insertSelective(ProfileCoopDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    List<ProfileCoopDO> selectByExample(ProfileCoopDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    ProfileCoopDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int updateByExampleSelective(@Param("record") ProfileCoopDO record, @Param("example") ProfileCoopDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int updateByExample(@Param("record") ProfileCoopDO record, @Param("example") ProfileCoopDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int updateByPrimaryKeySelective(ProfileCoopDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table profile_coop
     *
     * @mbggenerated Wed Jan 07 14:39:30 CST 2015
     */
    int updateByPrimaryKey(ProfileCoopDO record);
}