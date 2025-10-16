package com.example.bankcards.service.impl;

import com.example.bankcards.dto.NewUserDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicatedDataException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AdminService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminServiceImpl implements AdminService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto addUser(NewUserDto newUserDto) {
        if (userRepository.existsByEmail(newUserDto.getEmail())) {
            throw new DuplicatedDataException("Email already exists: " + newUserDto.getEmail());
        }

        User user = UserMapper.mapNewUserDtoToUser(newUserDto);
        user.setPassword(passwordEncoder.encode(newUserDto.getPassword()));

        User newUser = userRepository.save(user);
        return UserMapper.mapUserToUserDto(newUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto updateUser(Long userId, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (updateUserDto.getFirstName() != null) {
            user.setFirstName(updateUserDto.getFirstName());
        }
        if (updateUserDto.getLastName() != null) {
            user.setLastName(updateUserDto.getLastName());
        }
        if (updateUserDto.getEmail() != null) {
            if (!user.getEmail().equals(updateUserDto.getEmail()) &&
                    userRepository.existsByEmail(updateUserDto.getEmail())) {
                throw new DuplicatedDataException("Email already exists: " + updateUserDto.getEmail());
            }
            user.setEmail(updateUserDto.getEmail());
        }
        if (updateUserDto.getRoles() != null && !updateUserDto.getRoles().isEmpty()) {
            user.setRoles(updateUserDto.getRoles());
        }

        User updatedUser = userRepository.save(user);

        return UserMapper.mapUserToUserDto(updatedUser);
    }
}
