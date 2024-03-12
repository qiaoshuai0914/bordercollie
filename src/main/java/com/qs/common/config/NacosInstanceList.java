package com.qs.common.config;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qiaoshuai
 * @date 2024/2/27
 */
@Component
public class NacosInstanceList {

    private static final Logger logger = LoggerFactory.getLogger(NacosInstanceList.class);
    @Autowired
    ServicePropertyConf servicePropertyConf;

    @Autowired
    NacosHttpRequestUtils request;

    /**
     * 从Nacos服务中获取给定服务名称和标签的实例列表
     *
     * @param serviceName 服务名称
     * @param headerTag   服务标签
     * @return 包含所有符合条件的实例URL的列表
     * @throws Exception 当发生异常时抛出
     */
    public List<String> getInstanceList(String serviceName, String headerTag) throws Exception {
        List<String> resultList = new ArrayList<>();
        List<String> tagResultList = new ArrayList<>();
        try {
            String token = getToken(servicePropertyConf.getNacosUrl() + servicePropertyConf.getNacosLogin(), servicePropertyConf.getUsername(), servicePropertyConf.getPassword());
            StringBuilder sb = new StringBuilder();
            sb.append("accessToken=").append(token);
            sb.append("&serviceName=").append(serviceName);
            sb.append("&healthyOnly=").append(true);
            sb.append("&namespaceId=").append(servicePropertyConf.getNamespaceId());

            String value = sendGet(servicePropertyConf.getNacosUrl() + servicePropertyConf.getNacosList(), sb.toString());
            JSONObject jsonObject = JSONObject.parseObject(value);
            JSONArray jsonArray = jsonObject.getJSONArray("hosts");

            for (Object obj : jsonArray) {
                JSONObject hostJson = (JSONObject) obj;
                String ip = hostJson.getString("ip");
                int port = hostJson.getIntValue("port");
                resultList.add("http://" + ip + ":" + port);
                JSONObject metadata = hostJson.getJSONObject("metadata");
                if (metadata != null) {
                    String nacosTag = metadata.getString("tag");
                    if (isNotBlank(headerTag) && isNotBlank(nacosTag) && headerTag.equals(nacosTag)) {
                        tagResultList.add("http://" + ip + ":" + port);
                        return tagResultList;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取nacos列表失败", e);
            throw e; // rethrow the exception or wrap it in a custom exception
        }
        return resultList;
    }
    public boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }
    public  boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen == 0) {
            return true;
        } else {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }
    public  int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }
    /**
     * 获取访问令牌
     *
     * @param url     Nacos服务地址
     * @param user    用户名
     * @param password 密码
     * @return 返回访问令牌
     * @throws Exception 抛出异常
     */
    public String getToken(String url, String user, String password) throws Exception {
        String token = sendPost(url, "username=" + user + "&password=" + password);
        return JSONObject.parseObject(token).getString("accessToken");
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     * @throws Exception
     */
    public String sendGet(String url, String param) throws Exception {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            if (isNotBlank(param)) {
                urlNameString = urlNameString + "?" + param;
            }
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();

            // 建立实际的连接
            connection.connect();
            connection.setConnectTimeout(5000);
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            throw e;
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }


    public String sendPost(String url, String param) throws Exception {
        BufferedWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.5) " + "Gecko/2008120122 Firefox/3.0.5");
            if (param != null) {
                conn.setRequestProperty("Content-Length", String.valueOf(param.length()));
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestMethod("POST");
            conn.connect();

            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"));

            out.write(param == null ? "" : param);

            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result = result + line + "\n";
            }
        } catch (Exception e) {
            logger.info("HttpRequestUtils sendPost() error:");
            e.printStackTrace();
            throw e;
        } finally {
            close(out, in);
        }
        return result;
    }

    private void close(Closeable out, Closeable in) throws IOException {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            logger.error("HttpRequestUtils close() error:", ex);
        }
    }

}
