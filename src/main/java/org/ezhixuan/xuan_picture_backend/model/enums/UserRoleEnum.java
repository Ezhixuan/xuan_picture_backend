package org.ezhixuan.xuan_picture_backend.model.enums;

import lombok.Getter;

/**
 * @author ezhixuan
 */

@Getter
public enum UserRoleEnum {
    /**
     * 管理员
     */
    ADMIN("admin", "管理员"),
    /**
     * 普通用户
     */
    USER("user", "普通用户");

    private final String desc;

    private final String value;

    UserRoleEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
