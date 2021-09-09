package com.suimz.example.api.signature.sign.util;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Http工具类
 */
public class HttpRequestUtil {

    /**
     * 将 URL 的参数 和 body 参数合并
     * @param request
     */
    public static SortedMap<String, String> getAllParams(HttpServletRequest request) throws IOException {
        SortedMap<String, String> result = new TreeMap<>();
        // 获取 URL 上的参数
        Map<String, Object> urlParams = getUrlParams(request);
        for (Map.Entry entry : urlParams.entrySet()) {
            result.put((String) entry.getKey(), String.valueOf(entry.getValue()));
        }
        // 获取 body 中的参数
        Map<String, String> bodyParams = new HashMap<>();
        if (!HttpMethod.GET.name().equals(request.getMethod())) {
            bodyParams = geBodyParam(request);
        }
        // 将 URL 的参数和 body 参数进行合并
        if (ObjectUtil.isNotEmpty(bodyParams)) {
            for (Map.Entry entry : bodyParams.entrySet()) {
                result.put((String) entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * 获取 Body 中的参数（json），转换成 Map
     * @param request
     */
    public static Map<String, Object> geBodyParam(final HttpServletRequest request) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String str = "";
        StringBuilder wholeStr = new StringBuilder();
        // 一行一行的读取body体里面的内容；
        while ((str = reader.readLine()) != null) {
            wholeStr.append(str);
        }
        // 转化成json对象
        return JSONObject.parseObject(wholeStr.toString(), Map.class);
    }

    /**
     * 获取 URL 上的参数，转换成 Map
     * @author show
     * @param request
     */
    public static Map<String, Object> getUrlParams(HttpServletRequest request) {
        String param = "";
        try {
            param = URLDecoder.decode(request.getQueryString(), "utf-8");
        } catch (Exception e) { }
        Map<String, Object> result = new HashMap<>();
        if (ObjectUtil.isNotEmpty(param)) {
            String[] params = param.split("&");
            for (String s : params) {
                int index = s.indexOf("=");
                result.put(s.substring(0, index), s.substring(index + 1));
            }
        }
        return result;
    }
}
