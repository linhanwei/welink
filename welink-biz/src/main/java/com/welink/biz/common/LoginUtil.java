package com.welink.biz.common;

/**
 * Created by daniel on 14-12-22.
 */
public class LoginUtil {
//
//    private static org.slf4j.Logger log = LoggerFactory.getLogger(LoginUtil.class);
//
//    /**
//     * 获取登录方式
//     *
//     * @param session
//     * @return
//     */
//    public static BizConstants.LoginEnum getLoginType(Session session) {
//        if (null != session && null != session.getAttribute(BizConstants.LOGIN_TYPE) &&
//                BizConstants.LoginEnum.SERVICE.getType() == (byte) session.getAttribute(BizConstants.LOGIN_TYPE)) {
//            return BizConstants.LoginEnum.SERVICE;
//        }
//        return null;
//    }
//
//    /**
//     * 检测用户是否登录
//     * @param session
//     * @return
//     */
//    public static boolean checkLogin(Session session){
//        try {
//            if (session == null) {
//                return false;
//            }
//            BizConstants.LoginEnum loginType = getLoginType(session);
//            if (null!=loginType&&loginType==BizConstants.LoginEnum.SERVICE){//微信公众号用户
//                if (null!=session.getAttribute(BizConstants.WE_CHAT_USER_ID)){
//                    return true;
//                }
//                return false;
//            }else {
//                //普通用户
//                if (null!=session.getAttribute("profileId")){
//                    return true;
//                }
//                return false;
//            }
//        } catch (Exception e) {
//            log.error("fetch paras from session failed . exp:" + e.getMessage());
//        }
//        return false;
//    }
}
