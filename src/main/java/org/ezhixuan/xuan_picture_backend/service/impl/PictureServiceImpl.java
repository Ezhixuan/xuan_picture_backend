package org.ezhixuan.xuan_picture_backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureFactory;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadResult;
import org.ezhixuan.xuan_picture_backend.model.entity.Picture;
import org.ezhixuan.xuan_picture_backend.mapper.PictureMapper;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.vo.picture.PictureVO;
import org.ezhixuan.xuan_picture_backend.service.PictureService;
import org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * @author ezhixuan
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-03-12 15:28:33
 */
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    private final PictureFactory factory;

    /**
     * 获取图片工厂
     *
     * @return PictureFactory
     * @author Ezhixuan
     */
    @Override
    public PictureFactory getPictureFactory() {
        return factory;
    }

    /**
     * 图片上传
     *
     * @author Ezhixuan
     * @param file MultipartFile
     * @param request 请求参数
     * @param loginUser 用户信息
     * @return 图片脱敏后信息
     */
    @Override
    public PictureVO upload(MultipartFile file, PictureUploadRequest request, User loginUser) {
        ThrowUtils.throwIf(Objects.isNull(loginUser), ErrorCode.NO_AUTH_ERROR);
        // 校验文件
        Long picId = null;
        if (Objects.nonNull(request)) {
            picId = request.getId();
        }
        if (Objects.nonNull(picId)) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, picId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
        }
        String targetPath = String.format("public/%s/", loginUser.getId());
        try {
            PictureUploadResult result = factory.getInstance().doUpload(file, targetPath, request.isNotReName());
            Picture picture = BeanUtil.copyProperties(result, Picture.class);
            picture.setUserId(loginUser.getId());
            if (Objects.nonNull(picId)) {
                picture.setId(picId);
                picture.setEditTime(new Date());
            }
            boolean resultSave = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!resultSave, ErrorCode.OPERATION_ERROR, "图片上传失败");
            return PictureVO.objToVo(picture);
        } catch (IOException | UnirestException e) {
            throw new RuntimeException(e);
        }
    }
}
