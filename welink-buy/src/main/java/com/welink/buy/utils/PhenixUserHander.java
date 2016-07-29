package com.welink.buy.utils;


/**
 *
 */
public class PhenixUserHander {

    private static final String START_CHAR = "v";

    public static String encodeUserId(Long userId) throws RuntimeException {

        if (userId == null || userId == 0) {
            return null;
        }

        try {
            String base64Uid = PhenixBase64.encode(String.valueOf(userId).getBytes());
            return START_CHAR + base64Uid;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


    public static long decodeUserId(String userIdCode) throws RuntimeException {

        if (userIdCode == null || userIdCode.length() < 2 || !userIdCode.startsWith(START_CHAR)) {
            return 0;
        }
        try {
            String userBase64 = userIdCode.substring(1);
            String userIdStr = new String(PhenixBase64.decode(userBase64));
            return Long.valueOf(userIdStr);

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
