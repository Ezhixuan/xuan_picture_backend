package org.ezhixuan.xuan_picture_backend.factory.picture.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;

import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.GitHubConfig;
import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.UploadModelEnum;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;

/**
 * github 实现图片管理
 *
 * @author ezhixuan
 */
@Service
@RequiredArgsConstructor
public class GitHubPictureServiceImpl implements PictureService {

    private final GitHubConfig gitHubConfig;

    /**
     * 获取上传模型
     *
     * @return 实现者所属上传模型
     * @author Ezhixuan
     */
    @Override
    public UploadModelEnum getUploadModelEnum() {
        return UploadModelEnum.GIT_HUB;
    }

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @param fileName 文件名
     * @return 返回url
     * @author Ezhixuan
     * @throws IOException 文件转换异常
     * @throws UnirestException 网络请求异常
     */
    @Override
    public String doUpload(MultipartFile multipartFile, String fileName) throws IOException, UnirestException {
        ThrowUtils.throwIf(Objects.isNull(multipartFile), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(
            Objects.isNull(gitHubConfig) || Objects.isNull(gitHubConfig.getRepo())
                || Objects.isNull(gitHubConfig.getBranch()) || Objects.isNull(gitHubConfig.getToken()),
            ErrorCode.SYSTEM_ERROR, "检查github配置");
        // 转为byte数组
        return uploadImage(toBase64Code(multipartFile), fileName);
    }

    /**
     * 下载文件
     *
     * @author Ezhixuan
     * @param urlStr
     * @return 文件流
     */
    @Override
    public InputStream doDownload(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            ThrowUtils.throwIf(connection.getResponseCode() != HttpURLConnection.HTTP_OK, ErrorCode.OPERATION_ERROR,
                "下载失败，请检查网络");
            // 获取输入流并写入响应
            return connection.getInputStream();
        } catch (IOException e) {
            ThrowUtils.exception(ErrorCode.SYSTEM_ERROR.getCode(), "下载失败 url={}", urlStr);
        }
        return null;
    }

    /**
     * 将mutipartFile转为base64格式
     *
     * @author Ezhixuan
     * @param multipartFile 文件
     * @return base64格式内容
     */
    private String toBase64Code(MultipartFile multipartFile) throws IOException {
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
     * 内部上传逻辑
     *
     * @author Ezhixuan
     * @param base64Code base64编码图片
     * @param filename 文件名
     * @return url
     */
    private String uploadImage(String base64Code, String filename) throws UnirestException {
        String repo = gitHubConfig.getRepo();
        String branch = gitHubConfig.getBranch();
        String token = gitHubConfig.getToken();

        String apiUrl = "https://api.github.com/repos/" + repo + "/contents/" + filename;

        JSONObject body = new JSONObject();
        body.set("message", "Upload image via Java");
        body.set("content", base64Code);
        body.set("branch", branch);

        HttpResponse<String> response = Unirest.put(apiUrl).header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github.v3+json").header("Content-Type", "application/json")
            .body(body.toString()).asString();
        if (response.getStatus() != 201) {
            ThrowUtils.throwIf(response.getBody().contains("Invalid request"), ErrorCode.PARAMS_ERROR, "文件名已存在");
            ThrowUtils.exception(ErrorCode.SYSTEM_ERROR.getCode(), "上传失败 response={}", response.toString());
        }
        JSONObject jsonResponse = JSONUtil.parseObj(response.getBody());
        String downloadUrl = jsonResponse.getJSONObject("content").getStr("download_url");
        // 使用CDN加速
        return "https://cdn.jsdelivr.net/gh/" + repo + "@" + branch + "/" + filename;
    }
}
