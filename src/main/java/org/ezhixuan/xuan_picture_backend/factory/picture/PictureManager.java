package org.ezhixuan.xuan_picture_backend.factory.picture;

import java.io.IOException;
import java.io.InputStream;

import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.UploadModelEnum;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadResult;
import org.springframework.web.multipart.MultipartFile;

import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * 上传接口
 *
 * @author ezhixuan
 */
public interface PictureManager {

    /**
     * 获取上传模型
     *
     * @author Ezhixuan
     * @return 实现者所属上传模型
     */
    UploadModelEnum getUploadModelEnum();

    /**
     * 上传图片
     * @author Ezhixuan
     * @param inputStream 文件流
     * @param targetPath 目标路径
     * @param fileName 文件名
     * @return url
     */
    String doUpload(InputStream inputStream, String targetPath, String fileName) throws IOException, UnirestException;

    /**
     * 下载文件
     *
     * @author Ezhixuan
     * @param urlStr
     * @return 文件流
     */
    InputStream doDownload(String urlStr);

}
