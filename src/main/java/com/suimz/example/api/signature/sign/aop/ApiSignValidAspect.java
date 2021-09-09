package com.suimz.example.api.signature.sign.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.suimz.example.api.signature.sign.core.ApiSignNonceStrCache;
import com.suimz.example.api.signature.sign.core.ApiSignProperties;
import com.suimz.example.api.signature.sign.core.ApiSignValidException;
import com.suimz.example.api.signature.sign.filter.ApiSignRequestWrapper;
import com.suimz.example.api.signature.sign.util.HttpRequestUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.SortedMap;

/**
 * AOP - 接口签名校验
 */
@Aspect
@Component
public class ApiSignValidAspect {

    private final Logger log = LoggerFactory.getLogger(ApiSignValidAspect.class);

    @Autowired
    private ApiSignProperties properties;

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.suimz.example.api.signature.sign.aop.ApiSignValid)")
    public void pointCut() { }

    /**
     * 方法运行之前调用
     * @param joinPoint
     */
    @Before("pointCut()")
    public void before(JoinPoint joinPoint) throws ApiSignValidException {
        try {
            HttpServletRequest request = getHttpRequest();
            // 从header取出签名相关参数
            String appIdStr = request.getHeader(properties.getHeaderAppId());
            String sign = request.getHeader(properties.getHeaderSign());
            String nonce = request.getHeader(properties.getHeaderNonce());
            String timestampStr = request.getHeader(properties.getHeaderTimestamp());
            log.info("【签名校验】 appId:{}, nonce:{}, timestamp:{}, sign:{}", appIdStr, nonce, timestampStr, sign);

            // 校验参数
            if (ObjectUtil.isEmpty(appIdStr)) throw new Exception("invalid appId");
            if (ObjectUtil.isEmpty(timestampStr) || ObjectUtil.isEmpty(nonce) || nonce.length() != properties.getNonceLen() || ObjectUtil.isEmpty(sign)) throw new ApiSignValidException("illegal parameter");
            ApiSignProperties.App app = properties.getAppById(Integer.valueOf(appIdStr));
            if (app == null) throw new ApiSignValidException("invalid appId");

            // 判断过期失效 - 防止重放攻击
            long timestamp = Long.valueOf(timestampStr);
            long now = System.currentTimeMillis() / 1000;
            if (now - timestamp > properties.getExpireTime()) {
                throw new ApiSignValidException("expire time");
            }

            // 判断随机字符串 - 防止重放攻击
            ApiSignNonceStrCache nonceStrCache = ApiSignNonceStrCache.getInstance(properties.getExpireTime());
            if (nonceStrCache.isExist(app.getId(), nonce)) throw new Exception();
            nonceStrCache.put(app.getId(), nonce); // 缓存随机字符串

            // 签名校验
            boolean isSigned = verifySign(app, sign, timestamp, nonce, request);
            if (!isSigned) throw new Exception();
        } catch (ApiSignValidException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ApiSignValidException("signature check failure");
        }
    }

    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes.getRequest();
    }

    /**
     * 校验签名
     * @param app
     * @param signStr
     * @param timestamp
     * @param nonce
     * @param request
     * @return
     */
    private boolean verifySign(ApiSignProperties.App app, String signStr, long timestamp, String nonce, HttpServletRequest request) {
        try {
            ApiSignRequestWrapper requestWrapper = new ApiSignRequestWrapper(request);
            // 获取全部参数(包括 URL 和 body 上的)，默认按ASCII对Key进行排序
            SortedMap<String, String> allParams = HttpRequestUtil.getAllParams(requestWrapper);
            // 加入签名信息
            allParams.put("appId", app.getStrId());
            allParams.put("timestamp", String.valueOf(timestamp));
            allParams.put("nonce", nonce);
            // 将参数转为字符串，格式：[key1][value1][key2][value2]...[secret]
            StringBuilder sbd = new StringBuilder("");
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                // 排除空val的参数
                if (ObjectUtil.isEmpty(entry.getValue())) continue;
                sbd.append(entry.getKey()).append(entry.getValue());
            }
            String params = sbd.append(app.getSecret()).toString();

            // 服务器对参数进行签名
            String sign = sign(params);
            // 前端传过来的sign作比较
            return sign.equals(signStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 签名 - 当前使用的 SHA256 摘要算法
     * @param str
     * @return
     */
    private String sign(String str) {
        return SecureUtil.sha256(str).toUpperCase();
    }

}
