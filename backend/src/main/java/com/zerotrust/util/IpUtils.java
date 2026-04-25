package com.zerotrust.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IpUtils {

    private static final String[] FORWARDED_FOR_HEADERS = {
            "X-Real-IP",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public String extractClientIp(HttpServletRequest request) {
        for (String header : FORWARDED_FOR_HEADERS) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    public boolean isPrivateIp(String ip) {
        if (ip == null) return false;
        return ip.startsWith("192.168.") ||
               ip.startsWith("10.")      ||
               ip.startsWith("172.16.") ||
               ip.equals("127.0.0.1")   ||
               ip.equals("0:0:0:0:0:0:0:1") ||
               ip.equals("::1");
    }
}
