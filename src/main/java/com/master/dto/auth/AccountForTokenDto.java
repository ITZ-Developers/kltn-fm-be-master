package com.master.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AccountForTokenDto {
    @ApiModelProperty(name = "id")
    private Long id;
    @ApiModelProperty(name = "kind")
    private int kind;
    @ApiModelProperty(name = "username")
    private String username;
    @ApiModelProperty(name = "email")
    private String email;
    @ApiModelProperty(name = "fullName")
    private String fullName;
    @ApiModelProperty(name = "isSuperAdmin")
    private Boolean isSuperAdmin;
    @ApiModelProperty(name="isMfa")
    private Boolean isMfa;
}
