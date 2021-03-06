package com.welink.commons.persistence;

import com.welink.commons.domain.MikuMineScBoxDO;
import com.welink.commons.domain.MikuMineScBoxDOExample;
import com.welink.commons.vo.MikuMineScBoxVO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface MikuMineScBoxDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int countByExample(MikuMineScBoxDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int deleteByExample(MikuMineScBoxDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int insert(MikuMineScBoxDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int insertSelective(MikuMineScBoxDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    List<MikuMineScBoxDO> selectByExampleWithRowbounds(MikuMineScBoxDOExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    List<MikuMineScBoxDO> selectByExample(MikuMineScBoxDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    MikuMineScBoxDO selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByExampleSelective(@Param("record") MikuMineScBoxDO record, @Param("example") MikuMineScBoxDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByExample(@Param("record") MikuMineScBoxDO record, @Param("example") MikuMineScBoxDOExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByPrimaryKeySelective(MikuMineScBoxDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table miku_mine_sc_box
     *
     * @mbggenerated Fri May 20 09:53:42 CST 2016
     */
    int updateByPrimaryKey(MikuMineScBoxDO record);
    
    /**
     * 查询盒子订单
     * @param paramMap
     * @return
     */
    List<MikuMineScBoxVO> getMineScBoxTradeList(Map<String, Object> paramMap);
}