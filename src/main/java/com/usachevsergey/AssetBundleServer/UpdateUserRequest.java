package com.usachevsergey.AssetBundleServer;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String newUsername;
    private String newEmail;
    private String oldPassword;
    private String newPassword;
}
