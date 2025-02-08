package com.master.dto.customer;

import com.master.dto.ABasicAdminDto;
import com.master.dto.account.AccountDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CustomerAdminDto extends ABasicAdminDto {
    @ApiModelProperty(name = "name")
    private String name;
    @ApiModelProperty(name = "account")
    private AccountDto account;
}
