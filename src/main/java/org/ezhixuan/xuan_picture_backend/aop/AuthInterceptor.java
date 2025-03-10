package org.ezhixuan.xuan_picture_backend.aop;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ezhixuan.xuan_picture_backend.annotation.AuthCheck;
import org.ezhixuan.xuan_picture_backend.exception.ErrorCode;
import org.ezhixuan.xuan_picture_backend.exception.ThrowUtils;
import org.ezhixuan.xuan_picture_backend.model.entity.User;
import org.ezhixuan.xuan_picture_backend.model.enums.UserRoleEnum;
import org.ezhixuan.xuan_picture_backend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 权限校验拦截器
 *
 * @author ezhixuan
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @SneakyThrows
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) {
        String mustRoleStr = authCheck.mustRole();
        UserRoleEnum mustRole = UserRoleEnum.getEnumByValue(mustRoleStr);
        if (Objects.isNull(mustRole)) {
            joinPoint.proceed();
        }
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User user = userService.getLoginUser(request);
        UserRoleEnum userRole = UserRoleEnum.getEnumByValue(user.getUserRole());
        ThrowUtils.throwIf(!Objects.equals(mustRole, userRole), ErrorCode.NO_AUTH_ERROR);
        return joinPoint.proceed();
    }
}
