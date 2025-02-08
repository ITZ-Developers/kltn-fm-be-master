package com.master.controller;

import com.master.constant.MasterConstant;
import com.master.dto.ApiMessageDto;
import com.master.dto.ErrorCode;
import com.master.dto.ResponseListDto;
import com.master.dto.customer.CustomerAdminDto;
import com.master.dto.customer.CustomerDto;
import com.master.form.customer.CreateCustomerForm;
import com.master.form.customer.UpdateCustomerForm;
import com.master.mapper.CustomerMapper;
import com.master.model.Account;
import com.master.model.Customer;
import com.master.model.Group;
import com.master.model.criteria.CustomerCriteria;
import com.master.repository.AccountRepository;
import com.master.repository.CustomerRepository;
import com.master.repository.GroupRepository;
import com.master.repository.LocationRepository;
import com.master.service.MasterApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/customer")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CustomerController extends ABasicController {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MasterApiService apiService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CU_V')")
    public ApiMessageDto<CustomerAdminDto> get(@PathVariable("id") Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return makeErrorResponse(ErrorCode.CUSTOMER_ERROR_NOT_FOUND, "Not found customer");
        }
        return makeSuccessResponse(customerMapper.fromEntityToCustomerAdminDto(customer), "Get customer success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CustomerDto>>> autoComplete(CustomerCriteria customerCriteria) {
        Pageable pageable = customerCriteria.getIsPaged().equals(MasterConstant.IS_PAGED_TRUE) ? PageRequest.of(0, 10) : PageRequest.of(0, Integer.MAX_VALUE);
        customerCriteria.setStatus(MasterConstant.STATUS_ACTIVE);
        Page<Customer> customers = customerRepository.findAll(customerCriteria.getCriteria(), pageable);
        ResponseListDto<List<CustomerDto>> responseListDto = new ResponseListDto<>();
        responseListDto.setContent(customerMapper.fromEntityListToCustomerDtoList(customers.getContent()));
        responseListDto.setTotalPages(customers.getTotalPages());
        responseListDto.setTotalElements(customers.getTotalElements());
        return makeSuccessResponse(responseListDto, "Get list customer success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CU_L')")
    public ApiMessageDto<ResponseListDto<List<CustomerAdminDto>>> list(CustomerCriteria customerCriteria, Pageable pageable) {
        if (customerCriteria.getIsPaged().equals(MasterConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<Customer> customers = customerRepository.findAll(customerCriteria.getCriteria(), pageable);
        ResponseListDto<List<CustomerAdminDto>> responseListDto = new ResponseListDto<>();
        responseListDto.setContent(customerMapper.fromEntityListToCustomerAdminDtoList(customers.getContent()));
        responseListDto.setTotalPages(customers.getTotalPages());
        responseListDto.setTotalElements(customers.getTotalElements());
        return makeSuccessResponse(responseListDto, "Get list customer success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CU_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateCustomerForm createCustomerForm, BindingResult bindingResult) {
        if (accountRepository.findFirstByUsername(createCustomerForm.getUsername()).isPresent()) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_USERNAME_EXISTED, "Username existed");
        }
        if (accountRepository.findFirstByEmail(createCustomerForm.getEmail()).isPresent()) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED, "Email existed");
        }
        if (accountRepository.findFirstByPhone(createCustomerForm.getPhone()).isPresent()) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED, "Phone existed");
        }
        Group group = groupRepository.findById(createCustomerForm.getGroupId()).orElse(null);
        if (group == null) {
            return makeErrorResponse(ErrorCode.GROUP_ERROR_NOT_FOUND, "Not found group");
        }
        Account account = customerMapper.fromCreateCustomerFormToAccountEntity(createCustomerForm);
        account.setPassword(passwordEncoder.encode(createCustomerForm.getPassword()));
        account.setKind(MasterConstant.USER_KIND_CUSTOMER);
        if (!account.getKind().equals(group.getKind())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_KIND_NOT_MATCH, "Account kind does not match group kind");
        }
        account.setGroup(group);
        accountRepository.save(account);
        Customer customer = customerMapper.fromCreateCustomerFormToEntity(createCustomerForm);
        customer.setAccount(account);
        customerRepository.save(customer);
        return makeSuccessResponse(null, "Create customer success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CU_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateCustomerForm updateCustomerForm, BindingResult bindingResult) {
        Account account = accountRepository.findById(updateCustomerForm.getId()).orElse(null);
        if (account == null) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_NOT_FOUND, "Not found account");
        }
        if (!updateCustomerForm.getEmail().equals(account.getEmail()) && accountRepository.findFirstByEmail(updateCustomerForm.getEmail()).isPresent()) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED, "Email existed");
        }
        if (!updateCustomerForm.getPhone().equals(account.getPhone()) && accountRepository.findFirstByPhone(updateCustomerForm.getPhone()).isPresent()) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED, "Phone existed");
        }
        Group group = groupRepository.findById(updateCustomerForm.getGroupId()).orElse(null);
        if (group == null) {
            return makeErrorResponse(ErrorCode.GROUP_ERROR_NOT_FOUND, "Not found group");
        }
        if (StringUtils.isNotBlank(updateCustomerForm.getAvatarPath())
                && !updateCustomerForm.getAvatarPath().equals(account.getAvatarPath())) {
            apiService.deleteFile(account.getAvatarPath());
            account.setAvatarPath(updateCustomerForm.getAvatarPath());
        }
        customerMapper.fromUpdateCustomerFormToAccountEntity(updateCustomerForm, account);
        if (!account.getKind().equals(group.getKind())) {
            return makeErrorResponse(ErrorCode.ACCOUNT_ERROR_KIND_NOT_MATCH, "Account kind does not match group kind");
        }
        account.setGroup(group);
        accountRepository.save(account);
        Customer customer = customerRepository.findById(updateCustomerForm.getId()).orElse(null);
        if (customer == null) {
            return makeErrorResponse(ErrorCode.CUSTOMER_ERROR_NOT_FOUND, "Not found customer");
        }
        customerMapper.fromUpdateCustomerFormToEntity(updateCustomerForm, customer);
        customer.setAccount(account);
        customerRepository.save(customer);
        return makeSuccessResponse(null, "Update customer success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CU_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return makeErrorResponse(ErrorCode.CUSTOMER_ERROR_NOT_FOUND, "Not found customer");
        }
        if (locationRepository.existsByCustomerId(id)) {
            return makeErrorResponse(ErrorCode.CUSTOMER_ERROR_NOT_ALLOW_DELETE, "Not allowed to delete customer");
        }
        customerRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete customer success");
    }
}
