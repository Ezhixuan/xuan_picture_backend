package org.ezhixuan.xuan_picture_backend.pictureTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.ezhixuan.xuan_picture_backend.config.picUploadConfigurations.GitHubConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@SpringBootTest
public class UploadTest {

    // test github upload
    private static String REPO; // 格式：用户名/仓库名
    private static String BRANCH;
    private static String TOKEN;

    @PostConstruct
    void init() {
        REPO = gitHubConfig.getRepo();
        BRANCH = gitHubConfig.getBranch();
        TOKEN = gitHubConfig.getToken();
    }

    @Resource
    private GitHubConfig gitHubConfig;

    public static String uploadImage(byte[] imageBytes, String filename) throws Exception {
        String apiUrl = "https://api.github.com/repos/" + REPO + "/contents/" + filename;
        String encodedContent = Base64.getEncoder().encodeToString(imageBytes);

        JSONObject body = new JSONObject();
        body.put("message", "Upload image via Java");
        body.put("content", encodedContent);
        body.put("branch", BRANCH);

        HttpResponse<String> response = Unirest.put(apiUrl).header("Authorization", "token " + TOKEN)
            .header("Accept", "application/vnd.github.v3+json").header("Content-Type", "application/json")
            .body(body.toString()).asString();

        if (response.getStatus() == 201) {
            JSONObject jsonResponse = JSONUtil.parseObj(response.getBody());
            String downloadUrl = jsonResponse.getJSONObject("content").getStr("download_url");
            return "https://cdn.jsdelivr.net/gh/" + REPO + "@" + BRANCH + "/" + filename; // 使用CDN加速
        } else {
            throw new RuntimeException("上传失败: " + response.getBody());
        }
    }

    @Test
    void uploadGitHub() {
        try {
            byte[] imageData = Files.readAllBytes(Paths.get("src/main/resources/test.jpg"));
            String url = uploadImage(imageData, "images/test.jpg");
            System.out.println("图片URL: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void readConfig() {
        // 读取resource下的图片
        String repo = gitHubConfig.getRepo();
        String branch = gitHubConfig.getBranch();
        String token = gitHubConfig.getToken();
    }
}
