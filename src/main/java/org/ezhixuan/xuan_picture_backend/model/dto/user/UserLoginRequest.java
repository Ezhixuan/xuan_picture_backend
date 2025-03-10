package org.ezhixuan.xuan_picture_backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 5234137319039568174L;

    /**
     * 用户帐号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;
}
