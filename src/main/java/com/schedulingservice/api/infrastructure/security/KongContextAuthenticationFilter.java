package com.schedulingservice.api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class KongContextAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String USER_ID_HEADER = "X-User-ID";

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        final String rolesHeader = request.getHeader(USER_ROLES_HEADER);
        final String userId = request.getHeader(USER_ID_HEADER);

        final List<SimpleGrantedAuthority> authorities = parseRoles(rolesHeader);
        final RequestContextPrincipal principal = new RequestContextPrincipal(userId, rolesHeader);
        final UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
            principal,
            null,
            authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> parseRoles(final String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rolesHeader.split(","))
            .map(String::trim)
            .filter(role -> !role.isBlank())
            .map(role -> "ROLE_" + role.toUpperCase(Locale.ROOT))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    private record RequestContextPrincipal(String userId, String rolesHeader) {
    }
}
