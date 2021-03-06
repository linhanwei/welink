package com.welink.commons.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import com.welink.commons.domain.MikuCsadDO;
import com.welink.commons.domain.MikuCsadDOExample;
import com.welink.commons.vo.MikuCsadEvaluateVO;
import com.welink.commons.vo.MikuCsadVO;
import com.welink.commons.vo.MikuMineRecentlycontactLogVO;

@Repository
public interface MikuCsadDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int countByExample(MikuCsadDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int deleteByExample(MikuCsadDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int insert(MikuCsadDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int insertSelective(MikuCsadDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    List<MikuCsadDO> selectByExampleWithRowbounds(MikuCsadDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    List<MikuCsadDO> selectByExample(MikuCsadDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    MikuCsadDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuCsadDO record, @Param("example") MikuCsadDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int updateByExample(@Param("record") MikuCsadDO record, @Param("example") MikuCsadDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int updateByPrimaryKeySelective(MikuCsadDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_csad
     *
     * @mbggenerated Tue Apr 26 11:50:10 CST 2016
     */
    int updateByPrimaryKey(MikuCsadDO record);
    
    List<MikuCsadVO> getGroupCsadList(Map<String, Object> paramMap);
    
    /**
     * 获取聊天列表
     * @param paramMap
     * @return
     */
    List<MikuMineRecentlycontactLogVO> getMineRecentlycontactLogVOList(Map<String, Object> paramMap);
    
    /**
     * 获取专家评论列表
     * @param paramMap
     * @return
     */
    List<MikuCsadEvaluateVO> getCsadEvaluateVOList(Map<String, Object> paramMap);
    
    /**
     * 获取私人专家列表
     * @param paramMap
     * @return
     */
    List<MikuCsadVO> getPrivateExpertList(Map<String, Object> paramMap);
    
}