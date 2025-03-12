package com.master.form.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateTagForm {
    @NotBlank(message = "name cannot be blank")
    @ApiModelProperty(required = true)
    private String name;
    @NotBlank(message = "color cannot be blank")
    @ApiModelProperty(required = true)
    private String color;
}
