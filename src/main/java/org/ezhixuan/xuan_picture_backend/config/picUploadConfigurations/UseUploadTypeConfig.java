package org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author ezhixuan
 */
@ConfigurationProperties("upload")
@Configuration
@Data
public class UseUploadTypeConfig {

    /**
     * 上传模型
     */
    private String type;
}
