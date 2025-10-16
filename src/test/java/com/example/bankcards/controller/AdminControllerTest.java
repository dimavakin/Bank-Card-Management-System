package com.example.bankcards.controller;

import com.example.bankcards.dto.NewUserDto;
import com.example.bankcards.dto.UpdateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController controller;

    private NewUserDto newUserDto;
    private UpdateUserDto updateUserDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        newUserDto = new NewUserDto();
        newUserDto.setEmail("new@example.com");
        newUserDto.setFirstName("New");
        newUserDto.setLastName("User");

        updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName("Updated");
        updateUserDto.setLastName("User");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("user@example.com");
        userDto.setFirstName("Updated");
        userDto.setLastName("User");
    }

    @Test
    void createUser_Success_Returns201() {
        when(adminService.addUser(newUserDto)).thenReturn(userDto);

        ResponseEntity<UserDto> result = controller.createUser(newUserDto);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(1L, result.getBody().getId());
        verify(adminService).addUser(newUserDto);
    }

    @Test
    void deleteUser_Success_Returns204() {
        doNothing().when(adminService).deleteUser(1L);

        ResponseEntity<Void> result = controller.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(adminService).deleteUser(1L);
    }

    @Test
    void updateUser_Success_Returns200() {
        when(adminService.updateUser(1L, updateUserDto)).thenReturn(userDto);

        ResponseEntity<UserDto> result = controller.updateUser(1L, updateUserDto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Updated", result.getBody().getFirstName());
        verify(adminService).updateUser(1L, updateUserDto);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        doThrow(new NotFoundException("User not found")).when(adminService).deleteUser(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> controller.deleteUser(999L));
        assertEquals("User not found", exception.getMessage());
        verify(adminService).deleteUser(999L);
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(adminService.updateUser(999L, updateUserDto))
                .thenThrow(new NotFoundException("User not found"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> controller.updateUser(999L, updateUserDto));
        assertEquals("User not found", exception.getMessage());
        verify(adminService).updateUser(999L, updateUserDto);
    }
}