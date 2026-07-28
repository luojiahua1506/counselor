package com.counselor.security;

import com.counselor.entity.Admin;
import com.counselor.entity.Counselor;
import com.counselor.repository.AdminRepository;
import com.counselor.repository.CounselorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CounselorRepository counselorRepository;
    private final AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            if ("ROLE_COUNSELOR".equals(role)) {
                Counselor counselor = counselorRepository.findByUsername(username).orElse(null);
                if (counselor != null) {
                    String path = request.getRequestURI();
                    boolean passwordOnly = Boolean.TRUE.equals(counselor.getMustChangePassword())
                            && path.startsWith("/api/counselor/")
                            && !path.equals("/api/counselor/password")
                            && !path.equals("/api/counselor/profile");
                    if (passwordOnly) { filterChain.doFilter(request, response); return; }
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(counselor, null,
                                    List.of(new SimpleGrantedAuthority(role)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else if ("ROLE_ADMIN".equals(role)) {
                Admin admin = adminRepository.findByUsername(username).orElse(null);
                if (admin != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(admin, null,
                                    List.of(new SimpleGrantedAuthority(role)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
