package com.priteshchittrode.user_crud.auth;
import com.priteshchittrode.user_crud.user.User;
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


