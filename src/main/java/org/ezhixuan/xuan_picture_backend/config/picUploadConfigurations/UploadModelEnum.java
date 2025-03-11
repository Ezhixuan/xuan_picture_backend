package org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations;

import lombok.Getter;

import java.util.Objects;

/**
 * @author ezhixuan
 */

@Getter
public enum UploadModelEnum {
    /**
     * github
     */
    GIT_HUB("github", "github");

    private final String desc;

    private final String value;

    UploadModelEnum(String desc, String value) {
        this.desc = desc;
        this.value = value;
    }

    /**
     * 获取当前使用的上传模型，默认使用github
     * @author Ezhixuan
     * @param model 上传模型
     * @return 当前使用的上传模型
     */
    public static UploadModelEnum getUploadModelEnum(String model) {
        for (UploadModelEnum uploadModelEnum :UploadModelEnum.values()) {
            if (Objects.equals(uploadModelEnum.getValue(), model)) {
                return uploadModelEnum;
            }
        }
        return GIT_HUB;
    }
}
