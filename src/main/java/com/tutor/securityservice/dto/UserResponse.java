package com.tutor.securityservice.dto;

import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserResponse {
    private String username;
    private String email;

}
