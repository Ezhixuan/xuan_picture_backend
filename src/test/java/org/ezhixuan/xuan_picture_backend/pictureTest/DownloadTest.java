package org.ezhixuan.xuan_picture_backend.pictureTest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadTest {

    @Test
    void testDownload() {
        System.out.println(getImageByUrl());
    }

    public String getImageByUrl() {
        String imageUrl =
            "https://cdn.jsdelivr.net/gh/Ezhixuan/myPic@main/CBB9949A311AB44A7961E2C983C613F31741702759687.gif";

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 获取输入流并写入响应
                try (InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    return Base64.getEncoder().encodeToString(outputStream.toByteArray());
                }
            }
        } catch (Exception e) {
            log.error("下载失败：" + e.getMessage());
        }
        return null;
    }

}
