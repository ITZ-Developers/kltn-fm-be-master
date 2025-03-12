package com.master.controller;

import com.master.constant.MasterConstant;
import com.master.dto.ApiMessageDto;
import com.master.dto.ErrorCode;
import com.master.exception.BadRequestException;
import com.master.jwt.MasterJwt;
import com.master.model.Customer;
import com.master.model.Location;
import com.master.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.Date;

public class ABasicController {
    @Autowired
    private UserServiceImpl userService;

    public long getCurrentUser(){
        MasterJwt masterJwt = userService.getAddInfoFromToken();
        return masterJwt.getAccountId();
    }

    public long getTokenId(){
        MasterJwt masterJwt = userService.getAddInfoFromToken();
        return masterJwt.getTokenId();
    }

    public MasterJwt getSessionFromToken(){
        return userService.getAddInfoFromToken();
    }

    public boolean isSuperAdmin(){
        MasterJwt masterJwt = userService.getAddInfoFromToken();
        if(masterJwt !=null){
            return masterJwt.getIsSuperAdmin();
        }
        return false;
    }

    public String getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                return oauthDetails.getTokenValue();
            }
        }
        return null;
    }

    public <T> ApiMessageDto<T> makeErrorResponse(String code, String message){
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setResult(false);
        apiMessageDto.setCode(code);
        apiMessageDto.setMessage(message);
        return apiMessageDto;
    }

    public <T> ApiMessageDto<T> makeSuccessResponse(T data, String message){
        ApiMessageDto<T> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setData(data);
        apiMessageDto.setMessage(message);
        return apiMessageDto;
    }

    private void checkValidLocation(Location location) {
        if (location == null) {
            throw new BadRequestException(ErrorCode.LOCATION_ERROR_NOT_FOUND, "Location not found");
        }
        if (!MasterConstant.STATUS_ACTIVE.equals(location.getStatus())) {
            throw new BadRequestException(ErrorCode.LOCATION_ERROR_NOT_ACTIVE, "Location not active");
        }
        if (location.getExpiredDate().before(new Date())) {
            throw new BadRequestException(ErrorCode.LOCATION_ERROR_EXPIRED, "Location is expired");
        }
        Customer customer = location.getCustomer();
        if (customer == null) {
            throw new BadRequestException(ErrorCode.CUSTOMER_ERROR_NOT_FOUND, "Customer not found");
        }
        if (!MasterConstant.STATUS_ACTIVE.equals(customer.getStatus())) {
            throw new BadRequestException(ErrorCode.CUSTOMER_ERROR_NOT_ACTIVE, "Customer not active");
        }
    }
}
