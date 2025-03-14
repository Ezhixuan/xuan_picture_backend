package org.ezhixuan.xuan_picture_backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.ezhixuan.xuan_picture_backend.exception.BusinessException;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.model.dto.user.UserLoginRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.user.UserQueryRequest;
import org.ezhixuan.xuan_picture_backend.model.dto.user.UserRegisterRequest;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.enums.UserRoleEnum;
import org.ezhixuan.xuan_picture_backend.model.vo.user.UserVO;
import org.ezhixuan.xuan_picture_backend.service.UserService;
import org.ezhixuan.xuan_picture_backend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ezhixuan.xuan_picture_backend.constant.UserConstant.USER_LOGIN_STATE;
import static org.ezhixuan.xuan_picture_backend.exception.ThrowUtils.throwIf;

/**
 * @author ezhixuan
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-03-10 16:06:20
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final String SALT = "EzhixuanSALT";

    /**
     * 用户注册
     *
     * @param registerRequest 用户注册请求
     * @return 用户id
     * @author Ezhixuan
     */
    @Override
    public long userRegister(UserRegisterRequest registerRequest) {
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        // 参数校验
        throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "请求参数为空");
        throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "帐号过短");
        throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        // 检查是否已存在
        long count = this.count(Wrappers.<User>lambdaQuery().eq(User::getUserAccount, userAccount));
        throwIf(count > 0, ErrorCode.PARAMS_ERROR, "帐号重复");
        // 密码加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        // 存入数据库
        User user = registerRequest.toUser(encryptedPassword, UserRoleEnum.USER);
        this.save(user);
        throwIf(Objects.isNull(user), ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        // 返回用户id
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param loginRequest 用户登录请求
     * @param request      请求
     * @return 脱敏后的用户信息
     * @author Ezhixuan
     */
    @Override
    public UserVO userLogin(UserLoginRequest loginRequest, HttpServletRequest request) {
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        // 数据校验
        throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR);
        throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "帐号错误");
        throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        // 判断用户是否存在
        String encryptedPassword = getEncryptedPassword(userPassword);
        User user = this.getOne(Wrappers.<User>lambdaQuery().eq(User::getUserAccount, userAccount).eq(User::getUserPassword, encryptedPassword));
        throwIf(Objects.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        // 设置用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getUserVO(user);
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @author Ezhixuan
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        // 判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        throwIf(Objects.isNull(userObj), ErrorCode.NOT_LOGIN_ERROR);
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * 获取登录用户信息
     *
     * @param request 请求
     * @return 用户信息
     * @author Ezhixuan
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        throwIf(Objects.isNull(userObj), ErrorCode.NOT_LOGIN_ERROR);
        User user = (User) userObj;
        throwIf(Objects.isNull(user.getId()), ErrorCode.NOT_LOGIN_ERROR);
        user = this.getById(user.getId());
        throwIf(Objects.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    /**
     * user对象信息脱敏
     *
     * @param user user对象
     * @return 脱敏后的user信息
     * @author Ezhixuan
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    /**
     * 获取用户脱敏信息列表
     *
     * @param userList 用户列表
     * @return 脱敏后的用户信息列表
     * @author Ezhixuan
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (ObjectUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 加密前密码
     * @return 加密后的密码
     * @author Ezhixuan
     */
    @Override
    public String getEncryptedPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获取queryWrapper
     *
     * @param queryRequest 查询请求
     * @return QueryWrapper<User>
     * @author Ezhixuan
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest queryRequest) {
        throwIf(Objects.isNull(queryRequest), ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = queryRequest.getId();
        String userAccount = queryRequest.getUserAccount();
        String userName = queryRequest.getUserName();
        String userProfile = queryRequest.getUserProfile();
        String userRole = queryRequest.getUserRole();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 判断是否为管理员
     *
     * @param user 用户
     * @return 是/否
     * @author Ezhixuan
     */
    @Override
    public boolean isAdmin(User user) {
        return Objects.nonNull(user) && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}




