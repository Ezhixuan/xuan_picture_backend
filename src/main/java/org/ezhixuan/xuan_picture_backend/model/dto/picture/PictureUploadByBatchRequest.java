package org.ezhixuan.xuan_picture_backend.model.dto.picture;

import lombok.Data;

/**
 * 批量抓取请求
 * @author ezhixuan
 */
@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
}
