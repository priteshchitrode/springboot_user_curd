package com.javatechie.crud.example.security;

import org.springframework.stereotype.Component;

@Component
public class PublicEndpoints {

    private static final String[] PUBLIC_URLS = {
            "/api/auth/sign-up",
            "/api/auth/sign-in",
            "/api/auth/refresh-token"
    };

    public boolean isPublic(String requestUri) {
        for (String url : PUBLIC_URLS) {
            if (requestUri.startsWith(url)) {
                return true;
            }
        }
        return false;
    }
}
