package org.ezhixuan.xuan_picture_backend.factory.picture;

import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.UploadModelEnum;
import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.UseUploadTypeConfig;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 图片工厂
 *
 * @author ezhixuan
 */
@Component
@RequiredArgsConstructor
public class PictureFactory {

    private final UseUploadTypeConfig typeConfig;
    private final List<PictureService> serviceList;

    private static UploadModelEnum type;

    @PostConstruct
    public void init() {
        type = UploadModelEnum.getUploadModelEnum(typeConfig.getType());
    }

    public PictureService getInstance() {
        return serviceList.stream().filter(service -> Objects.equals(service.getUploadModelEnum(), type)).findAny()
            .orElseThrow(() -> new RuntimeException(ErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
