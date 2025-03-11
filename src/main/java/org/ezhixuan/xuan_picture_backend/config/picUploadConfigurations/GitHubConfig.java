package org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author ezhixuan
 */
@ConfigurationProperties(prefix = "github")
@Configuration
@Data
public class GitHubConfig {

    /**
     * 用户名/仓库名
     */
    private String repo;

    /**
     * 分支
     */
    private String branch;

    /**
     * token
     */
    private String token;
}
