package com.qs.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author qiaoshuai
 * @date 2024/2/29
 */
@Service
public class ServicePropertyConf {

    @Value("${spring.profiles:test}")
    private String porjectEnv;
    @Value("${spring.cloud.nacos.discovery.list}")
    private String nacosList ;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosUrl;
    @Value("${spring.cloud.nacos.discovery.login}")
    private String nacosLogin ;
    @Value("${spring.cloud.nacos.discovery.username}")
    private String username ;
    @Value("${spring.cloud.nacos.discovery.password}")
    private  String password;
    @Value("${spring.cloud.nacos.discovery.namespace}")
    private  String namespaceId ;

    public String getPorjectEnv() {
        return porjectEnv;
    }

    public void setPorjectEnv(String porjectEnv) {
        this.porjectEnv = porjectEnv;
    }

    public String getNacosList() {
        return nacosList;
    }

    public void setNacosList(String nacosList) {
        this.nacosList = nacosList;
    }

    public String getNacosUrl() {
        return nacosUrl;
    }

    public void setNacosUrl(String nacosUrl) {
        this.nacosUrl = nacosUrl;
    }

    public String getNacosLogin() {
        return nacosLogin;
    }

    public void setNacosLogin(String nacosLogin) {
        this.nacosLogin = nacosLogin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
