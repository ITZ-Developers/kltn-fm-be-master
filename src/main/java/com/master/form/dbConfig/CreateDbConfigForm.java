package com.master.form.dbConfig;

import com.master.validation.SchemaConstraint;
import com.master.validation.UsernameConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class CreateDbConfigForm {
    @NotBlank(message = "schema cannot be null")
    @SchemaConstraint
    @ApiModelProperty(name = "schema", required = true)
    private String schema;
    @NotBlank(message = "username cannot be null")
    @UsernameConstraint
    @ApiModelProperty(name = "username", required = true)
    private String username;
    @NotBlank(message = "password cannot be null")
    @ApiModelProperty(name = "password", required = true)
    private String password;
    @NotNull(message = "maxConnection cannot be null")
    @Min(value = 1, message = "maxConnection must be greater than or equal to 1")
    @ApiModelProperty(name = "maxConnection", required = true)
    private Integer maxConnection;
    @ApiModelProperty(name = "initialize")
    private Boolean initialize;
    @ApiModelProperty(name = "license")
    private String license;
    @NotNull(message = "serverProviderId cannot be null")
    @ApiModelProperty(name = "serverProviderId", required = true)
    private Long serverProviderId;
    @NotNull(message = "locationId cannot be null")
    @ApiModelProperty(name = "locationId", required = true)
    private Long locationId;
}
