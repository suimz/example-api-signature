package com.suimz.example.api.signature.sign.core;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配置参数类 - 签名应用
 */
@Data
@Component
@ConfigurationProperties(prefix = "signature")
public class ApiSignProperties {

    /**
     * 自定义请求头的key
     */
    private String headerAppId = "_appId";
    private String headerSign = "_sign";
    private String headerNonce = "_nonce";
    private String headerTimestamp = "_timestamp";

    /**
     * 请求过期失效，单位：秒，默认5分钟
     */
    private long expireTime = 60 * 5;

    /**
     * 随机字符串长度
     */
    private int nonceLen = 18;

    /**
     * 支持的应用列表
     */
    private List<App> apps;

    @Data
    public static class App {
        private Integer id;
        private String secret;
        private String remark;
        public String getStrId() {
            return String.valueOf(id);
        }
    }

    /**
     * 获取app信息
     * @param appId
     * @return
     */
    public App getAppById(int appId) {
        if (ObjectUtil.isEmpty(apps)) return null;
        for (App app : apps) if (app.getId() == appId) return app;
        return null;
    }

}
