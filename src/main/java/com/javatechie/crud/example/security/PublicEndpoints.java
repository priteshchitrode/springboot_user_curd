package com.javatechie.crud.example.security;

import org.springframework.stereotype.Component;

@Component
public class PublicEndpoints {

    private static final String[] PUBLIC_URLS = {
            "/api/auth/signup",
            "/api/auth/signin",
            "/api/auth/refresh_token"
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
