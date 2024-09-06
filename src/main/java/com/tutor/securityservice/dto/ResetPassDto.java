package com.tutor.securityservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassDto {
    private String email;
    private String password;
    private String verificationCode;
}
