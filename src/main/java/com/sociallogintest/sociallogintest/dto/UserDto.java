package com.sociallogintest.sociallogintest.dto;

import lombok.Data;

@Data
public class UserDto {
    private int id;
    private String email;
    private String providerId;
    private String provider;
}
