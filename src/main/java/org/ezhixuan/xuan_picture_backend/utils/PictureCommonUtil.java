package org.ezhixuan.xuan_picture_backend.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadResult;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

/**
 * 图片处理工具
 *
 * @author ezhixuan
 */
public class PictureCommonUtil {

    /**
     * 重命名
     *
     * @author Ezhixuan
     * @param fileName 文件名
     * @return 新文件名
     */
    public static String reName(String fileName) {
        ThrowUtils.throwIf(!StringUtils.hasText(fileName), ErrorCode.PARAMS_ERROR, "文件名不能为空");
        String ext = FileUtil.getSuffix(fileName);

        // 生成唯一标识
        String date = DateUtil.formatDate(new Date());
        String uniqueId = UUID.randomUUID().toString();

        // 保留部分原始名称+唯一标识+扩展名
        return String.format("%s_%s.%s", date, uniqueId, ext);
    }

    /**
     * 通过url获取ContentType
     *
     * @author Ezhixuan
     * @param urlStr
     * @return String
     */
    public static String getContentType(String urlStr) {
        String[] commonTypes = {"gif", "image/gif", "jpg", "image/jpeg", "png", "image/png",};

        // 提取后缀（不区分大小写）
        String suffix = urlStr.substring(urlStr.lastIndexOf('.') + 1).toLowerCase();

        // 遍历映射表匹配类型
        for (int i = 0; i < commonTypes.length; i += 2) {
            if (commonTypes[i].equals(suffix)) {
                return commonTypes[i + 1];
            }
        }
        return commonTypes[commonTypes.length - 1];
    }

    /**
     * 将mutipartFile转为base64格式
     *
     * @author Ezhixuan
     * @param multipartFile 文件
     * @return base64格式内容
     */
    public static String toBase64Code(MultipartFile multipartFile) throws IOException {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * 检验文件是否合规
     * @author Ezhixuan
     * @param file 文件
     */
    public static void validateFile(MultipartFile file) {
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!StringUtils.hasText(fileSuffix)) {
            throw new RuntimeException("文件格式错误");
        }
        final String[] IMAGE_SUFFIX_ARRAY = {"png", "jpg", "jpeg", "webp"};
        for (String suffix : IMAGE_SUFFIX_ARRAY) {
            if (fileSuffix.equals(suffix)) {
                return;
            }
        }
        throw new RuntimeException("不支持的文件格式");
    }

    /**
     * 解析图片信息
     *
     * @author Ezhixuan
     * @param file 图片文件
     * @param notReName 是否重命名 默认重命名
     * @return 除url外的图片信息
     */
    public static PictureUploadResult processImage(MultipartFile file, boolean notReName) throws IOException {
        validateFile(file);
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        long sizeKB = file.getSize() / 1024;
        PictureUploadResult.PictureUploadResultBuilder builder = PictureUploadResult.builder()
            .name(notReName ? file.getOriginalFilename() : reName(file.getOriginalFilename())).picSize(sizeKB)
            .picFormat(fileSuffix);
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            ThrowUtils.throwIf(image == null, ErrorCode.PARAMS_ERROR, "文件非图片格式");
            int width = image.getWidth();
            int height = image.getHeight();
            double aspectRatio = (double)width / height;
            return builder.picWidth(width).picHeight(height).picScale(aspectRatio).build();
        }
    }
}
