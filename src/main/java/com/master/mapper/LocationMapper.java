package com.master.mapper;

import com.master.form.location.CreateLocationForm;
import com.master.form.location.UpdateLocationForm;
import com.master.model.Location;
import com.master.dto.location.LocationAdminDto;
import com.master.dto.location.LocationDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CustomerMapper.class})
public interface LocationMapper {
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "logoPath", source = "logoPath")
    @Mapping(target = "bannerPath", source = "bannerPath")
    @Mapping(target = "hotline", source = "hotline")
    @Mapping(target = "settings", source = "settings")
    @Mapping(target = "language", source = "language")
    @BeanMapping(ignoreByDefault = true)
    Location fromCreateLocationFormToEntity(CreateLocationForm createLocationForm);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "logoPath", source = "logoPath")
    @Mapping(target = "bannerPath", source = "bannerPath")
    @Mapping(target = "hotline", source = "hotline")
    @Mapping(target = "settings", source = "settings")
    @Mapping(target = "language", source = "language")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateLocationFormToEntity(UpdateLocationForm updateLocationForm, @MappingTarget Location location);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "logoPath", source = "logoPath")
    @Mapping(target = "bannerPath", source = "bannerPath")
    @Mapping(target = "hotline", source = "hotline")
    @Mapping(target = "settings", source = "settings")
    @Mapping(target = "language", source = "language")
    @Mapping(target = "customer", source = "customer", qualifiedByName = "fromEntityToCustomerDto")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "modifiedDate", source = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToLocationAdminDto")
    LocationAdminDto fromEntityToLocationAdminDto(Location location);

    @IterableMapping(elementTargetType = LocationAdminDto.class, qualifiedByName = "fromEntityToLocationAdminDto")
    List<LocationAdminDto> fromEntityListToLocationAdminDtoList(List<Location> locations);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenantId", source = "tenantId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "language", source = "language")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToLocationDto")
    LocationDto fromEntityToLocationDto(Location location);

    @IterableMapping(elementTargetType = LocationDto.class, qualifiedByName = "fromEntityToLocationDto")
    List<LocationDto> fromEntityListToLocationDtoList(List<Location> locations);
}
