package com.hikcreate.com.gitlab.code.stat.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GitLabConfigBean 配置类
 *
 * @author: zhaodongxx
 * @date: 2020-02-14
 */
@ToString
@Data
@Component
@ConfigurationProperties(prefix = "gitlab")
public class GitLabConfigBean {

    /**
     * GitLab 地址
     */
    private String host;

    /**
     * GitLab access Token
     */
    private String token;
}