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
     * 上传文件
     *
     * @author Ezhixuan
     * @param multipartFile 文件
     * @param targetPath 目标路径
     * @param notReName 是否重命名 默认重命名
     * @return 返回url
     * @throws IOException 文件转换异常
     * @throws UnirestException 网络请求异常
     */
    PictureUploadResult doUpload(MultipartFile multipartFile, String targetPath, boolean notReName) throws IOException, UnirestException;

    /**
     * 下载文件
     *
     * @author Ezhixuan
     * @param urlStr
     * @return 文件流
     */
    InputStream doDownload(String urlStr);

}
