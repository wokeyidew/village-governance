package com.scau.village.module.auth.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String phone;
    private String password;
}