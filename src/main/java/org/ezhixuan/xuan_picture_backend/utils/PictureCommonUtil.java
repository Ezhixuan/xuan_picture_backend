package org.ezhixuan.xuan_picture_backend.utils;

import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 图片处理工具
 * @author ezhixuan
 */
public class PictureCommonUtil {

    /**
     * 重命名
     * @author Ezhixuan
     * @param fileName 文件名
     * @param contentType 文件类型
     * @return 新文件名
     */
    public static String reName(String fileName, String contentType) {
        String cleanName = fileName.replaceAll("[\\\\/]", "_");
        cleanName = cleanName.replaceAll("[^a-zA-Z0-9_.-]", "_");

        // 提取扩展名
        String ext = "";
        int lastDotIndex = cleanName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            ext = cleanName.substring(lastDotIndex);
            cleanName = cleanName.substring(0, lastDotIndex);
        } else if (StringUtils.hasText(contentType)) {
            // 若无扩展名，尝试根据contentType补充
            ext = MimeTypeUtils.parseMimeType(contentType).getSubtypeSuffix();
            if (ext != null)
                ext = "." + ext;
        }

        // 生成唯一标识
        String uniqueId = UUID.randomUUID().toString().substring(0, 6) + "-" + System.currentTimeMillis();

        // 保留部分原始名称+唯一标识+扩展名
        return String.format("%s_%s%s", truncate(cleanName, 20), uniqueId, ext);
    }

    /**
     * 截断过长文件名
     *
     * @author Ezhixuan
     * @param input 截断前文件名
     * @param maxLength 最大长度
     * @return 截断后的文件名
     */
    public static String truncate(String input, int maxLength) {
        return input.length() > maxLength ? input.substring(0, maxLength) : input;
    }

    /**
     * 通过url获取ContentType
     * @author Ezhixuan
     * @param urlStr
     * @return String
     */
    public static String getContentType(String urlStr) {
        String[] commonTypes = {
                "gif", "image/gif",
                "jpg", "image/jpeg",
                "png", "image/png",
        };

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
}
