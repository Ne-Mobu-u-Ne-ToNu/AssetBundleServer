package com.usachevsergey.AssetBundleServer.requests;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String verToken;
    private String newUsername;
    private String newEmail;
    private String oldPassword;
    private String newPassword;
    private String confPassword;
}
