package com.pitang.booster_c1m1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.pitang.booster_c1m1.domain.User;
import com.pitang.booster_c1m1.dto.CreateUserDTO;
import com.pitang.booster_c1m1.dto.UserDTO;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(CreateUserDTO createUserDTO);

    UserDTO toUserDTO(User user);
}
