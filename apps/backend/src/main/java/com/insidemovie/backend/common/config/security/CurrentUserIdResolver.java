package com.insidemovie.backend.common.config.security;

import com.insidemovie.backend.common.exception.UnAuthorizedException;
import com.insidemovie.backend.common.response.ErrorStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserIdResolver {

    public Long resolve(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new UnAuthorizedException(ErrorStatus.USER_UNAUTHORIZED.getMessage());
        }
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new UnAuthorizedException(ErrorStatus.USER_UNAUTHORIZED.getMessage());
        }
    }
}
