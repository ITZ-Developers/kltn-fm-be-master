package com.master.form.account;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class LoginEmployeeForm {
    @NotNull(message = "userId is required")
    private Long userId;
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "tenantId is required")
    private String tenantId;
    @NotEmpty(message = "permissionIds is required")
    private List<Long> permissionIds = new ArrayList<>();
}
