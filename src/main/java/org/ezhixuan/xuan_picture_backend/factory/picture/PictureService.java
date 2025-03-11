package org.ezhixuan.xuan_picture_backend.factory.picture;

import java.io.IOException;
import java.io.InputStream;

import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.UploadModelEnum;
import org.springframework.web.multipart.MultipartFile;

import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * 上传接口
 *
 * @author ezhixuan
 */
public interface PictureService {

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
     * @param fileName 文件名
     * @return 返回url
     * @throws IOException 文件转换异常
     * @throws UnirestException 网络请求异常
     */
    String doUpload(MultipartFile multipartFile, String fileName) throws IOException, UnirestException;

    /**
     * 下载文件
     *
     * @author Ezhixuan
     * @param urlStr
     * @return 文件流
     */
    InputStream doDownload(String urlStr);

}
