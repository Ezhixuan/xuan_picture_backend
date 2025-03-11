package org.ezhixuan.xuan_picture_backend.controller;

import static org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil.getContentType;
import static org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil.reName;

import java.io.InputStream;
import java.util.Objects;

import org.ezhixuan.xuan_picture_backend.annotation.AuthCheck;
import org.ezhixuan.xuan_picture_backend.constant.UserConstant;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.servlet.ServletOutputStream;
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

    private final PictureFactory pictureFactory;

    @SneakyThrows
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public String upload(@RequestPart("file") MultipartFile file, boolean reName) {
        return pictureFactory.getInstance()
                .doUpload(file, reName ? reName(Objects.requireNonNull(file.getOriginalFilename()), file.getContentType())
                        : file.getOriginalFilename());
    }

    @GetMapping("/download")
    @SneakyThrows
    public void download(@RequestParam("url") String url, HttpServletResponse response) {
        String suffix = url.contains(".") ? url.substring(url.lastIndexOf('.') + 1) : "jpg";
        String fileName = reName("pic_." + suffix, getContentType(url));
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        try(InputStream inputStream = pictureFactory.getInstance().doDownload(url);
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
