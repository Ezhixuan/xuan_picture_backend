package org.ezhixuan.xuan_picture_backend.service;

import org.ezhixuan.xuan_picture_backend.factory.picture.PictureFactory;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadRequest;
import org.ezhixuan.xuan_picture_backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.vo.picture.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author ezhixuan
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-12 15:28:33
*/
public interface PictureService extends IService<Picture> {

    /**
     * 获取图片工厂
     * @author Ezhixuan
     * @return PictureFactory
     */
    PictureFactory getPictureFactory();

    /**
     * 图片上传
     * @author Ezhixuan
     * @param file MultipartFile
     * @param request 请求参数
     * @param loginUser 用户信息
     * @return 图片脱敏后信息
     */
    PictureVO upload(MultipartFile file, PictureUploadRequest request, User loginUser);
}
