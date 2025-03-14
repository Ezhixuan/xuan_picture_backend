package org.ezhixuan.xuan_picture_backend.factory.picture.impl;

import static org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil.toBase64Code;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.GitHubConfig;
import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.UploadModelEnum;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.factory.picture.PictureManager;
import org.ezhixuan.xuan_picture_backend.model.dto.picture.PictureUploadResult;
import org.ezhixuan.xuan_picture_backend.utils.PictureCommonUtil;
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
public class GitHubPictureServiceImpl implements PictureManager {

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
     * @author Ezhixuan
     * @param multipartFile 文件
     * @param targetPath 目标路径
     * @param notReName 是否重命名 默认重命名
     * @return 返回url
     * @throws IOException 文件转换异常
     * @throws UnirestException 网络请求异常
     */
    @Override
    public PictureUploadResult doUpload(MultipartFile multipartFile, String targetPath, boolean notReName)
        throws IOException, UnirestException {
        ThrowUtils.throwIf(Objects.isNull(multipartFile), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(
            Objects.isNull(gitHubConfig) || Objects.isNull(gitHubConfig.getRepo())
                || Objects.isNull(gitHubConfig.getBranch()) || Objects.isNull(gitHubConfig.getToken()),
            ErrorCode.SYSTEM_ERROR, "检查github配置");
        PictureCommonUtil.validateFile(multipartFile);
        PictureUploadResult result = PictureCommonUtil.processImage(multipartFile, notReName);
        String url = uploadImage(toBase64Code(multipartFile), targetPath + result.getName());
        result.setUrl(url);
        return result;
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
