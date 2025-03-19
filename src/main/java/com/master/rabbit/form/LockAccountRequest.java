package com.master.rabbit.form;

import lombok.Data;

@Data
public class LockAccountRequest {
    private String app;
    private String username;
    private Integer userKind;
    private String tenantName;
}
