package com.welink.commons.utils;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class PhenixBase64 {

    static final char base64EncodeChars[] = {'N', '9', 'j', 'm', 'H', 'h', /* 索引 0 ~ 5*/
            'z', 'e', 'I', '-', 'K', '2', 'v', 'M', 'O', 'o', 'Q', 'P', 'X',  /* 索引6 ~ 18*/
            'F', '8', 'R', 'w', '_', 'x', 'Z', 'a', '1', 'c', '7', 'E', 'f',  /* 索引 19 ~ 31*/
            'g', '*', 'i', 'C', 'k', 'l', 'D', 'n', 'A', 'p', '5', 'r', 's',  /* 索引 32 ~ 44*/
            't', '3', 'U', 'W', 'Y', 'y', 'G', '0', 'b', 'L', 'u', '4', 'S',  /* 索引 45 ~ 57*/
            '6', 'd', 'V', 'B', 'q', 'J'}; 								 	/* 索引58 ~ 63*/


    static final byte base64DecodeChars[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, -1, 9, -1, -1, 52, 27, 11, 46, 56, 42,
            58, 29, 20, 1, -1, -1, -1, -1, -1, -1, -1, 40, 61, 35, 38, 30, 19, 51, 4, 8, 63, 10, 54,
            13, 0, 14, 17, 16, 21, 57, -1, 47, 60, 48, 18, 49, 25, -1, -1, -1, -1, 23, -1, 26, 53,
            28, 59, 7, 31, 32, 5, 34, 2, 36, 37, 3, 39, 15, 41, 62, 43, 44, 45, 55, 12, 22, 24, 50, 6};

    public static String encode(byte[] data) throws Exception {
        StringBuilder sb = new StringBuilder();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;
        while (i < len) {
            b1 = data[i++] & 0xff;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
                sb.append("TT");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 0x03) << 4)
                        | ((b2 & 0xf0) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
                sb.append("T");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 0x03) << 4)
                    | ((b2 & 0xf0) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2)
                    | ((b3 & 0xc0) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 0x3f]);
        }
        return sb.toString();
    }

    public static byte[] decode(String str) throws Exception {
        if (org.apache.commons.lang.StringUtils.isBlank(str) || StringUtils.equals(str, "null")) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        byte[] data = str.getBytes("US-ASCII");
        int len = data.length;
        int i = 0;
        int b1, b2, b3, b4;
        while (i < len) {

            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1)
                break;

            do {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1)
                break;
            sb.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

            do {
                b3 = data[i++];
                if (b3 == 84)
                    return sb.toString().getBytes("iso8859-1");
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1)
                break;
            sb.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

            do {
                b4 = data[i++];
                if (b4 == 84)
                    return sb.toString().getBytes("iso8859-1");
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1)
                break;
            sb.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return sb.toString().getBytes("iso8859-1");
    }

    public static void main(String[] args) throws Exception {
        String s = "12306";
        String ss = encode(s.getBytes());
        System.out.println("encode 12306 : " + ss);
        System.out.println("decode 12306 : " + new String(decode(ss)));
    }
}
