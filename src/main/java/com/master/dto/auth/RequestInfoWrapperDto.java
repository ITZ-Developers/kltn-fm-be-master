package com.master.dto.auth;

import lombok.Data;

@Data
public class RequestInfoWrapperDto {
    private Long restaurantId;
    private String restaurantName;
    private String restaurantLogo;
    private String tenantId;
    private String posId;
    private Integer deviceType;
    private String loginType;
    private Integer viewStatus;
    private Integer posStatus;
    private String posTimeLastUsed;
}
