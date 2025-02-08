package com.master.controller;

import com.master.constant.MasterConstant;
import com.master.dto.ApiMessageDto;
import com.master.dto.ErrorCode;
import com.master.dto.ResponseListDto;
import com.master.dto.dbConfig.DbConfigAdminDto;
import com.master.dto.dbConfig.DbConfigDto;
import com.master.form.dbConfig.CreateDbConfigForm;
import com.master.form.dbConfig.UpdateDbConfigForm;
import com.master.mapper.DbConfigMapper;
import com.master.model.DbConfig;
import com.master.model.Location;
import com.master.model.ServerProvider;
import com.master.model.criteria.DbConfigCriteria;
import com.master.repository.DbConfigRepository;
import com.master.repository.LocationRepository;
import com.master.repository.ServerProviderRepository;
import com.master.utils.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/db-config")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class DbConfigController extends ABasicController {
    @Autowired
    private DbConfigRepository dbConfigRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private ServerProviderRepository serverProviderRepository;
    @Autowired
    private DbConfigMapper dbConfigMapper;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DB_C_V')")
    public ApiMessageDto<DbConfigAdminDto> get(@PathVariable("id") Long id) {
        DbConfig dbConfig = dbConfigRepository.findById(id).orElse(null);
        if (dbConfig == null) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_NOT_FOUND, "Not found db config");
        }
        return makeSuccessResponse(dbConfigMapper.fromEntityToDbConfigAdminDto(dbConfig), "Get db config success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<DbConfigDto>>> autoComplete(DbConfigCriteria dbConfigCriteria) {
        Pageable pageable = dbConfigCriteria.getIsPaged().equals(MasterConstant.IS_PAGED_TRUE) ? PageRequest.of(0, 10) : PageRequest.of(0, Integer.MAX_VALUE);
        dbConfigCriteria.setStatus(MasterConstant.STATUS_ACTIVE);
        Page<DbConfig> dbConfigs = dbConfigRepository.findAll(dbConfigCriteria.getCriteria(), pageable);
        ResponseListDto<List<DbConfigDto>> responseListDto = new ResponseListDto<>();
        responseListDto.setContent(dbConfigMapper.fromEntityListToDbConfigDtoList(dbConfigs.getContent()));
        responseListDto.setTotalPages(dbConfigs.getTotalPages());
        responseListDto.setTotalElements(dbConfigs.getTotalElements());
        return makeSuccessResponse(responseListDto, "Get list db config success");
    }

    @GetMapping(value = "/get-by-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<DbConfigAdminDto> getByName(@RequestParam("name") String name) {
        DbConfig dbConfig = dbConfigRepository.findFirstByNameAndInitialize(name, true).orElse(null);
        if (dbConfig == null) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_NOT_FOUND, "Not found db config");
        }
        return makeSuccessResponse(dbConfigMapper.fromEntityToDbConfigAdminDto(dbConfig), "Get db config success");
    }

    @GetMapping(value = "/get-by-tenant-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<DbConfigAdminDto> getByTenantId(@RequestParam("tenantId") String tenantId) {
        DbConfig dbConfig = dbConfigRepository.findByTenantId(tenantId).orElse(null);
        if (dbConfig == null) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_NOT_FOUND, "Not found db config");
        }
        return makeSuccessResponse(dbConfigMapper.fromEntityToDbConfigAdminDto(dbConfig), "Get db config success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DB_C_L')")
    public ApiMessageDto<ResponseListDto<List<DbConfigAdminDto>>> list(DbConfigCriteria dbConfigCriteria, Pageable pageable) {
        if (dbConfigCriteria.getIsPaged().equals(MasterConstant.IS_PAGED_FALSE)) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        Page<DbConfig> dbConfigs = dbConfigRepository.findAll(dbConfigCriteria.getCriteria(), pageable);
        ResponseListDto<List<DbConfigAdminDto>> responseListDto = new ResponseListDto<>();
        responseListDto.setContent(dbConfigMapper.fromEntityListToDbConfigAdminDtoList(dbConfigs.getContent()));
        responseListDto.setTotalPages(dbConfigs.getTotalPages());
        responseListDto.setTotalElements(dbConfigs.getTotalElements());
        return makeSuccessResponse(responseListDto, "Get list db config success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DB_C_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateDbConfigForm createDbConfigForm, BindingResult bindingResult) {
        ServerProvider serverProvider = serverProviderRepository.findById(createDbConfigForm.getServerProviderId()).orElse(null);
        if (serverProvider == null) {
            return makeErrorResponse(ErrorCode.SERVER_PROVIDER_ERROR_NOT_FOUND, "Not found server provider");
        }
        if (dbConfigRepository.findFirstByUsername(createDbConfigForm.getUsername()).isPresent()) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_USERNAME_EXISTED, "Username existed");
        }
        String url = TenantUtils.getDbConfigUrl(serverProvider.getMySqlJdbcUrl(), createDbConfigForm.getSchema());
        if (dbConfigRepository.findFirstByUrl(url).isPresent()) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_URL_EXISTED, "URL existed");
        }
        Location location = locationRepository.findById(createDbConfigForm.getLocationId()).orElse(null);
        if (location == null) {
            return makeErrorResponse(ErrorCode.LOCATION_ERROR_NOT_FOUND, "Not found location");
        }
        if (dbConfigRepository.findFirstByLocationId(location.getId()).isPresent()) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_LOCATION_EXISTED, "Db config existed in this location");
        }
        Integer currentTenantsCount = dbConfigRepository.countByServerProviderId(serverProvider.getId());
        if (currentTenantsCount >= serverProvider.getMaxTenant()) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_REACHED_LIMIT, "Tenant limit reached for this server provider");
        }
        DbConfig dbConfig = dbConfigMapper.fromCreateDbConfigFormToEntity(createDbConfigForm);
        dbConfig.setUrl(url);
        dbConfig.setDriverClassName(serverProvider.getDriverClassName());
        dbConfig.setServerProvider(serverProvider);
        dbConfig.setLocation(location);
        dbConfig.setName("tenant_id_" + location.getId());
        TenantUtils.createTenantDatabase(dbConfig);
        dbConfigRepository.save(dbConfig);
        serverProvider.setCurrentTenantCount(currentTenantsCount + 1);
        serverProviderRepository.save(serverProvider);
        return makeSuccessResponse(null, "Create db config success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DB_C_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateDbConfigForm updateDbConfigForm, BindingResult bindingResult) {
        DbConfig dbConfig = dbConfigRepository.findById(updateDbConfigForm.getId()).orElse(null);
        if (dbConfig == null) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_NOT_FOUND, "Not found db config");
        }
        dbConfigMapper.fromUpdateDbConfigFormToEntity(updateDbConfigForm, dbConfig);
        dbConfigRepository.save(dbConfig);
        return makeSuccessResponse(null, "Update db config success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DB_C_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        DbConfig dbConfig = dbConfigRepository.findById(id).orElse(null);
        if (dbConfig == null) {
            return makeErrorResponse(ErrorCode.DB_CONFIG_ERROR_NOT_FOUND, "Not found db config");
        }
        TenantUtils.deleteTenantDatabase(dbConfig);
        dbConfigRepository.deleteById(id);
        ServerProvider serverProvider = dbConfig.getServerProvider();
        serverProvider.setCurrentTenantCount(serverProvider.getCurrentTenantCount() - 1);
        serverProviderRepository.save(serverProvider);
        return makeSuccessResponse(null, "Delete db config success");
    }
}
