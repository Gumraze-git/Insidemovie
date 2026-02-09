package com.insidemovie.backend.api.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 발급 및 검증 유틸리티.
 *
 * <p>Access/Refresh 토큰 생성, 토큰 파싱, 인증 객체 생성, 유효성 검증을 담당한다.
 * {@code sub}에는 memberId 문자열이 들어가며, {@code auth} 클레임으로 권한을 저장한다.
 */
@Component
@Slf4j
public class JwtProvider {
    private static final String AUTHORITIES_KEY = "auth";

    private final Key key;
    private final long accessExpMs;
    private final long refreshExpMs;

    /**
     * JWT 프로퍼티를 주입받아 서명 키와 만료 시간을 초기화한다.
     *
     * @param jwtProperties JWT 설정값
     */
    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = jwtProperties.getAccessExpMs();
        this.refreshExpMs = jwtProperties.getRefreshExpMs();
    }

    /**
     * memberId 기반 Access 토큰을 생성한다.
     *
     * @param memberId 사용자 식별자
     * @param authorities 사용자 권한 목록
     * @return 서명된 Access 토큰 문자열
     */
    public String generateAccessToken(Long memberId, List<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        return buildAccessToken(memberId.toString(), roles);
    }

    /**
     * memberId 기반 Refresh 토큰을 생성한다.
     *
     * @param memberId 사용자 식별자
     * @return 서명된 Refresh 토큰 문자열
     */
    public String generateRefreshToken(Long memberId) {
        return buildRefreshToken(memberId.toString());
    }

    private String buildAccessToken(String subject, List<String> roles) {
        Date accessTokenExpiresIn = new Date(System.currentTimeMillis() + accessExpMs);

        return Jwts.builder()
            .setSubject(subject)
            .claim(AUTHORITIES_KEY, roles)
            .setExpiration(accessTokenExpiresIn)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    private String buildRefreshToken(String subject) {
        Date refreshTokenExpiresIn = new Date(System.currentTimeMillis() + refreshExpMs);

        return Jwts.builder()
            .setSubject(subject)
            .setExpiration(refreshTokenExpiresIn)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    /**
     * 토큰의 클레임에서 인증 객체를 생성한다.
     *
     * @param token JWT 문자열
     * @return Spring Security Authentication
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        List<String> roles = Optional.ofNullable(claims.get(AUTHORITIES_KEY, List.class))
            .orElseGet(Collections::emptyList);

        Collection<GrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Access/Refresh 공통 유효성 검증.
     * null/blank 는 단순 false (로그 X)로 처리해 노이즈를 줄인다.
     *
     * @param token JWT 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("JWT invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Refresh 토큰 검증용 래퍼.
     * 추후 별도 정책(예: prefix, 저장소 검증)이 필요하면 분리한다.
     *
     * @param token JWT 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }

    /**
     * 토큰의 Claims를 추출한다.
     * 만료된 토큰은 예외에서 Claims를 꺼내 반환한다.
     *
     * @param token JWT 문자열
     * @return Claims
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
