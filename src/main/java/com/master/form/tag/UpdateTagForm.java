package com.master.form.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateTagForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;
    @NotBlank(message = "name cannot be blank")
    @ApiModelProperty(required = true)
    private String name;
    @NotBlank(message = "color cannot be blank")
    @ApiModelProperty(required = true)
    private String color;
}