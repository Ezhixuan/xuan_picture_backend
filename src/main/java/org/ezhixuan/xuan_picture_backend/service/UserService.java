package org.ezhixuan.xuan_picture_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.ezhixuan.xuan_picture_backend.model.dto.user.UserLoginRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.user.UserQueryRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.user.UserRegisterRequest;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.ezhixuan.xuan_picture_backend.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ezhixuan
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-03-10 16:06:20
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerRequest 用户注册请求
     * @return 用户id
     * @author Ezhixuan
     */
    long userRegister(UserRegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param loginRequest 用户登录请求
     * @param request      请求
     * @return 脱敏后的用户信息
     * @author Ezhixuan
     */
    UserVO userLogin(UserLoginRequest loginRequest, HttpServletRequest request);

    /**
     * 退出登录
     * @author Ezhixuan
     * @param request 请求
     */
    void userLogout(HttpServletRequest request);

    /**
     * 获取加密后的密码
     *
     * @param userPassword 加密前密码
     * @return 加密后的密码
     * @author Ezhixuan
     */
    String getEncryptedPassword(String userPassword);

    /**
     * user对象信息脱敏
     *
     * @param user user对象
     * @return 脱敏后的user信息
     * @author Ezhixuan
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户脱敏信息列表
     * @author Ezhixuan
     * @param userList 用户列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取登录用户信息
     *
     * @param request 请求
     * @return 用户信息
     * @author Ezhixuan
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取queryWrapper
     * @author Ezhixuan
     * @param queryRequest 查询请求
     * @return QueryWrapper<User>
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest queryRequest);

    /**
     * 判断是否为管理员
     * @author Ezhixuan
     * @param user 用户
     * @return 是/否
     */
    boolean isAdmin(User user);
}
