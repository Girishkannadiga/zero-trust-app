package com.zerotrust.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeoLocationService {

    private static final Logger log = LoggerFactory.getLogger(GeoLocationService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public String getLocation(String ip) {
        if (ip == null || isPrivateOrLoopback(ip)) return "Local";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    "http://ip-api.com/json/" + ip + "?fields=status,city,country", Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                String city    = (String) response.getOrDefault("city",    "");
                String country = (String) response.getOrDefault("country", "");
                if (!city.isBlank() && !country.isBlank()) return city + ", " + country;
                if (!country.isBlank()) return country;
            }
        } catch (Exception e) {
            log.debug("GeoLocation lookup failed for IP {}: {}", ip, e.getMessage());
        }
        return "Unknown";
    }

    private boolean isPrivateOrLoopback(String ip) {
        return ip.equals("127.0.0.1") || ip.equals("::1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("192.168.") || ip.startsWith("10.")
                || ip.startsWith("172.16.") || ip.startsWith("::ffff:127.");
    }
}
