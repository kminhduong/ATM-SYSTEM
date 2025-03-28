package com.atm.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String accountNumber = jwtUtil.validateToken(token);  // Validate token và lấy accountNumber từ token
            String role = jwtUtil.getRoleFromToken(token); // Lấy role từ token

            // Kiểm tra nếu có accountNumber và chưa có authentication trong SecurityContext
            if (accountNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (role == null) {
                    role = "USER"; // Nếu không có role, mặc định là USER (hoặc có thể thay bằng một vai trò mặc định khác)
                }
                String grantedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role; // Đảm bảo đúng format của role

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(grantedRole));

                // Tạo đối tượng Authentication với các quyền tương ứng
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(accountNumber, null, authorities);

                // Thiết lập chi tiết Authentication (ví dụ như IP)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Gán đối tượng Authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("🔒 SecurityContext đã nhận: " + authentication.getAuthorities());
            }
        }

        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }
}
