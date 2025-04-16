package com.usachevsergey.AssetBundleServer;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    private String confPassword;
    private Role role;
}
