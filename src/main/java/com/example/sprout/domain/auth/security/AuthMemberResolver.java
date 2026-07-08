package com.example.sprout.domain.auth.security;

import com.example.sprout.global.error.BusinessException;
import com.example.sprout.global.error.GlobalErrorCode;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthMemberResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(AuthMember.class);
        boolean isLongType = Long.class.equals(parameter.getParameterType());

        return hasAnnotation && isLongType;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !((authentication.getPrincipal()) instanceof CustomUserDetails userDetails)) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED);
        }
        return userDetails.getMemberId();
    }
}
