package com.master.dto.location;

import com.master.dto.ABasicAdminDto;
import com.master.dto.account.AccountDto;
import com.master.dto.customer.CustomerDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LocationAdminDto extends ABasicAdminDto {
    @ApiModelProperty(name = "tenantId")
    private String tenantId;
    @ApiModelProperty(name = "name")
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
    @ApiModelProperty(name = "customer")
    private CustomerDto customer;
}
