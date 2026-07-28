package com.counselor.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private Long userId;
    private String name;
    private Boolean mustChangePassword;

    public LoginResponse(String accessToken,String refreshToken,String role,Long userId,String name){this(accessToken,refreshToken,role,userId,name,false);}
}
