package org.ezhixuan.xuan_picture_backend.controller;

import org.ezhixuan.xuan_picture_backend.common.BaseResponse;
import org.ezhixuan.xuan_picture_backend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
