package org.ezhixuan.xuan_picture_backend.model.dto.user;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.enums.UserRoleEnum;

import java.io.Serializable;

/**
 * @author ezhixuan
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    @JsonIgnore
    public User toUser(String encryptedPassword, UserRoleEnum userRole) {
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "用户信息有误");
        User user = new User();
        user.setUserAccount(this.userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("无名");
        user.setUserRole(userRole.getValue());
        return user;
    }
}
