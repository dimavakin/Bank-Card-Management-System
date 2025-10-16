package com.bankcards.service;

import com.bankcards.dto.NewUserDto;
import com.bankcards.dto.UpdateUserDto;
import com.bankcards.dto.UserDto;

public interface AdminService {
    UserDto addUser(NewUserDto newUserDto);

    void deleteUser(Long userId);

    UserDto updateUser(Long userId, UpdateUserDto updateUserDto);
}
