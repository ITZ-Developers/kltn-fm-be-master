package com.master.form.customer;

import com.master.validation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateCustomerForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;
    @NotBlank(message = "name cannot be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;
    @NameConstraint
    @NotBlank(message = "fullName cannot be null")
    @ApiModelProperty(name = "fullName", required = true)
    private String fullName;
    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;
    @NotBlank(message = "email cannot be null")
    @EmailConstraint
    @ApiModelProperty(name = "email", required = true)
    private String email;
    @NotBlank(message = "phone cannot be null")
    @PhoneConstraint
    @ApiModelProperty(name = "phone", required = true)
    private String phone;
    @NotNull(message = "groupId cannot be null")
    @ApiModelProperty(name = "groupId", required = true)
    private Long groupId;
    @ApiModelProperty(name = "status", required = true)
    @StatusConstraint
    private Integer status;
}
