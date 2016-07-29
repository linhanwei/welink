package com.welink.biz.service;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.*;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by daniel on 14-10-16.
 */
@Service
public class ConversationService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ConversationService.class);

    @Resource
    private LinkConversationDOMapper linkConversationDOMapper;

    @Resource
    private LinkMessageDOMapper linkMessageDOMapper;

    @Resource
    private LinkCategoryMapper linkCategoryMapper;

    @Resource
    private LinkReplyDOMapper linkReplyDOMapper;

    @Resource
    private LinkNotifyDOMapper linkNotifyDOMapper;

    public void clearLinkMsg(long profileId, long communityId) {
        LinkMessageDO linkMessageDO = new LinkMessageDO();
        linkMessageDO.setMsgCount(0l);
        LinkMessageDOExample lExample = new LinkMessageDOExample();
        lExample.createCriteria().andProfileIdEqualTo(profileId).andCommunityIdEqualTo(communityId);
        if (linkMessageDOMapper.updateByExampleSelective(linkMessageDO, lExample) < 1) {
            log.error("clear link message failed. profileId:" + profileId);
        }
        LinkNotifyDO linkNotifyDO = new LinkNotifyDO();
        linkNotifyDO.setStatus((byte) 0);
        LinkNotifyDOExample linkExample = new LinkNotifyDOExample();
        List<Byte> toclear = new ArrayList<>();
        toclear.add((byte) 1);
        toclear.add((byte) 2);
        linkExample.createCriteria().andProfileIdEqualTo(profileId).andStatusIn(toclear).andCommunityIdEqualTo(communityId);
        if (linkNotifyDOMapper.updateByExampleSelective(linkNotifyDO, linkExample) < 1) {
            log.error("clear link notify failed. profileId:" + profileId);
        }
    }

    /**
     * 某个用户是否有邻里信息
     *
     * @param profileId
     * @return
     */
    public boolean hasLinkNotify(long profileId) {
        LinkNotifyDOExample lExample = new LinkNotifyDOExample();
        lExample.createCriteria().andProfileIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        return linkNotifyDOMapper.countByExample(lExample) > 0;
    }

    /**
     * 某个用户消息数目
     *
     * @param profileId
     * @return
     */
    public int linkNotifyCount(long profileId) {
        LinkNotifyDOExample lExample = new LinkNotifyDOExample();
        lExample.createCriteria().andProfileIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        return linkNotifyDOMapper.countByExample(lExample);
    }

    /**
     * 获取某个人的未读消息
     *
     * @param profileId
     * @return
     */
    public List<LinkNotifyDO> fetchLinkNotifys(long profileId) {
        LinkNotifyDOExample lExample = new LinkNotifyDOExample();
        lExample.createCriteria().andProfileIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        lExample.setOrderByClause("date_created DESC");
        return linkNotifyDOMapper.selectByExample(lExample);
    }

    /**
     * 分页查询邻里消息
     *
     * @param startRow
     * @param size
     * @return
     */
    public List<LinkNotifyDO> fetchLinkNotifysByPage(int startRow, int size, long profileId, long communityId) {
        LinkNotifyDOExample linkNotifyDOExample = new LinkNotifyDOExample();
        linkNotifyDOExample.createCriteria() //
                .andProfileIdEqualTo(profileId) //
                .andCommunityIdEqualTo(communityId);

        linkNotifyDOExample.setOffset(startRow);
        linkNotifyDOExample.setLimit(size);

        return linkNotifyDOMapper.selectByExample(linkNotifyDOExample);
    }

    /**
     * 添加邻里消息
     *
     * @param profileId
     * @param operatorId
     * @param convId
     * @param replyId
     * @param actionType
     * @param content
     * @return
     */
    public boolean addLinkNotify(long communityId, long profileId, long operatorId, long convId, long replyId, byte actionType, String content) {
        LinkNotifyDO linkNotifyDO = new LinkNotifyDO();
        linkNotifyDO.setLastUpdate(new Date());
        linkNotifyDO.setActionType(actionType);
        linkNotifyDO.setConvId(convId);
        linkNotifyDO.setOperatorId(operatorId);
        linkNotifyDO.setProfileId(profileId);
        linkNotifyDO.setCommunityId(communityId);
        linkNotifyDO.setReplyId(replyId);
        linkNotifyDO.setStatus((byte) 1);
        linkNotifyDO.setDateCreated(new Date());

        if (linkNotifyDOMapper.insertSelective(linkNotifyDO) < 0) {
            log.error("insert notify message error. convId:" + convId + ", replyId" + replyId);
            return false;
        }
        return true;
    }

    /**
     * 获取所有分享的分类
     *
     * @return
     */
    public List<LinkCategory> findAllCates() {
        LinkCategoryExample lExample = new LinkCategoryExample();
        lExample.createCriteria().andIdNotEqualTo(null);
        return linkCategoryMapper.selectByExample(lExample);
    }


    /**
     * 分页查询
     *
     * @param startRow
     * @param size
     * @return
     */
    public List<LinkConversationDO> queryConversationsByPage(int offset, int limit, long type, long communityId) {
        LinkConversationDOExample linkConversationDOExample = new LinkConversationDOExample();
        linkConversationDOExample.setOrderByClause("id DESC");
        LinkConversationDOExample.Criteria criteria = linkConversationDOExample.createCriteria();
        criteria.andStatusEqualTo((byte) 1);
        criteria.andCommunityIdEqualTo(communityId);

        linkConversationDOExample.setLimit(limit);
        linkConversationDOExample.setOffset(offset);

        if (type > 0) {
            criteria.andConTypeEqualTo(type);
        }

        return linkConversationDOMapper.selectByExample(linkConversationDOExample);
    }

    /**
     * 添加一条话题 图文分享
     *
     * @param profileId
     * @param conType
     * @param content
     * @param pics
     * @param status
     * @return
     */
    public boolean addConversation(long profileId, long conType, String content, String pics, byte status) {
        LinkConversationDO linkConversationDO = new LinkConversationDO();
        linkConversationDO.setProfileId(profileId);
        linkConversationDO.setConType(conType);
        linkConversationDO.setContent(content);
        linkConversationDO.setPics(pics);
        linkConversationDO.setStatus(status);
        linkConversationDO.setDateCreated(new Date());
        linkConversationDO.setLastUpdate(new Date());
        linkConversationDO.setFavourCount(0l);
        linkConversationDO.setReplyCount(0l);
        if (linkConversationDOMapper.insertSelective(linkConversationDO) > 0) {
            return true;
        }
        log.error("insert link conversation failed. profileId:" + profileId);
        return false;
    }

    /**
     * @param communityId
     * @param profileId
     * @param conType
     * @param content
     * @param pics
     * @param status
     * @return
     */
    public boolean addConversation(long communityId, long profileId, long conType, String content, String pics, byte status) {
        LinkConversationDO linkConversationDO = new LinkConversationDO();
        linkConversationDO.setProfileId(profileId);
        linkConversationDO.setConType(conType);
        linkConversationDO.setContent(content);
        linkConversationDO.setPics(pics);
        linkConversationDO.setStatus(status);
        linkConversationDO.setDateCreated(new Date());
        linkConversationDO.setLastUpdate(new Date());
        linkConversationDO.setFavourCount(0l);
        linkConversationDO.setReplyCount(0l);
        linkConversationDO.setCommunityId(communityId);
        if (linkConversationDOMapper.insertSelective(linkConversationDO) > 0) {
            return true;
        }
        log.error("insert link conversation failed. profileId:" + profileId);
        return false;
    }

    /**
     * 对某个话题分享点赞，同时会更新到消息记录中
     *
     * @param convId
     * @param whoFavour
     * @return
     */
    //@Async
    public void addFavour(long convId, long whoFavour, long communityId) {
        //獲取conversation
        LinkConversationDO conversationDO = linkConversationDOMapper.selectByPrimaryKey(convId);
        if (null == conversationDO) {
            return;
        }
        //1. 添加消息
        LinkMessageDOExample mExample = new LinkMessageDOExample();
        mExample.createCriteria().andProfileIdEqualTo(conversationDO.getProfileId()).andCommunityIdEqualTo(communityId);
        List<LinkMessageDO> linkMessageDOs = linkMessageDOMapper.selectByExample(mExample);
        LinkMessageDO linkMessageDO = new LinkMessageDO();
        linkMessageDO.setDateCreated(new Date());
        linkMessageDO.setLastUpdate(new Date());
        linkMessageDO.setCommunityId(communityId);
        if (null != linkMessageDOs && linkMessageDOs.size() > 0) {
            //update
            long cnt = linkMessageDOs.get(0).getMsgCount();
            linkMessageDO.setMsgCount(cnt + 1);
            linkMessageDO.setProfileId(conversationDO.getProfileId());
            if (linkMessageDOMapper.updateByExampleSelective(linkMessageDO, mExample) < 1) {
                log.error("update link message failed . conversation id:" + convId + ",who favour:" + whoFavour);
            }
        } else {
            //add
            linkMessageDO.setProfileId(conversationDO.getProfileId());
            linkMessageDO.setMsgCount(1l);
            if (linkMessageDOMapper.insertSelective(linkMessageDO) < 0) {
                log.error("insert link message failed . conversation id:" + convId + ",who favour:" + whoFavour);
            }
        }
        //2. 添加reply 更新本分享的赞数量 添加reply记录
        long replyId = addReplyOrFav(convId, whoFavour, null, null, BizConstants.LinkReplyType.FAVOUR.getType());

        //3. 添加 notify
        LinkNotifyDO linkNotifyDO = new LinkNotifyDO();
        linkNotifyDO.setDateCreated(new Date());
        linkNotifyDO.setStatus((byte) 1);
        linkNotifyDO.setConvId(convId);
        linkNotifyDO.setReplyId(replyId);
        linkNotifyDO.setOperatorId(whoFavour);
        linkNotifyDO.setStatus((byte) 1);
        linkNotifyDO.setLastUpdate(new Date());
        linkNotifyDO.setActionType(BizConstants.LinkReplyType.FAVOUR.getType());
        linkNotifyDO.setProfileId(conversationDO.getProfileId());
        linkNotifyDO.setCommunityId(communityId);
        if (linkNotifyDOMapper.insertSelective(linkNotifyDO) < 0) {
            log.warn("insert link notify message failed. who op opid:" + whoFavour + ",convId:" + convId);
        }
    }

    /**
     * 获取conversation
     *
     * @param id
     * @return
     */
    public LinkConversationDO fetchConversation(long id) {
        return linkConversationDOMapper.selectByPrimaryKey(id);
    }

    /**
     * 获取link message
     *
     * @param profileId
     * @return
     */
    public LinkMessageDO fetchLinkMessage(long profileId, long communityId) {
        LinkMessageDOExample lExample = new LinkMessageDOExample();
        lExample.createCriteria().andProfileIdEqualTo(profileId).andCommunityIdEqualTo(communityId);

        List<LinkMessageDO> linkMessageDOs = linkMessageDOMapper.selectByExample(lExample);
        if (null != linkMessageDOs && linkMessageDOs.size() > 0) {
            return linkMessageDOs.get(0);
        }
        return null;
    }

    /**
     * 获取某个conversation下所有的回复
     *
     * @param convId
     * @return
     */
    public List<LinkReplyDO> fetchReplys(long convId) {
        LinkReplyDOExample lExample = new LinkReplyDOExample();
        lExample.createCriteria().andConIdEqualTo(convId).andStatusEqualTo((byte) 1);
        lExample.setOrderByClause("date_created DESC");
        return linkReplyDOMapper.selectByExample(lExample);
    }

    /**
     * 分页查询分享的回复
     *
     * @return
     */
    public List<LinkReplyDO> fetchReplysByPage(long convId, int offset, int limit) {
        LinkReplyDOExample linkReplyDOExample = new LinkReplyDOExample();
        linkReplyDOExample.setOrderByClause("id DESC");
        linkReplyDOExample.createCriteria() //
                .andStatusEqualTo((byte) 1)
                .andConIdEqualTo(convId);

        linkReplyDOExample.setOffset(offset);
        linkReplyDOExample.setLimit(limit);

        return linkReplyDOMapper.selectByExample(linkReplyDOExample);
    }


    /**
     * 根据id查询reply
     *
     * @param id
     * @return
     */
    public LinkReplyDO fetchReplyById(long id) {
        return linkReplyDOMapper.selectByPrimaryKey(id);
    }

    /**
     * 添加一条回复
     *
     * @param conId
     * @param whoReply
     * @param content
     * @param pics
     * @return
     */
    public boolean addReply(long conId, long whoReply, String content, String pics) {
        LinkReplyDO linkReplyDO = new LinkReplyDO();
        linkReplyDO.setDateCreated(new Date());
        linkReplyDO.setLastUpdate(new Date());
        linkReplyDO.setConId(conId);
        linkReplyDO.setPics(pics);
        linkReplyDO.setProfileId(whoReply);
        linkReplyDO.setActionType(BizConstants.LinkReplyType.REPLY.getType());
        linkReplyDO.setStatus((byte) 1);
        linkReplyDO.setContent(content);
        if (linkReplyDOMapper.insertSelective(linkReplyDO) < 0) {
            return false;
        } else {
            //成功后更新回复
            LinkConversationDO linkConversationDO = linkConversationDOMapper.selectByPrimaryKey(conId);
            if (null != linkConversationDO) {
                long replyCount = linkConversationDO.getReplyCount();
                linkConversationDO.setReplyCount(replyCount + 1);
                linkConversationDO.setLastUpdate(new Date());
            }
            LinkConversationDOExample lExample = new LinkConversationDOExample();
            lExample.createCriteria().andIdEqualTo(conId);
            if (linkConversationDOMapper.updateByExampleSelective(linkConversationDO, lExample) < 1) {
                //降级，及时更新失败，也成功返回
                log.error("update conversation failed. id:" + conId);
            }
        }
        return true;
    }

    /**
     * 添加回复或点赞
     *
     * @param conId
     * @param whoReply
     * @param content
     * @param pics
     * @param type
     * @return
     */
    public long addReplyOrFav(long conId, long whoReply, String content, String pics, byte type) {
        LinkReplyDO linkReplyDO = new LinkReplyDO();
        linkReplyDO.setDateCreated(new Date());
        linkReplyDO.setLastUpdate(new Date());
        linkReplyDO.setConId(conId);
        linkReplyDO.setPics(pics);
        linkReplyDO.setProfileId(whoReply);
        linkReplyDO.setStatus((byte) 1);
        linkReplyDO.setContent(content);
        linkReplyDO.setActionType(type);
        long replyId = -1l;
        if (linkReplyDOMapper.insertSelective(linkReplyDO) < 0) {
            return -1;
        } else {
            //成功后更新回复
            LinkConversationDO linkConversationDO = linkConversationDOMapper.selectByPrimaryKey(conId);
            if (null != linkConversationDO) {
                long replyCount = linkConversationDO.getReplyCount();
                long favCount = linkConversationDO.getFavourCount();
                if (type == BizConstants.LinkReplyType.FAVOUR.getType()) {
                    linkConversationDO.setFavourCount(favCount + 1);
                } else {
                    linkConversationDO.setReplyCount(replyCount + 1);
                }
                linkConversationDO.setLastUpdate(new Date());
            }
            LinkConversationDOExample lExample = new LinkConversationDOExample();
            lExample.createCriteria().andIdEqualTo(conId);
            if (linkConversationDOMapper.updateByExampleSelective(linkConversationDO, lExample) < 1) {
                //降级，及时更新失败，也成功返回
                log.error("update conversation failed. id:" + conId);
            }
        }
        //
        return linkReplyDO.getId();
    }


    /**
     * 获取所有分享分类
     *
     * @return
     */
    public List<LinkCategory> fetchLinkCategory() {
        LinkCategoryExample lExample = new LinkCategoryExample();
        lExample.createCriteria().andIdNotEqualTo(-1l);
        return linkCategoryMapper.selectByExample(lExample);
    }


    /**
     * 获取某个人合法的分享
     *
     * @param profileId
     * @return
     */
    public List<LinkConversationDO> fetchConversations(long profileId) {
        LinkConversationDOExample lExample = new LinkConversationDOExample();
        lExample.createCriteria().andProfileIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        return linkConversationDOMapper.selectByExample(lExample);
    }


    /**
     * 查询消息的未读数
     *
     * @param profileId
     * @param communityId
     * @return
     */
    public long unReadCount(long profileId, long communityId) {
        LinkNotifyDOExample lnExample = new LinkNotifyDOExample();
        lnExample.createCriteria().andProfileIdEqualTo(profileId).andCommunityIdEqualTo(communityId)
                .andStatusEqualTo((byte) BizConstants.UserNotifyMsgStatus.NON_READ.getStatus());
        return linkNotifyDOMapper.countByExample(lnExample);
    }
}
