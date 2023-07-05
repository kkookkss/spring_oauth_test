package com.sociallogintest.sociallogintest.mapper;

import com.sociallogintest.sociallogintest.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    void createUser(UserDto userDto);
    List<UserDto> findAllUser();
    void deleteUser(int id);
    Optional<UserDto> findByEmail(String email);
}
