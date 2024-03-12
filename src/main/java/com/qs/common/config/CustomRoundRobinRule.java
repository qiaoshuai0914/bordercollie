package com.qs.common.config;

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class CustomRoundRobinRule extends AbstractLoadBalancerRule {

    private static final Logger logger = LoggerFactory.getLogger(CustomRoundRobinRule.class);
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object o) {
        List<Server> servers = this.getLoadBalancer().getReachableServers();
        if (servers == null || servers.isEmpty()) {
            logger.warn("No reachable servers found.");
            // 这里可以根据实际需求抛出异常或返回null
            return null;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            logger.warn("Not running in a Servlet environment. Cannot retrieve HttpServletRequest.");
            // 这里可以根据实际需求进行处理，比如返回默认服务器或抛出异常
            return servers.get(0);
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String headerTag = request.getHeader("headerTag");
        logger.info("headerTag:{}", headerTag);
        logger.info("all servers:{}", servers.size());

        for (Server server : servers) {
            if (!(server instanceof NacosServer)) {
                throw new IllegalArgumentException("参数非法，不是NacosServer实例！");
            }
            NacosServer nacosServer = (NacosServer) server;
            Instance instance = nacosServer.getInstance();
            Map<String, String> map = instance.getMetadata();
            if (map != null && map.containsKey("tag") && StringUtils.isNotBlank(map.get("tag"))) {
                String nacosTag = map.get("tag");
                if (nacosTag.equals(headerTag)) {
                    logger.info("Matched headerTag:{} with nacosTag:{}", headerTag, nacosTag);
                    return server;
                }
            }
        }
        logger.info("No matching server found. Returning the first server.");
        Server res = RandomListElement.getRandomElement(servers);
        return res;
    }
}
