package com.example.bankcards.mapper;

import com.example.bankcards.dto.NewUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;

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
