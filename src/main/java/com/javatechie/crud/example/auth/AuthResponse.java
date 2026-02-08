package com.javatechie.crud.example.auth;
import com.javatechie.crud.example.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private User user;
    private String accessToken;
    private String refreshToken;
}


