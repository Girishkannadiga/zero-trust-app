package com.zerotrust.security.jwt;

import com.zerotrust.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (StringUtils.hasText(token) && tokenProvider.isTokenValid(token)) {

            // ── IP change detection ───────────────────────────────────
            String tokenIp = tokenProvider.extractIp(token);
            if (tokenIp != null && !tokenIp.isEmpty() && !isLoopback(tokenIp)) {
                String requestIp = extractClientIp(request);
                if (!isLoopback(requestIp) && !isSameIp(tokenIp, requestIp)) {
                    log.warn("IP change detected: token IP={} request IP={}", tokenIp, requestIp);
                    sendIpChangedError(response);
                    return;
                }
            }

            String email = tokenProvider.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendIpChangedError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"success\":false,\"message\":\"SESSION_IP_CHANGED\",\"data\":null}"
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) return realIp;
        return request.getRemoteAddr();
    }

    /** Treat IPv4/IPv6 loopback variants as the same address. */
    private boolean isSameIp(String ip1, String ip2) {
        if (ip1.equals(ip2)) return true;
        boolean l1 = isLoopback(ip1);
        boolean l2 = isLoopback(ip2);
        return l1 && l2;
    }

    private boolean isLoopback(String ip) {
        return ip.equals("::1")
            || ip.equals("127.0.0.1")
            || ip.startsWith("::ffff:127.")
            || ip.equals("0:0:0:0:0:0:0:1");
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
