package com.tutor.securityservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDto {
    private String password;
    private String email;
}
