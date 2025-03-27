package com.atm.util;

import com.atm.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.List;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
            String accountNumber = jwtUtil.validateToken(token);
            String role = jwtUtil.getRoleFromToken(token); // 🟢 Chỉ cần khai báo 1 lần

//            if (accountNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(accountNumber, null, authorities);
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }

            if (accountNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String grantedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role; // ✅ Đảm bảo đúng format

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(grantedRole));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(accountNumber, null, authorities); // ✅ Dùng biến authorities

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("🔒 SecurityContext đã nhận: " + authentication.getAuthorities());
            }

        }

        filterChain.doFilter(request, response);
    }
}