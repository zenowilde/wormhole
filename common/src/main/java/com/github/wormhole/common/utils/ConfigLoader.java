package com.github.wormhole.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.github.wormhole.common.config.ProxyServiceConfig;
import com.github.wormhole.common.config.ProxyServiceConfig.ServiceConfig;

public class ConfigLoader {
    public static ProxyServiceConfig load(String path) throws IOException {
        InputStream inputStream = new FileInputStream(path);
        byte[] bytes = IOUtils.toByteArray(inputStream);
        String s = new String(bytes, StandardCharsets.UTF_8);
        ProxyServiceConfig parse = parse(s);
        return parse;
    }

    public static ProxyServiceConfig parse(String data) throws IOException {
        // 禁用 fastjson ASM，兼容 GraalVM native-image
        ParserConfig.getGlobalInstance().setAsmEnable(false);

        String s = data;
        JSONObject jsonObject = JSON.parseObject(s);
        ProxyServiceConfig proxyServiceConfig = new ProxyServiceConfig();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (entry.getValue() instanceof  JSONObject) {
                JSONObject serviceJson = (JSONObject) entry.getValue();
                ProxyServiceConfig.ServiceConfig serviceConfig = new ProxyServiceConfig.ServiceConfig();
                serviceConfig.setIp(serviceJson.getString("ip"));
                serviceConfig.setPort(serviceJson.getInteger("port"));
                serviceConfig.setMappingPort(serviceJson.getInteger("mappingPort"));
                proxyServiceConfig.addConfig(entry.getKey(), serviceConfig);
            }
        }
        proxyServiceConfig.setServerHost(jsonObject.getString("serverHost"));
        proxyServiceConfig.setServerPort(jsonObject.getInteger("serverPort"));
        proxyServiceConfig.setDataTransPort(jsonObject.getInteger("dataTransPort"));
        proxyServiceConfig.setUsername(jsonObject.getString("username"));
        proxyServiceConfig.setPassword(jsonObject.getString("password"));
        return proxyServiceConfig;
    }

    public static String serialize(ProxyServiceConfig config) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("serverHost", config.getServerHost());
        jsonObject.put("serverPort", config.getServerPort());
        Map<String, ServiceConfig> map = config.getMap();
        map.forEach((k, v) -> {
            // 手动构建 JSON，避免 fastjson ASM 序列化
            JSONObject serviceJson = new JSONObject();
            serviceJson.put("ip", v.getIp());
            serviceJson.put("port", v.getPort());
            serviceJson.put("mappingPort", v.getMappingPort());
            jsonObject.put(k, serviceJson);
        });
        return jsonObject.toJSONString();
    }

}
