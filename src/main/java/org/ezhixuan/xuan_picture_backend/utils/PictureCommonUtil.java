package org.ezhixuan.xuan_picture_backend.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import org.ezhixuan.xuan_picture_backend.exception.BusinessException;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadRequest;
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
        return reName(fileName, null);
    }

    public static String reName(String fileName, String contentType) {
        ThrowUtils.throwIf(!StringUtils.hasText(fileName), ErrorCode.PARAMS_ERROR, "文件名不能为空");
        String ext = "";
        if (StrUtil.isNotBlank(contentType)) {
            ext = getSuffixByContentType(contentType);
        }else {
            ext = FileUtil.getSuffix(fileName);
        }

        if (StrUtil.isBlank(ext)) {
            return fileName;
        }
        // 生成唯一标识
        String date = DateUtil.formatDate(new Date());
        String uniqueId = UUID.randomUUID().toString();

        // 保留部分原始名称+唯一标识+扩展名
        return String.format("%s_%s.%s", date, uniqueId, ext);
    }

    public static String getSuffixByContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            default:
                return "";
        }
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
    public static String toBase64Code(InputStream inputStream) {
        try (inputStream; ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检验文件是否合规
     * @author Ezhixuan
     * @param file 文件
     */
    public static void validatePicture(MultipartFile file) {
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
     * 检验url是否合规
     * @author Ezhixuan
     * @param fileUrl url
     */
    public static void validatePicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证 URL 格式
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long maxSize = 20 * 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > maxSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过 20M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }
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
        validatePicture(file);
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        long sizeKB = file.getSize();
        PictureUploadResult.PictureUploadResultBuilder builder = PictureUploadResult.builder()
            .name(notReName ? file.getOriginalFilename() : reName(file.getOriginalFilename())).picSize(sizeKB)
            .picFormat(fileSuffix);
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            ThrowUtils.throwIf(image == null, ErrorCode.PARAMS_ERROR, "文件非图片格式");
            int width = image.getWidth();
            int height = image.getHeight();
            int gcd = gcd(width, height);
            double aspectRatio = (double) (width / gcd) / (height / gcd);
            return builder.picWidth(width).picHeight(height).picScale(aspectRatio).build();
        }
    }

    public static String pathName(String targetPath, String fileName) {
        if (!targetPath.endsWith(File.separator)) {
            targetPath += File.separator;
        }
        return targetPath + fileName;
    }

    public static PictureUploadResult processImage(String fileUrl, boolean notReName) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        URL url;
        try {
            url = new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        String ext = FileUtil.extName(fileUrl);
        String contentType;
        PictureUploadResult.PictureUploadResultBuilder builder = PictureUploadResult.builder();
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
        InputStream inputStream = url.openStream()) {
            ThrowUtils.throwIf(Objects.isNull(response) || response.getStatus() != HttpStatus.HTTP_OK, ErrorCode.PARAMS_ERROR, "文件地址不正确" + url);
            contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                if (StrUtil.isBlank(ext) && notReName) {
                    // 如果包含类型，按照类型添加后缀
                    ext = contentType.substring(contentType.indexOf("/")+1);
                }
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            String contentLengthStr = response.header("Content-Length");
            long contentLength = 0L;
            if (StrUtil.isNotBlank(contentLengthStr)) {
                contentLength = Long.parseLong(contentLengthStr);
            } else {
                contentLength = (long) inputStream.available();
            }
            final long maxSize = 20 * 1024 * 1024L;
            builder.picSize(contentLength);
            ThrowUtils.throwIf(contentLength > maxSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过 20M");
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的图片格式");
            }
            int width = image.getWidth();
            int height = image.getHeight();
            int gcd = gcd(width, height);
            double aspectRatio = (double) (width / gcd) / (height / gcd);
            return builder
                    .picWidth(width)
                    .picHeight(height)
                    .picScale(aspectRatio)
                    .name(notReName ? FileUtil.mainName(fileUrl) + "." + ext : reName(FileUtil.mainName(fileUrl),contentType))
                    .picFormat(ext)
                    .build();
        }catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址不正确");
        }
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

}
