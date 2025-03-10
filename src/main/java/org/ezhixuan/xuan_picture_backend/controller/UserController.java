package org.ezhixuan.xuan_picture_backend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.ezhixuan.xuan_picture_backend.annotation.AuthCheck;
import org.ezhixuan.xuan_picture_backend.common.BaseResponse;
import org.ezhixuan.xuan_picture_backend.common.DeleteRequest;
import org.ezhixuan.xuan_picture_backend.common.ResultUtils;
import org.ezhixuan.xuan_picture_backend.constant.UserConstant;
import org.ezhixuan.xuan_picture_backend.exception.BusinessException;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.model.dto.user.*;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.vo.user.UserVO;
import org.ezhixuan.xuan_picture_backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * 用户接口
 * @author ezhixuan
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest registerRequest) {
        ThrowUtils.throwIf(Objects.isNull(registerRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userRegister(registerRequest));
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(Objects.isNull(loginRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userLogin(loginRequest, request));
    }

    @ApiOperation("用户登录信息")
    @PostMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getUserVO(userService.getLoginUser(request)));
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(true);
    }

    @ApiOperation("添加用户（管理员）")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userAddRequest, User.class);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptedPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    @ApiOperation("查询用户（管理员）")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    @ApiOperation("查询用户VO")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    @ApiOperation("删除用户（管理员）")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    @ApiOperation("更新用户（管理员）")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @ApiOperation("查询用户列表（管理员）")
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }


}
