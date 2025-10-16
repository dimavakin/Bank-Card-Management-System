package com.bankcards.mapper;

import com.bankcards.dto.NewUserDto;
import com.bankcards.dto.UserDto;
import com.bankcards.entity.Role;
import com.bankcards.entity.User;

import java.util.Set;

public class UserMapper {

    public static UserDto mapUserToUserDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public static User mapNewUserDtoToUser(NewUserDto newUserDto) {
        if (newUserDto == null) {
            return null;
        }

        User user = new User();

        user.setFirstName(newUserDto.getFirstName());
        user.setLastName(newUserDto.getLastName());
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setEmail(newUserDto.getEmail());
        return user;
    }
}
