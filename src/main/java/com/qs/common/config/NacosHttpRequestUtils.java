package com.qs.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author zengfeiyang
 * @date 2023-03-30 17:56
 * @description 不要在使用HttpRequestUtils 引入是为了兼容旧的，新的模块不要在使用
 */
@Service
public class NacosHttpRequestUtils {
    private static Logger logger = LoggerFactory.getLogger(NacosHttpRequestUtils.class);

    @Autowired
    NacosInstanceList nacosInstanceList;


    public String sendGet(String projectName, String api, String param) throws Exception {

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String headerTag = request.getHeader("headerTag");
        logger.info("request headerTag:" + headerTag);

        List<String> urls = nacosInstanceList.getInstanceList(projectName, headerTag);
        String host = RandomListElement.getRandomElement(urls);
        String url = host + api;

        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            if (param != null && !param.isEmpty()) {
                urlNameString = urlNameString + "?" + param;
            }
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("headerTag", headerTag);
            // 建立实际的连接
            connection.connect();
            connection.setConnectTimeout(5000);
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error("发送GET请求出现异常！", e);
            throw e;
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                logger.error("close error", e2);
            }
        }
        return result;
    }
}
