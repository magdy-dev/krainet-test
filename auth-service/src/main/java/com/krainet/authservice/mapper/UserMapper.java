package com.krainet.authservice.mapper;

import com.krainet.authservice.dto.CreateUserRequest;
import com.krainet.authservice.dto.UpdateUserRequest;
import com.krainet.authservice.dto.UserDto;
import com.krainet.authservice.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {
    
    User toEntity(CreateUserRequest createUserRequest);
    
    UserDto toDto(User user);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserRequest dto, @MappingTarget User user);
}
