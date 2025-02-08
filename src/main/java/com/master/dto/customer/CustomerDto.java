package com.master.dto.customer;

import com.master.dto.account.AccountDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CustomerDto {
    @ApiModelProperty(name = "id")
    private Long id;
    @ApiModelProperty(name = "name")
    private String name;
    @ApiModelProperty(name = "account")
    private AccountDto account;
    @ApiModelProperty(name = "status")
    private Integer status;
}
