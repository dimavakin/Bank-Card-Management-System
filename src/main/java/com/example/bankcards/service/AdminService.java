package com.example.bankcards.service;

import com.example.bankcards.dto.NewUserDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;

public interface AdminService {
    UserDto addUser(NewUserDto newUserDto);

    void deleteUser(Long userId);

    UserDto updateUser(Long userId, UpdateUserDto updateUserDto);
}
