package com.github.ollgei.plugin.mybatisplus.sensitive.utils;

import java.util.Map;

import com.github.ollgei.base.commonj.utils.GsonHelper;

/**
 * 工具类
 * @author chenhaiyang
 */
public class JsonUtils {
    /**
     * 将json字符串转化为StringObject类型的map
     * @param jsonStr json字符串
     * @return map
     */
    public static Map<String,Object> parseToObjectMap(String jsonStr) {
        return GsonHelper.toMap(jsonStr);
    }

    /**
     * 将map转化为json字符串
     * @param params 参数集合
     * @return json
     */
    public static String parseMaptoJSONString(Map<String,Object> params){
        return GsonHelper.fromMap(params);
    }

}
