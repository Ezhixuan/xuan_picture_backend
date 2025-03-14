package org.ezhixuan.xuan_picture_backend.controller;

import static org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil.getContentType;
import static org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil.reName;

import java.io.InputStream;
import java.util.Objects;

import io.swagger.annotations.ApiOperation;
import org.ezhixuan.xuan_picture_backend.annotation.AuthCheck;
import org.ezhixuan.xuan_picture_backend.common.BaseResponse;
import org.ezhixuan.xuan_picture_backend.common.ResultUtils;
import org.ezhixuan.xuan_picture_backend.constant.UserConstant;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureManager;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadResult;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.vo.picture.PictureVO;
import org.ezhixuan.xuan_picture_backend.service.PictureService;
import org.ezhixuan.xuan_picture_backend.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 图片控制器
 *
 * @author ezhixuan
 */
@RequiredArgsConstructor
@RequestMapping("/pic")
@RestController
public class PictureController {

    private final PictureService pictureService;
    private final UserService userService;

    @ApiOperation("图片上传")
    @SneakyThrows
    @PostMapping("/v1/upload")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<PictureUploadResult> upload(@RequestPart("file") MultipartFile file, boolean notReName) {
        PictureUploadResult result = pictureService.getPictureFactory().getInstance()
                .doUpload(file, "public/", notReName);
        return ResultUtils.success(result);
    }

    @ApiOperation("图片上传")
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<PictureVO> upload(@RequestPart("file") MultipartFile file, PictureUploadRequest uploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(pictureService.upload(file, uploadRequest, loginUser));
    }

    @ApiOperation("图片下载")
    @SneakyThrows
    @GetMapping("/download")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public void download(@RequestParam("url") String url, HttpServletResponse response) {
        String suffix = url.contains(".") ? url.substring(url.lastIndexOf('.') + 1) : "jpg";
        String fileName = reName("pic_." + suffix);
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        try(InputStream inputStream = pictureService.getPictureFactory().getInstance().doDownload(url);
            ServletOutputStream outputStream = response.getOutputStream()) {
            byte[] bytes = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
}
