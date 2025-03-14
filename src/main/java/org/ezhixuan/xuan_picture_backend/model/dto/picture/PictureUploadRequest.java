package org.ezhixuan.xuan_picture_backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ezhixuan
 */
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 重命名
     */
    private boolean notReName;

    private static final long serialVersionUID = 1L;
}
