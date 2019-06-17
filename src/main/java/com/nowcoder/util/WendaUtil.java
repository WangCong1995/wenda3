package com.nowcoder.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Map;

/**
 * Created by nowcoder on 2016/7/3.
 */
public class WendaUtil {
    private static final Logger logger = LoggerFactory.getLogger(WendaUtil.class);

    public static  int ANONYMOUS_USERID=3;


    /**
     *  封装一个生成 Json串的工具。默认情况是没有“msg”的
     */
    public static String getJSONString(int code){
        JSONObject json=new JSONObject();
        json.put("code",code);  //像map一样，put进去即可
        return json.toJSONString();//json对象 直接转换成了string
    }

    /**
     *  封装一个生成 Json串的工具
     */
    public static String getJSONString(int code,String msg){
        JSONObject json=new JSONObject();
        json.put("code",code);  //像map一样，put进去即可
        json.put("msg",msg);
        return json.toJSONString();//json对象 直接转换成了string
    }

    /* 将字符串用MD5算法加密 */
    public static String MD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            logger.error("生成MD5失败", e);
            return null;
        }
    }
}
