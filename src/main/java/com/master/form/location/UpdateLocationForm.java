package com.master.form.location;

import com.master.validation.UsernameConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel
public class UpdateLocationForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;
    @NotBlank(message = "tenantId cannot be null")
    @UsernameConstraint
    @ApiModelProperty(name = "tenantId", required = true)
    private String tenantId;
    @NotBlank(message = "name cannot be null")
    @ApiModelProperty(name = "name", required = true)
    private String name;
    @ApiModelProperty(name = "address")
    private String address;
    @ApiModelProperty(name = "logoPath")
    private String logoPath;
    @ApiModelProperty(name = "bannerPath")
    private String bannerPath;
    @ApiModelProperty(name = "hotline")
    private String hotline;
    @ApiModelProperty(name = "settings")
    private String settings;
    @ApiModelProperty(name = "language")
    private String language;
}
