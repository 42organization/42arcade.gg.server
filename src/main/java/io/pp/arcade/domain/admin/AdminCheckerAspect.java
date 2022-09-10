package io.pp.arcade.domain.admin;

import io.pp.arcade.domain.admin.dto.AdminCheckerDto;
import io.pp.arcade.domain.security.jwt.TokenService;
import io.pp.arcade.domain.user.UserService;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.exception.AccessException;
import io.pp.arcade.global.type.RoleType;
import io.pp.arcade.global.util.ApplicationYmlRead;
import io.pp.arcade.global.util.CookieUtil;
import io.pp.arcade.global.util.HeaderUtil;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

@Aspect
@AllArgsConstructor
@Component
public class AdminCheckerAspect {
    private final TokenService tokenService;
    private final ApplicationYmlRead applicationYmlRead;

    @Pointcut("execution(* io.pp.arcade.domain.admin.controller..*(..))")
    public void managedAdminController() {
    }

    @Pointcut("execution(* io.pp.arcade.domain.admin.management..*(..))")
    public void adminManagementController() {
    }

    @Around("adminManagementController()")
    public Object checkAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Object target = joinPoint.getTarget();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        /* Request 정보 가져오기 */
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        UserDto user = null;
        HttpSession session = request.getSession();
        if (HeaderUtil.getAccessToken(request) != null) {
            /* 관리자 유저 확인
            findAdminByAccessToken에서 관리자가 아닌 경우 메인 페이지로 리다이렉트*/
            user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
            session.setAttribute("user", AdminCheckerDto.builder().intraId(user.getIntraId()).roleType(user.getRoleType()).build());
        } else {
            String cookie = CookieUtil.getCookie(request, "refresh_token")
                    .map(Cookie::getValue)
                    .orElseThrow();
            user = tokenService.findAdminByRefreshToken(cookie);
            session.setAttribute("user", AdminCheckerDto.builder().intraId(user.getIntraId()).roleType(user.getRoleType()).build());
        }
        if (user.getRoleType() != RoleType.ADMIN)
            return redirect(response);
        // 실제 실행할 메서드
        return method.invoke(target, args);
    }

    private Object redirect(HttpServletResponse response) throws Throwable {
        /* response 클라이언트 대기 모드로 바꾸기 */
        String frontUrl = applicationYmlRead.getFrontUrl();
        response.sendRedirect(frontUrl);
        return frontUrl;
    }
}
