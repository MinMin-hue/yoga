package com.yoga.config;

import com.yoga.common.SecurityContext;
import com.yoga.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 过滤器: 解析 token 后写入 SecurityContext + 自定义上下文
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                JwtUtil.ParsedToken parsed = jwtUtil.parse(token);
                SecurityContext.set(parsed.getUserId(), parsed.getUsername(), parsed.getRole());
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        parsed.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + parsed.getRole())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // token 无效则放行, 后续由 Security 拦截
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }
}
