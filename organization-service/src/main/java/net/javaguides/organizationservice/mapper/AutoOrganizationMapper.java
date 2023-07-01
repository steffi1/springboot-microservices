package net.javaguides.organizationservice.mapper;

import net.javaguides.organizationservice.dto.OrganizationDto;
import net.javaguides.organizationservice.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AutoOrganizationMapper {
    AutoOrganizationMapper MAPPER = Mappers.getMapper(AutoOrganizationMapper.class);

    OrganizationDto mapToOrganizationDto(Organization organization);
    Organization mapToOrganization(OrganizationDto organizationDto);

}
